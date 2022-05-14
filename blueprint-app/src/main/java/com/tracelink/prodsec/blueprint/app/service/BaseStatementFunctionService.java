package com.tracelink.prodsec.blueprint.app.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementImportException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementFunctionRepository;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;

/**
 * Service to import base statement functions on application startup.
 *
 * @author mcool
 */
@Service
public class BaseStatementFunctionService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BaseStatementFunctionService.class);
	private static final Pattern LOWERCASE_UNDERSCORE_PATTERN = Pattern
			.compile("^[a-z0-9]+(?:_[a-z0-9]+)*$");
	private final BaseStatementFunctionRepository functionRepository;
	private final PolicyTypeRepository policyTypeRepository;
	private final Validator validator;

	public BaseStatementFunctionService(
			@Autowired BaseStatementFunctionRepository functionRepository,
			@Autowired PolicyTypeRepository policyTypeRepository) {
		this.functionRepository = functionRepository;
		this.policyTypeRepository = policyTypeRepository;
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	/**
	 * Validates the given list of {@link BaseStatementFunctionDto}s and saves them to the database
	 * as {@link BaseStatementFunctionEntity}s. Only saves the functions if every function is valid.
	 *
	 * @param functionDtos the list of function DTOs to validate and save
	 * @return the base statement functions that are saved to the database
	 * @throws BaseStatementImportException if any of the functions is invalid
	 */
	public Map<String, BaseStatementFunctionEntity> importBaseStatementFunctions(
			List<BaseStatementFunctionDto> functionDtos) throws BaseStatementImportException {
		// Check that each function is valid
		if (!functionDtos.stream().allMatch(this::isBaseStatementFunctionValid)) {
			throw new BaseStatementImportException(
					"At least one imported base statement function is invalid. Check the log for more details");
		}
		// Check that the functions are valid together
		if (!areBaseStatementFunctionsValid(functionDtos)) {
			throw new BaseStatementImportException(
					"The imported base statement functions are invalid. Check the log for more details");
		}
		// Save policy types to the database
		Map<String, PolicyTypeEntity> policyTypeMap = importFunctionPolicyTypes(functionDtos);
		// Save functions to the database and return
		Map<String, BaseStatementFunctionEntity> functions = functionRepository.saveAll(
				functionDtos.stream().map(BaseStatementFunctionDto::toEntity)
						.collect(Collectors.toSet())).stream()
				.collect(Collectors
						.toMap(BaseStatementFunctionEntity::getName, Function.identity()));
		// Assign policy types and dependencies and re-save to database
		Map<String, BaseStatementFunctionDto> functionDtoMap = functionDtos.stream().collect(
				Collectors.toMap(BaseStatementFunctionDto::getName, Function.identity()));
		functions.values().forEach(function -> {
			Set<PolicyTypeEntity> policyTypes = functionDtoMap.get(function.getName())
					.getPolicyTypes().stream().map(policyTypeMap::get).collect(
							Collectors.toSet());
			function.setPolicyTypes(policyTypes);
		});
		functions.values().forEach(function -> {
			Set<BaseStatementFunctionEntity> dependencies = functionDtoMap.get(function.getName())
					.getDependencies().stream().map(functions::get).collect(Collectors.toSet());
			function.setDependencies(dependencies);
		});
		functionRepository.saveAll(functions.values());
		functionRepository.flush();
		return functions;
	}

	private boolean areBaseStatementFunctionsValid(List<BaseStatementFunctionDto> functionDtos) {
		boolean valid = true;
		// Identify duplicate function names
		Set<String> functionNames = new HashSet<>();
		Set<String> duplicateFunctionNames = functionDtos.stream()
				.map(BaseStatementFunctionDto::getName)
				.filter(name -> !functionNames.add(name))
				.collect(Collectors.toSet());
		// If there are duplicates, log an error and include duplicate function names
		if (!duplicateFunctionNames.isEmpty()) {
			LOGGER.error(
					"The following base statement function names are duplicated '{}'. Function names must be unique",
					String.join("', '", duplicateFunctionNames));
			valid = false;
		}
		// Identify undefined function dependencies
		Set<String> undefinedDependencies = functionDtos
				.stream()
				.flatMap(dto -> dto.getDependencies().stream()
						.map(dependency -> new SimpleEntry<>(dto.getName(), dependency)))
				.filter(entry -> !functionNames.contains(entry.getValue()))
				.map(entry -> entry.getKey() + "': '" + entry.getValue())
				.collect(Collectors.toSet());
		// If there are any undefined function dependencies, log an error
		if (!undefinedDependencies.isEmpty()) {
			LOGGER.error("The following functions have dependencies that are undefined: '{}'",
					String.join("', '", undefinedDependencies));
			valid = false;
		}
		// If there are any cyclic dependencies, log an error
		Map<String, BaseStatementFunctionDto> functionMap = functionDtos.stream()
				.collect(Collectors.toMap(BaseStatementFunctionDto::getName,
						Function.identity(), (f1, f2) -> f1));
		Set<String> cyclicDependencies = functionMap.keySet().stream()
				.filter(functionName -> hasCyclicDependencies(functionName, functionMap))
				.collect(Collectors.toSet());
		if (!cyclicDependencies.isEmpty()) {
			LOGGER.error("The following functions have cyclic dependencies: '{}'",
					String.join("', '", cyclicDependencies));
			valid = false;
		}
		return valid;
	}


	private boolean isBaseStatementFunctionValid(BaseStatementFunctionDto functionDto) {
		LOGGER.info("Validating base statement function '{}'", functionDto.getName());
		boolean valid = true;
		String name = functionDto.getName();
		// Validate basic checks
		Set<String> violations = validator.validate(functionDto).stream()
				.map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		if (!violations.isEmpty()) {
			LOGGER.error(
					"The base statement function '{}' has the following validation errors: '{}'",
					name, String.join("', '", violations));
			valid = false;
		}
		// Ensure function name matches the regex pattern
		if (StringUtils.isNotBlank(name) && !LOWERCASE_UNDERSCORE_PATTERN.matcher(name).matches()) {
			LOGGER.error(
					"The base statement function name '{}' is invalid. Function names must only contain lowercase letters, numbers and underscores",
					name);
			valid = false;
		}
		// Ensure function parameters match the regex pattern
		Set<String> invalidParameters = functionDto.getParameters().stream()
				.filter(param -> StringUtils.isNotBlank(param)
						&& !LOWERCASE_UNDERSCORE_PATTERN.matcher(param).matches())
				.collect(Collectors.toSet());
		if (!invalidParameters.isEmpty()) {
			LOGGER.error(
					"The following parameters for the base statement function '{}' are invalid: '{}' Function parameters must only contain lowercase letters, numbers and underscores",
					name, String.join("', '", invalidParameters));
			valid = false;
		}
		return valid;
	}

	private boolean hasCyclicDependencies(String functionName,
			Map<String, BaseStatementFunctionDto> functionMap) {
		// Set up list of visited functions and dependencies to visit
		Set<String> visited = new HashSet<>();
		visited.add(functionName);
		Stack<String> toVisit = new Stack<>();
		toVisit.addAll(functionMap.get(functionName).getDependencies());
		// DFS of dependencies to identify cycles
		while (!toVisit.empty()) {
			String dependency = toVisit.pop();
			if (!visited.add(dependency)) {
				return true;
			}
			if (functionMap.containsKey(dependency)) {
				toVisit.addAll(functionMap.get(dependency).getDependencies());
			}
		}
		return false;
	}

	private Map<String, PolicyTypeEntity> importFunctionPolicyTypes(
			List<BaseStatementFunctionDto> functionDtos) {
		// Get the set of policy types defined in the base statements and functions
		Set<String> policyTypes = functionDtos.stream()
				.map(BaseStatementFunctionDto::getPolicyTypes)
				.flatMap(Collection::stream).collect(Collectors.toSet());
		// Create entity objects and save to database
		List<PolicyTypeEntity> policyTypeEntities = policyTypeRepository
				.saveAll(policyTypes.stream().map(name -> {
					PolicyTypeEntity policyType = new PolicyTypeEntity();
					policyType.setName(name);
					return policyType;
				}).collect(Collectors.toSet()));
		policyTypeRepository.flush();
		return policyTypeEntities.stream()
				.collect(Collectors.toMap(PolicyTypeEntity::getName, Function.identity()));
	}
}
