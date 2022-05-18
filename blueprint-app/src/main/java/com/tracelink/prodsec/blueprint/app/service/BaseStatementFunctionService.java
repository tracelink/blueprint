package com.tracelink.prodsec.blueprint.app.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementFunctionException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementFunctionRepository;
import com.tracelink.prodsec.blueprint.app.rulesets.SavedPolicyRuleset;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.PolicyBuilder;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderError;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleViolation;
import com.tracelink.prodsec.blueprint.core.rulesets.configuration.ConfigurationRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.LogicRuleset;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Service to import functions on application startup and perform business logic for function
 * management.
 *
 * @author mcool
 */
@Service
public class BaseStatementFunctionService {

	private final BaseStatementFunctionRepository functionRepository;
	private final PolicyTypeService policyTypeService;
	private final PolicyBuilder policyBuilder;

	public BaseStatementFunctionService(
			@Autowired BaseStatementFunctionRepository functionRepository,
			@Autowired PolicyTypeService policyTypeService) {
		this.functionRepository = functionRepository;
		this.policyTypeService = policyTypeService;
		this.policyBuilder = PolicyBuilder.getInstance();
	}

	/*
	 * Base statement function repository methods
	 */

	/**
	 * Gets a map of base statement function names to base statement function ids. Note that this
	 * is a map of distinct function names to the most relevant function id. Typically, the most
	 * relevant function is the latest released version. If no released versions are available, the
	 * map will contain the id of the latest draft version of the function, and if no draft exists,
	 * it will contain the id of the latest deprecated version of the function.
	 *
	 * @return base statement function name and id map
	 */
	public Map<String, Long> getFunctions() {
		return functionRepository
				.findAllByOrderByNameAscStateDescVersionDesc().stream()
				.collect(Collectors.toMap(BaseStatementFunctionEntity::getName,
						BaseStatementFunctionEntity::getId, (v1, v2) -> v1, TreeMap::new));
	}

	/**
	 * Gets a list of the latest released version of each base statement function. Each string in
	 * the list is the versioned name of the function.
	 *
	 * @return latest released base statement functions
	 */
	public List<String> getLatestFunctions() {
		return functionRepository
				.findAllByStateOrderByNameAscVersionDesc(PolicyElementState.RELEASED).stream()
				.collect(Collectors.toMap(BaseStatementFunctionEntity::getName,
						BaseStatementFunctionEntity::getVersionedName, (v1, v2) -> v1,
						TreeMap::new)).values().stream()
				.collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Gets the base statement function with the given id from the database.
	 *
	 * @param id the id of the base statement function to retrieve
	 * @return the base statement function, or null
	 * @throws PolicyElementNotFoundException if no such function exists
	 */
	public BaseStatementFunctionEntity getFunction(Long id) throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = functionRepository.findById(id).orElse(null);
		if (function == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no base statement function with the id ''{0}''", id));
		}
		return function;
	}

	/**
	 * Gets the base statement function with the given name from the database. First tries to parse
	 * a name and version from the given name, otherwise gets the latest released version with the
	 * given name.
	 *
	 * @param name the name of the base statement function to retrieve
	 * @return the base statement function
	 * @throws PolicyElementNotFoundException if no such function exists
	 */
	public BaseStatementFunctionEntity getFunction(String name)
			throws PolicyElementNotFoundException {
		// Split string on colon and attempt to parse name and version
		String[] components = name.split(":");
		if (components.length == 2) {
			// Try to parse integer for version
			try {
				int version = Integer.parseInt(components[1]);
				return getFunction(components[0], version);
			} catch (NumberFormatException e) {
				// Do nothing
			}
		}
		// Get latest function with the given name
		return getLatestReleasedFunction(name);
	}

	/**
	 * Gets the base statement function with the given name and version from the database.
	 *
	 * @param name    the name of the base statement function to retrieve
	 * @param version the version of the base statement function to retrieve
	 * @return the base statement function
	 * @throws PolicyElementNotFoundException if no such function exists
	 */
	public BaseStatementFunctionEntity getFunction(String name, int version)
			throws PolicyElementNotFoundException {
		Optional<BaseStatementFunctionEntity> function = functionRepository
				.findByNameAndVersion(name, version);
		if (function.isEmpty()) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no base statement function with the name ''{0}'' and version {1}",
							name, version));
		}
		return function.get();
	}

	/**
	 * Gets the latest released version of the base statement function with the given name from the
	 * database.
	 *
	 * @param name the name of the base statement function to retrieve
	 * @return the base statement function
	 * @throws PolicyElementNotFoundException if no such function exists
	 */
	public BaseStatementFunctionEntity getLatestReleasedFunction(String name)
			throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = functionRepository
				.findFirstByNameAndStateOrderByVersionDesc(name, PolicyElementState.RELEASED);
		if (function == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no released base statement function with the name ''{0}''",
							name));
		}
		return function;
	}

	/**
	 * Gets the id of the previous version of the base statement function with the given name and
	 * version.
	 *
	 * @param name    the name of the function to get the previous version of
	 * @param version the version of the function to get the previous version of
	 * @return the id of the previous version, or null if there is no previous version
	 */
	public Long getPreviousVersionId(String name, int version) {
		return functionRepository.findFirstByNameAndVersionLessThanOrderByVersionDesc(name, version)
				.map(BaseStatementFunctionEntity::getId).orElse(null);
	}

	/**
	 * Gets the id of the next version of the base statement function with the given name and
	 * version.
	 *
	 * @param name    the name of the function to get the next version of
	 * @param version the version of the function to get the next version of
	 * @return the id of the next version, or null if there is no next version
	 */
	public Long getNextVersionId(String name, int version) {
		return functionRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc(name, version)
				.map(BaseStatementFunctionEntity::getId).orElse(null);
	}

	/**
	 * Gets a set of updated function dependencies for the given function, if any of the
	 * dependencies are not the latest released version.
	 *
	 * @param function the function to get updated dependencies for
	 * @return set of versioned names of latest dependencies
	 */
	public Set<String> getUpdatedDependencies(BaseStatementFunctionEntity function) {
		return function.getDependencies().stream()
				.map(dependency -> getUpdatedFunction(dependency.getName(),
						dependency.getVersion()))
				.filter(Optional::isPresent).map(Optional::get)
				.map(BaseStatementFunctionEntity::getVersionedName).collect(Collectors.toSet());
	}

	/**
	 * Gets the latest released version of the function with the given name whose version is greater
	 * than the given version.
	 *
	 * @param name    the name of the function to get the updated version of
	 * @param version the minimum (exclusive) version for the updated function
	 * @return the updated function, or {@link Optional#empty()} if no such function exists
	 */
	public Optional<BaseStatementFunctionEntity> getUpdatedFunction(String name, int version) {
		return functionRepository
				.findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc(name, version,
						PolicyElementState.RELEASED);
	}

	/*
	 * Base statement function management methods
	 */

	/**
	 * Creates a new draft revision of a function, incrementing the version and copying all other
	 * fields.
	 *
	 * @param functionId the id of the function to revise
	 * @param author     the user creating the revision
	 * @return the new version of the function
	 * @throws PolicyElementNotFoundException if there is no function with the given id
	 * @throws BaseStatementFunctionException if a draft already exists or if the function is
	 *                                        deprecated
	 */
	public BaseStatementFunctionEntity createRevision(Long functionId, String author)
			throws PolicyElementNotFoundException, BaseStatementFunctionException {
		// Get function with the given id
		BaseStatementFunctionEntity function = getFunction(functionId);
		// Get the latest version of function with same name
		BaseStatementFunctionEntity latestVersion = functionRepository
				.findFirstByNameOrderByVersionDesc(function.getName());
		// Ensure latest version is in released state
		if (latestVersion.getState().equals(PolicyElementState.DRAFT)) {
			throw new BaseStatementFunctionException("A draft of this function already exists");
		} else if (latestVersion.getState().equals(PolicyElementState.DEPRECATED)) {
			throw new BaseStatementFunctionException("A deprecated function cannot be revised");
		}
		// Create a revision and save
		BaseStatementFunctionEntity revision = latestVersion.toRevision();
		revision.setAuthor(author);
		return functionRepository.saveAndFlush(revision);
	}

	/**
	 * Saves the given base statement function to the database, after validating it. Overwrites an
	 * existing base statement function with the same name if the authors match.
	 *
	 * @param functionId  the id of the base statement function to save, if it already exists
	 * @param functionDto the base statement function to save to the database
	 * @return the saved function
	 * @throws BaseStatementFunctionException if the user cannot edit the base statement or if the
	 *                                        base statement is invalid
	 * @throws PolicyElementNotFoundException if no function with the given id exists
	 */
	public BaseStatementFunctionEntity saveFunction(Optional<Long> functionId,
			BaseStatementFunctionDto functionDto)
			throws PolicyElementNotFoundException, BaseStatementFunctionException {
		// Map function to an existing or new entity
		BaseStatementFunctionEntity functionEntity = convertFunctionDtoToEntity(functionId,
				functionDto, false);
		// Ensure the user has permission to save this function
		if (!functionEntity.getAuthor().equalsIgnoreCase(functionDto.getAuthor())) {
			throw new BaseStatementFunctionException(
					"You do not have permission to update this function");
		}
		// Ensure the function is in a draft state
		if (!functionEntity.getState().equals(PolicyElementState.DRAFT)) {
			throw new BaseStatementFunctionException(MessageFormat
					.format("Cannot edit a base statement function in the {0} state",
							functionEntity.getState().getName().toLowerCase()));
		}
		// If this is a new function, make sure the name isn't already used
		if (functionId.isEmpty() && functionRepository.findFirstByNameOrderByVersionDesc(
				functionEntity.getName()) != null) {
			throw new BaseStatementFunctionException(MessageFormat
					.format("A function with the name ''{0}'' already exists",
							functionEntity.getName()));
		}
		// Perform validation on function and save if no errors
		validateFunction(functionEntity.toCore());
		return functionRepository.saveAndFlush(functionEntity);
	}

	/**
	 * Deletes the base statement function with the given id, if it exists.
	 *
	 * @param functionId the id of the base statement function to delete
	 * @param user       the user performing the delete
	 * @return the deleted base statement function entity
	 * @throws PolicyElementNotFoundException if no base statement function with the given id exists
	 * @throws BaseStatementFunctionException if the user is not the author of the function, or if
	 *                                        the function is referenced by a base statement or
	 *                                        other function
	 */
	public BaseStatementFunctionEntity deleteFunction(Long functionId, String user)
			throws PolicyElementNotFoundException, BaseStatementFunctionException {
		BaseStatementFunctionEntity function = getFunction(functionId);
		if (!function.getAuthor().equals(user)) {
			throw new BaseStatementFunctionException(
					"You are not the author of the base statement function");
		}
		if (function.isReferenced()) {
			throw new BaseStatementFunctionException(MessageFormat.format(
					"The base statement function ''{0}'' is referenced by at least one base statement or function",
					function.getName()));
		}
		functionRepository.delete(function);
		functionRepository.flush();
		return function;
	}

	/**
	 * Updates the state of a function. Valid state transitions are draft to released and released
	 * to deprecated. Also ensures that the function is valid before updating the state.
	 *
	 * @param functionId the id of the function to update
	 * @param name       the name of the new state
	 * @throws BaseStatementFunctionException if the state is unknown, if the transition is not
	 *                                        valid, or if the function is not valid
	 * @throws PolicyElementNotFoundException if no function with the given id exists
	 */
	public void updateFunctionState(Long functionId, String name)
			throws BaseStatementFunctionException, PolicyElementNotFoundException {
		PolicyElementState state = PolicyElementState.getStateForName(name);
		if (state == null) {
			throw new BaseStatementFunctionException("Invalid state");
		}
		BaseStatementFunctionEntity function = getFunction(functionId);
		if (function.getState().ordinal() + 1 != state.ordinal()) {
			throw new BaseStatementFunctionException(MessageFormat
					.format("Function cannot move to {0} state from {1}",
							state.getName().toLowerCase(),
							function.getState().getName().toLowerCase()));
		}
		validateFunction(function.toCore());
		function.setState(state);
		functionRepository.saveAndFlush(function);
	}

	/*
	 * Base statement function import methods
	 */

	/**
	 * Validates the given list of {@link BaseStatementFunctionDto} objects and saves each to the
	 * database as a {@link BaseStatementFunctionEntity}. Only saves the functions if every function
	 * is valid. Creates policy types as needed. TODO revisit this logic later
	 *
	 * @param functionDtos the list of function DTOs to validate and save
	 * @throws BaseStatementFunctionException if any of the functions is invalid
	 * @throws PolicyElementNotFoundException if any of the referenced dependencies is undefined
	 *                                        and does not exist in the database
	 */
	public void importBaseStatementFunctions(List<BaseStatementFunctionDto> functionDtos)
			throws BaseStatementFunctionException, PolicyElementNotFoundException {
		// Convert functions to an existing or new entity
		Map<String, BaseStatementFunctionEntity> functionMap = new HashMap<>();
		for (BaseStatementFunctionDto functionDto : functionDtos) {
			BaseStatementFunctionEntity function = convertFunctionDtoToEntity(Optional.empty(),
					functionDto, true);
			if (functionMap.containsKey(function.getName())) {
				throw new BaseStatementFunctionException(MessageFormat
						.format("The base statement function name ''{0}'' is duplicated. Function names must be unique",
								function.getName()));
			}
			functionMap.put(function.getName(), function);
		}
		// Assign dependencies from other imported functions or from the database
		for (BaseStatementFunctionEntity function : functionMap.values()) {
			Set<BaseStatementFunctionEntity> dependencies = new HashSet<>();
			for (BaseStatementFunctionEntity dependency : function.getDependencies()) {
				if (functionMap.containsKey(dependency.getName())) {
					dependencies.add(functionMap.get(dependency.getName()));
				} else {
					dependencies.add(getFunction(dependency.getName()));
				}
			}
			function.setDependencies(dependencies);
		}
		// Perform validation on each function
		for (BaseStatementFunctionEntity function : functionMap.values()) {
			validateFunction(function.toCore());
		}
		// Get or create policy type entities and assign to each function
		functionMap.values().forEach(function -> {
			Set<PolicyTypeEntity> policyTypes = function.getPolicyTypes().stream()
					.map(PolicyTypeEntity::getName).map(policyTypeService::getOrCreatePolicyType)
					.collect(Collectors.toSet());
			function.setPolicyTypes(policyTypes);
		});
		// Save functions
		functionRepository.saveAll(functionMap.values());
		functionRepository.flush();
	}

	/*
	 * Conversion methods to/from core, DTO and Entity objects
	 */

	private BaseStatementFunctionEntity convertFunctionDtoToEntity(Optional<Long> functionId,
			BaseStatementFunctionDto functionDto, boolean imported)
			throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function;
		if (functionId.isPresent()) {
			// Get the entity with the given id
			function = getFunction(functionId.get());
		} else {
			// Create a new function
			function = new BaseStatementFunctionEntity();
			function.setName(functionDto.getName());
			function.setAuthor(functionDto.getAuthor());
			function.setVersion(1);
			if (imported) {
				function.setState(PolicyElementState.RELEASED);
			} else {
				function.setState(PolicyElementState.DRAFT);
			}
		}
		function.setDescription(functionDto.getDescription());
		Set<PolicyTypeEntity> policyTypes = new HashSet<>();
		for (String policyType : functionDto.getPolicyTypes()) {
			policyTypes.add(imported ? new PolicyTypeEntity(policyType)
					: policyTypeService.getPolicyType(policyType));
		}
		function.setPolicyTypes(policyTypes);
		function.setParameters(functionDto.getParameters());
		if (functionDto.getExpression() != null) {
			function.setExpression(functionDto.getExpression().strip());
		}
		Set<BaseStatementFunctionEntity> dependencies = new HashSet<>();
		for (String dependency : functionDto.getDependencies()) {
			dependencies.add(imported ? new BaseStatementFunctionEntity(dependency)
					: getFunction(dependency));
		}
		function.setDependencies(dependencies);
		return function;
	}

	/*
	 * Base statement function validation methods
	 */

	private void validateFunction(BaseStatementFunction function)
			throws BaseStatementFunctionException {
		PolicyBuilderReport report = policyBuilder
				.validate(function, new ConfigurationRuleset(), new LogicRuleset(),
						new SavedPolicyRuleset());
		if (report.hasErrors() || report.hasViolations()) {
			List<String> messages = report.getErrors().stream().map(PolicyBuilderError::getMessage)
					.collect(Collectors.toList());
			messages.addAll(report.getViolations().stream().map(RuleViolation::getMessage)
					.collect(Collectors.toList()));
			throw new BaseStatementFunctionException(
					"Function validation failed: " + String.join(". ", messages));
			// TODO improve this to display errors in correct portion of function
		}
	}
}
