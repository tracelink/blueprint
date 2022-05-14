package com.tracelink.prodsec.blueprint.app.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementRepository;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.ConfiguredArgument;
import com.tracelink.prodsec.blueprint.core.statement.ConstantArgument;

/**
 * Service to import base statements on application startup and retrieve them from the database.
 *
 * @author mcool
 */
@Service
public class BaseStatementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseStatementService.class);
	private static final Pattern ALPHABET_SPACE_PATTERN = Pattern
			.compile("^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$");
	private final BaseStatementRepository baseStatementRepository;
	private final PolicyTypeRepository policyTypeRepository;
	private final BaseStatementFunctionService functionService;
	private final Validator validator;

	public BaseStatementService(@Autowired BaseStatementRepository baseStatementRepository,
			@Autowired PolicyTypeRepository policyTypeRepository,
			@Autowired BaseStatementFunctionService functionService) {
		this.baseStatementRepository = baseStatementRepository;
		this.policyTypeRepository = policyTypeRepository;
		this.functionService = functionService;
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	/*
	 * Base statement repository methods
	 */

	/**
	 * Gets the base statement with the given name from the database.
	 *
	 * @param name the name of the base statement to retrieve
	 * @return the base statement, or null
	 */
	public BaseStatementEntity getBaseStatement(String name) {
		return baseStatementRepository.findByName(name);
	}

	/**
	 * Gets all base statements that are associated with the given policy type.
	 *
	 * @param name the policy type to retrieve base statements for
	 * @return a list of associated base statements
	 */
	public Map<String, BaseStatementDto> getBaseStatementsForPolicyType(String name) {
		PolicyTypeEntity policyType = policyTypeRepository.findByName(name);
		if (policyType == null) {
			return Collections.emptyMap();
		}
		List<BaseStatementEntity> baseStatements = baseStatementRepository
				.findByPolicyTypesInOrderByNameAsc(Collections.singleton(policyType));
		return baseStatements.stream().map(BaseStatementEntity::toDto)
				.collect(Collectors
						.toMap(BaseStatementDto::getName, Function.identity(), (s1, s2) -> s1,
								TreeMap::new));
	}

	/*
	 * Policy type repository methods
	 */

	public List<String> getPolicyTypes() {
		return policyTypeRepository.findAll().stream()
				.map(PolicyTypeEntity::getName).sorted().collect(Collectors.toUnmodifiableList());
	}

	public PolicyTypeEntity getPolicyType(String name) {
		return policyTypeRepository.findByName(name);
	}

	/*
	 * Base statement import methods
	 */

	/**
	 * Validates the given lists of {@link BaseStatementDto}s and {@link BaseStatementFunctionDto}s
	 * and saves them to the database as {@link BaseStatementEntity}s and {@link
	 * BaseStatementFunctionEntity}s,
	 * respectively. Only saves the base statements, functions and arguments if everything is
	 * valid.
	 *
	 * @param baseStatementDtos the list of base statement DTOs to validate and save
	 * @param functionDtos      the list of function DTOs to validate and save
	 * @throws BaseStatementImportException if any of the base statements, functions or arguments is
	 *                                      invalid
	 */
	public void importBaseStatements(List<BaseStatementDto> baseStatementDtos,
			List<BaseStatementFunctionDto> functionDtos) throws BaseStatementImportException {
		// Check that each base statement is valid
		if (!baseStatementDtos.stream().allMatch(this::isBaseStatementValid)) {
			throw new BaseStatementImportException(
					"At least one imported base statement is invalid");
		}
		// Check that the base statements are valid together
		if (!areBaseStatementsValid(baseStatementDtos,
				functionDtos.stream().map(BaseStatementFunctionDto::getName)
						.filter(Objects::nonNull)
						.collect(Collectors.toSet()))) {
			throw new BaseStatementImportException(
					"The imported base statements are invalid");
		}
		// Check that the base statement arguments are valid
		if (!baseStatementDtos.stream().allMatch(
				baseStatementDto -> areBaseStatementArgumentsValid(baseStatementDto,
						functionDtos))) {
			throw new BaseStatementImportException(
					"The imported base statement arguments are invalid");
		}
		// Validate and save functions to the database
		Map<String, BaseStatementFunctionEntity> functions = functionService
				.importBaseStatementFunctions(functionDtos);
		// Save base statements and arguments to the database
		baseStatementRepository.saveAll(baseStatementDtos.stream().map(dto -> {
			BaseStatementEntity baseStatement = dto.toEntity();
			BaseStatementFunctionEntity function = functions.get(dto.getFunction());
			baseStatement.setFunction(function);
			Map<String, PolicyTypeEntity> policyTypes = function.getPolicyTypes().stream()
					.collect(Collectors.toMap(PolicyTypeEntity::getName, Function.identity()));
			baseStatement.setPolicyTypes(
					dto.getPolicyTypes().stream().filter(policyTypes::containsKey)
							.map(policyTypes::get).collect(Collectors.toSet()));
			return baseStatement;
		}).collect(Collectors.toSet()));
		baseStatementRepository.flush();
	}

	private boolean areBaseStatementsValid(List<BaseStatementDto> baseStatementDtos,
			Set<String> functionNames) {
		boolean valid = true;
		// Identify duplicate names
		Set<String> baseStatementNames = new HashSet<>();
		Set<String> duplicateStatementNames = baseStatementDtos.stream()
				.map(BaseStatementDto::getName)
				.filter(name -> !baseStatementNames.add(name))
				.collect(Collectors.toSet());
		// If there are duplicates, log an error and include duplicate names
		if (!duplicateStatementNames.isEmpty()) {
			LOGGER.error(
					"The following base statement names are duplicated '{}'. Names must be unique",
					String.join("', '", duplicateStatementNames));
			valid = false;
		}
		// Identify undefined evaluated functions
		Set<String> undefinedFunctions = baseStatementDtos.stream()
				.filter(dto -> !functionNames.contains(dto.getFunction()))
				.map(dto -> dto.getName() + "': '" + dto.getFunction())
				.collect(Collectors.toSet());
		// If there are any undefined evaluated functions, log an error
		if (!undefinedFunctions.isEmpty()) {
			LOGGER.error(
					"The following base statements have evaluated functions that are undefined: '{}'",
					String.join("'; '", undefinedFunctions));
			valid = false;
		}
		return valid;
	}

	private boolean isBaseStatementValid(BaseStatementDto baseStatementDto) {
		LOGGER.info("Validating base statement '{}'", baseStatementDto.getName());
		boolean valid = true;
		String name = baseStatementDto.getName();
		// Validate basic checks
		Set<String> violations = validator.validate(baseStatementDto).stream()
				.map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		if (!violations.isEmpty()) {
			LOGGER.error(
					"The base statement '{}' has the following validation errors: '{}'",
					name, String.join("', '", violations));
			valid = false;
		}
		// Ensure base statement name matches the regex pattern
		if (StringUtils.isNotBlank(name) && !ALPHABET_SPACE_PATTERN.matcher(name).matches()) {
			LOGGER.error(
					"The base statement name '{}' is invalid. Names must only contain letters and spaces, with no whitespace at the beginning or end",
					name);
			valid = false;
		}
		return valid;
	}

	private boolean areBaseStatementArgumentsValid(BaseStatementDto baseStatementDto,
			List<BaseStatementFunctionDto> functionDtos) {
		List<BaseStatementArgumentDto> argumentDtos = baseStatementDto
				.getArguments();
		boolean valid = true;
		// Ensure base statement arguments are valid
		LOGGER.info("Validating arguments for base statement '{}'", baseStatementDto.getName());
		if (!IntStream.range(0, argumentDtos.size())
				.allMatch(index -> isBaseStatementArgumentValid(argumentDtos.get(index), index))) {
			LOGGER.error(
					"At least one argument for the base statement '{}' is invalid",
					baseStatementDto.getName());
			valid = false;
		}
		// Ensure the base statement has the correct number of arguments for the evaluated function
		BaseStatementFunctionDto evaluatedFunctionDto = functionDtos.stream()
				.filter(functionDto -> functionDto.getName() != null)
				.filter(functionDto -> functionDto.getName()
						.equals(baseStatementDto.getFunction()))
				.findFirst().orElse(null); // We've already checked that the function exists
		if (evaluatedFunctionDto == null
				|| argumentDtos.size() != evaluatedFunctionDto.getParameters().size()) {
			LOGGER.error(
					"The number of arguments defined for the base statement '{}' does not match the number of parameters for its evaluated function '{}'",
					baseStatementDto.getName(), baseStatementDto.getFunction());
			valid = false;
		}
		return valid;
	}

	private boolean isBaseStatementArgumentValid(BaseStatementArgumentDto argumentDto, int index) {
		boolean valid = true;
		// Validate basic checks
		Set<String> violations = validator.validate(argumentDto,
				argumentDto.isConstant() ? ConstantArgument.class : ConfiguredArgument.class)
				.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		if (!violations.isEmpty()) {
			LOGGER.error(
					"The base statement argument at index {} has the following validation errors: '{}'",
					index, String.join("', '", violations));
			valid = false;
		}
		// Perform validation based on whether the argument is constant
		if (argumentDto.isConstant()) {
			// TODO Validation for injected code?
		} else if (argumentDto.getEnumValues() != null) {
			// Ensure provided enum values match the given type
			String baseType = argumentDto.getType().getName().replaceAll("Array", "");
			ArgumentType argumentType = ArgumentType.getTypeForName(baseType);
			if (argumentType == null) {
				LOGGER.error(
						"The base statement argument at index {} has an unknown base type '{}'",
						index, baseType);
				valid = false;
			} else if (!argumentDto.getEnumValues().stream()
					.allMatch(value -> argumentType.matchesArgument(value, false))) {
				LOGGER.error(
						"The base statement argument at index {} has enum values that do not match the type '{}'",
						index, argumentType.getDisplayName());
				valid = false;
			}
		}
		return valid;
	}
}
