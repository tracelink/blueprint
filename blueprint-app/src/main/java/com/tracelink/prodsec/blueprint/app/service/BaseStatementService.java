package com.tracelink.prodsec.blueprint.app.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementRepository;
import com.tracelink.prodsec.blueprint.app.rulesets.SavedPolicyRuleset;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.PolicyBuilder;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderError;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleViolation;
import com.tracelink.prodsec.blueprint.core.rulesets.configuration.ConfigurationRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.LogicRuleset;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Service to import base statements on application startup and perform business logic for base
 * statement management.
 *
 * @author mcool
 */
@Service
public class BaseStatementService {

	private final BaseStatementRepository baseStatementRepository;
	private final PolicyTypeService policyTypeService;
	private final BaseStatementFunctionService functionService;
	private final PolicyBuilder policyBuilder;

	public BaseStatementService(@Autowired BaseStatementRepository baseStatementRepository,
			@Autowired PolicyTypeService policyTypeService,
			@Autowired BaseStatementFunctionService functionService) {
		this.baseStatementRepository = baseStatementRepository;
		this.policyTypeService = policyTypeService;
		this.functionService = functionService;
		this.policyBuilder = PolicyBuilder.getInstance();
	}

	/*
	 * Base statement repository methods
	 */

	/**
	 * Gets a map of all base statement versioned names to base statement DTOs.
	 *
	 * @return base statement name and DTO map
	 */
	public Map<String, BaseStatementDto> getAllBaseStatements() {
		return baseStatementRepository
				.findAllByOrderByNameAscStateDescVersionDesc().stream()
				.collect(Collectors.toMap(BaseStatementEntity::getVersionedName,
						BaseStatementEntity::toDto));
	}

	/**
	 * Gets a map of base statement names to base statement ids. Note that this is a map of
	 * distinct base statement names to the most relevant base statement id. Typically, the most
	 * relevant base statement is the latest released version. If no released versions are
	 * available, the map will contain the id of the latest draft version of the base statement,
	 * and if no draft exists, it will contain the id of the latest deprecated version of the base
	 * statement.
	 *
	 * @return base statement name and id map
	 */
	public Map<String, Long> getBaseStatements() {
		return baseStatementRepository
				.findAllByOrderByNameAscStateDescVersionDesc().stream()
				.collect(Collectors.toMap(BaseStatementEntity::getName,
						BaseStatementEntity::getId, (v1, v2) -> v1, TreeMap::new));
	}

	/**
	 * Gets latest released base statements that are associated with the given policy type.
	 *
	 * @param policyType the policy type to retrieve base statements for
	 * @return a list of associated base statements
	 */
	public Map<String, BaseStatementDto> getLatestBaseStatementsForPolicyType(
			PolicyTypeEntity policyType) {
		List<BaseStatementEntity> baseStatements = baseStatementRepository
				.findAllByStateAndPolicyTypesInOrderByNameAscVersionDesc(
						PolicyElementState.RELEASED, Collections.singleton(policyType));
		return baseStatements.stream().collect(Collectors
				.toMap(BaseStatementEntity::getName, BaseStatementEntity::toDto,
						(s1, s2) -> s1, TreeMap::new));
	}

	/**
	 * Gets the base statement with the given id from the database.
	 *
	 * @param id the id of the base statement to retrieve
	 * @return the base statement, or null
	 * @throws PolicyElementNotFoundException if no such base statement exists
	 */
	public BaseStatementEntity getBaseStatement(Long id) throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = baseStatementRepository.findById(id).orElse(null);
		if (baseStatement == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no base statement with the id ''{0}''", id));
		}
		return baseStatement;
	}

	/**
	 * Gets the base statement with the given name from the database. First tries to parse
	 * a name and version from the given name, otherwise gets the latest released version with the
	 * given name.
	 *
	 * @param name the name of the base statement to retrieve
	 * @return the base statement
	 * @throws PolicyElementNotFoundException if no such base statement exists
	 */
	public BaseStatementEntity getBaseStatement(String name)
			throws PolicyElementNotFoundException {
		// Split string on colon and attempt to parse name and version
		String[] components = name.split(":");
		if (components.length == 2) {
			// Try to parse integer for version
			try {
				int version = Integer.parseInt(components[1]);
				return getBaseStatement(components[0], version);
			} catch (NumberFormatException e) {
				// Do nothing
			}
		}
		// Get latest base statement with the given name
		return getLatestReleasedBaseStatement(name);
	}

	/**
	 * Gets the base statement with the given name and version from the database.
	 *
	 * @param name    the name of the base statement to retrieve
	 * @param version the version of the base statement to retrieve
	 * @return the base statement
	 * @throws PolicyElementNotFoundException if no such base statement exists
	 */
	public BaseStatementEntity getBaseStatement(String name, int version)
			throws PolicyElementNotFoundException {
		Optional<BaseStatementEntity> baseStatement = baseStatementRepository
				.findByNameAndVersion(name, version);
		if (baseStatement.isEmpty()) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no base statement with the name ''{0}'' and version {1}",
							name, version));
		}
		return baseStatement.get();
	}

	/**
	 * Gets the latest released version of the base statement with the given name from the
	 * database.
	 *
	 * @param name the name of the base statement to retrieve
	 * @return the base statement
	 * @throws PolicyElementNotFoundException if no such base statement exists
	 */
	public BaseStatementEntity getLatestReleasedBaseStatement(String name)
			throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = baseStatementRepository
				.findFirstByNameAndStateOrderByVersionDesc(name, PolicyElementState.RELEASED);
		if (baseStatement == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no released base statement with the name ''{0}''",
							name));
		}
		return baseStatement;
	}

	/**
	 * Gets the id of the previous version of the base statement with the given name and version.
	 *
	 * @param name    the name of the base statement to get the previous version of
	 * @param version the version of the base statement to get the previous version of
	 * @return the id of the previous version, or null if there is no previous version
	 */
	public Long getPreviousVersionId(String name, int version) {
		return baseStatementRepository
				.findFirstByNameAndVersionLessThanOrderByVersionDesc(name, version)
				.map(BaseStatementEntity::getId).orElse(null);
	}

	/**
	 * Gets the id of the next version of the base statement with the given name and version.
	 *
	 * @param name    the name of the base statement to get the next version of
	 * @param version the version of the base statement to get the next version of
	 * @return the id of the next version, or null if there is no next version
	 */
	public Long getNextVersionId(String name, int version) {
		return baseStatementRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc(name, version)
				.map(BaseStatementEntity::getId).orElse(null);
	}

	/**
	 * Gets the latest released version of the base statement with the given name whose version is
	 * greater than the given version.
	 *
	 * @param name    the name of the base statement to get the updated version of
	 * @param version the minimum (exclusive) version for the updated base statement
	 * @return the updated base statement, or {@link Optional#empty()} if no such base statement
	 * exists
	 */
	public Optional<BaseStatementEntity> getUpdatedBaseStatement(String name, int version) {
		return baseStatementRepository
				.findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc(name, version,
						PolicyElementState.RELEASED);
	}

	/*
	 * Base statement management methods
	 */

	/**
	 * Creates a new draft revision of a base statement, incrementing the version and copying all
	 * other
	 * fields.
	 *
	 * @param baseStatementId the id of the base statement to revise
	 * @param author          the user creating the revision
	 * @return the new version of the base statement
	 * @throws PolicyElementNotFoundException if there is no base statement with the given id
	 * @throws BaseStatementException         if a draft already exists or if the base statement is
	 *                                        deprecated
	 */
	public BaseStatementEntity createRevision(Long baseStatementId, String author)
			throws PolicyElementNotFoundException, BaseStatementException {
		// Get base statement with the given id
		BaseStatementEntity baseStatement = getBaseStatement(baseStatementId);
		// Get the latest version of base statement with same name
		BaseStatementEntity latestVersion = baseStatementRepository
				.findFirstByNameOrderByVersionDesc(baseStatement.getName());
		// Ensure latest version is in released state
		if (latestVersion.getState().equals(PolicyElementState.DRAFT)) {
			throw new BaseStatementException("A draft of this base statement already exists");
		} else if (latestVersion.getState().equals(PolicyElementState.DEPRECATED)) {
			throw new BaseStatementException("A deprecated base statement cannot be revised");
		}
		// Create a revision and save
		BaseStatementEntity revision = latestVersion.toRevision();
		revision.setAuthor(author);
		return baseStatementRepository.saveAndFlush(revision);
	}

	/**
	 * Saves the given base statement to the database, after validating it. Overwrites an
	 * existing base statement with the same name if the authors match.
	 *
	 * @param baseStatementId  the id of the base statement to save, if it already exists
	 * @param baseStatementDto the base statement to save to the database
	 * @return the saved base statement
	 * @throws BaseStatementException         if the user cannot edit the base statement or if the
	 *                                        base statement is invalid
	 * @throws PolicyElementNotFoundException if no base statement with the given id exists
	 */
	public BaseStatementEntity saveBaseStatement(Optional<Long> baseStatementId,
			BaseStatementDto baseStatementDto)
			throws PolicyElementNotFoundException, BaseStatementException {
		// Map base statement to an existing or new entity
		BaseStatementEntity baseStatementEntity = convertBaseStatementDtoToEntity(baseStatementId,
				baseStatementDto, false);
		// Ensure the user has permission to save this base statement
		if (!baseStatementEntity.getAuthor().equalsIgnoreCase(baseStatementDto.getAuthor())) {
			throw new BaseStatementException(
					"You do not have permission to update this base statement");
		}
		// Ensure the base statement is in a draft state
		if (!baseStatementEntity.getState().equals(PolicyElementState.DRAFT)) {
			throw new BaseStatementException(MessageFormat
					.format("Cannot edit a base statement in the {0} state",
							baseStatementEntity.getState().getName().toLowerCase()));
		}
		// If this is a new base statement, make sure the name isn't already used
		if (baseStatementId.isEmpty() && baseStatementRepository.findFirstByNameOrderByVersionDesc(
				baseStatementEntity.getName()) != null) {
			throw new BaseStatementException(MessageFormat
					.format("A base statement with the name ''{0}'' already exists",
							baseStatementEntity.getName()));
		}
		// Perform validation on base statement and save if no errors
		validateBaseStatement(baseStatementEntity.toCore());
		return baseStatementRepository.saveAndFlush(baseStatementEntity);
	}

	/**
	 * Deletes the base statement with the given id, if it exists.
	 *
	 * @param baseStatementId the id of the base statement to delete
	 * @param user            the user performing the delete
	 * @return the deleted base statement entity
	 * @throws PolicyElementNotFoundException if no base statement with the given id exists
	 * @throws BaseStatementException         if the base statement id is invalid, if the
	 *                                        user is not the author of the base statement, or if
	 *                                        the
	 *                                        base statement is referenced by a policy
	 */
	public BaseStatementEntity deleteBaseStatement(Long baseStatementId, String user)
			throws PolicyElementNotFoundException, BaseStatementException {
		BaseStatementEntity baseStatement = getBaseStatement(baseStatementId);
		if (!baseStatement.getAuthor().equals(user)) {
			throw new BaseStatementException(
					"You are not the author of the base statement");
		}
		if (baseStatement.isReferenced()) {
			throw new BaseStatementException(MessageFormat.format(
					"The base statement ''{0}'' is referenced by at least one policy",
					baseStatement.getName()));
		}
		baseStatementRepository.delete(baseStatement);
		baseStatementRepository.flush();
		return baseStatement;
	}

	/**
	 * Updates the state of a base statement. Valid state transitions are draft to released and
	 * released to deprecated. Also ensures that the base statement is valid before updating the
	 * state.
	 *
	 * @param baseStatementId the id of the base statement to update
	 * @param name            the name of the new state
	 * @throws BaseStatementException         if the state is unknown, if the transition is not
	 *                                        valid, or if the base statement is not valid
	 * @throws PolicyElementNotFoundException if no base statement with the given id exists
	 */
	public void updateBaseStatementState(Long baseStatementId, String name)
			throws BaseStatementException, PolicyElementNotFoundException {
		PolicyElementState state = PolicyElementState.getStateForName(name);
		if (state == null) {
			throw new BaseStatementException("Invalid state");
		}
		BaseStatementEntity baseStatement = getBaseStatement(baseStatementId);
		if (baseStatement.getState().ordinal() + 1 != state.ordinal()) {
			throw new BaseStatementException(MessageFormat
					.format("Base statement cannot move to {0} state from {1}",
							state.getName().toLowerCase(),
							baseStatement.getState().getName().toLowerCase()));
		}
		validateBaseStatement(baseStatement.toCore());
		baseStatement.setState(state);
		baseStatementRepository.saveAndFlush(baseStatement);
	}

	/**
	 * Gets base statement arguments for the given function name and compares them to the current
	 * arguments of the base statement with the given id, if provided.
	 *
	 * @param baseStatementId the id of the base statement to map arguments against
	 * @param functionName    the name of the function to get arguments for
	 * @return the list of base statement arguments for the given function
	 * @throws PolicyElementNotFoundException if the function or base statement does not exist
	 */
	public List<BaseStatementArgumentDto> getArgumentsForBaseStatement(
			Optional<Long> baseStatementId, String functionName)
			throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = functionService.getFunction(functionName);
		List<BaseStatementArgumentDto> arguments = new ArrayList<>();
		if (baseStatementId.isPresent()) {
			// Try to map existing arguments to new function
			BaseStatementEntity baseStatement = getBaseStatement(baseStatementId.get());
			IntStream.range(0, function.getParameters().size()).forEach(i -> {
				String parameter = function.getParameters().get(i);
				if (i < baseStatement.getArguments().size() && baseStatement.getArguments().get(i)
						.getParameter().equals(parameter)) {
					// Get the argument at the same index if it has the same name as the parameter
					arguments.add(baseStatement.getArguments().get(i).toDto());
				} else if (baseStatement.getArguments().stream()
						.anyMatch(arg -> arg.getParameter().equals(parameter))) {
					// Get any argument with the same name as the parameter
					arguments.add(baseStatement.getArguments().stream()
							.filter(arg -> arg.getParameter().equals(parameter)).findFirst()
							.orElse(new BaseStatementArgumentEntity()).toDto());
				} else {
					// Create a new argument with the parameter name
					BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
					argument.setParameter(parameter);
					arguments.add(argument);
				}
			});
		} else {
			// Create new arguments for each parameter
			function.getParameters().forEach(parameter -> {
				BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
				argument.setParameter(parameter);
				arguments.add(argument);
			});
		}
		return arguments;
	}

	/*
	 * Base statement import methods
	 */

	/**
	 * Validates the given lists of {@link BaseStatementDto} objects and saves each to the database
	 * as a {@link BaseStatementEntity}. If any of the base statements or arguments are invalid,
	 * none of the base statements will be imported. TODO revisit this logic later
	 *
	 * @param baseStatementDtos the list of base statement DTOs to validate and save
	 * @throws BaseStatementException         if any of the base statements or arguments is invalid
	 * @throws PolicyElementNotFoundException if any of the base statements references a policy
	 *                                        element that does not exist
	 */
	public void importBaseStatements(List<BaseStatementDto> baseStatementDtos)
			throws BaseStatementException, PolicyElementNotFoundException {
		// Convert base statements to an existing or new entity
		Map<String, BaseStatementEntity> baseStatementMap = new HashMap<>();
		for (BaseStatementDto baseStatementDto : baseStatementDtos) {
			BaseStatementEntity baseStatement = convertBaseStatementDtoToEntity(Optional.empty(),
					baseStatementDto, true);
			if (baseStatementMap.containsKey(baseStatement.getName())) {
				throw new BaseStatementException(MessageFormat
						.format("The base statement name ''{0}'' is duplicated. Base statement names must be unique",
								baseStatement.getName()));
			}
			baseStatementMap.put(baseStatement.getName(), baseStatement);
		}
		// Perform validation on each base statement
		for (BaseStatementEntity baseStatement : baseStatementMap.values()) {
			validateBaseStatement(baseStatement.toCore());
		}
		// Save base statements
		baseStatementRepository.saveAll(baseStatementMap.values());
		baseStatementRepository.flush();
	}

	/*
	 * Conversion methods to/from core, DTO and Entity objects
	 */

	private BaseStatementEntity convertBaseStatementDtoToEntity(Optional<Long> baseStatementId,
			BaseStatementDto baseStatementDto, boolean imported)
			throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement;
		if (baseStatementId.isPresent()) {
			// Get the entity with the given id
			baseStatement = getBaseStatement(baseStatementId.get());
		} else {
			// Create a new base statement
			baseStatement = new BaseStatementEntity();
			baseStatement.setName(baseStatementDto.getName());
			baseStatement.setAuthor(baseStatementDto.getAuthor());
			baseStatement.setVersion(1);
			if (imported) {
				baseStatement.setState(PolicyElementState.RELEASED);
			} else {
				baseStatement.setState(PolicyElementState.DRAFT);
			}
		}
		baseStatement.setDescription(baseStatementDto.getDescription());
		Set<PolicyTypeEntity> policyTypes = new HashSet<>();
		for (String policyType : baseStatementDto.getPolicyTypes()) {
			policyTypes.add(policyTypeService.getPolicyType(policyType));
		}
		baseStatement.setPolicyTypes(policyTypes);
		baseStatement.setNegationAllowed(baseStatementDto.isNegationAllowed());
		BaseStatementFunctionEntity function = functionService
				.getFunction(baseStatementDto.getFunction());
		baseStatement.setFunction(function);
		List<BaseStatementArgumentEntity> arguments = baseStatementDto.getArguments().stream()
				.map(this::convertArgumentDtoToEntity)
				.collect(Collectors.toList());
		baseStatement.setArguments(arguments);
		return baseStatement;
	}

	private BaseStatementArgumentEntity convertArgumentDtoToEntity(
			BaseStatementArgumentDto argumentDto) {
		BaseStatementArgumentEntity argument = new BaseStatementArgumentEntity();
		argument.setParameter(argumentDto.getParameter());
		argument.setDescription(argumentDto.getDescription());
		argument.setType(argumentDto.getType());
		argument.setEnumValues(argumentDto.getEnumValues());
		if (argumentDto.getType() != null && argumentDto.getType().isArrayType()) {
			argument.setArrayUnordered(argumentDto.isArrayUnordered());
			argument.setArrayUnique(argumentDto.isArrayUnique());
		}
		return argument;
	}

	/*
	 * Base statement validation methods
	 */

	private void validateBaseStatement(BaseStatement baseStatement)
			throws BaseStatementException {
		PolicyBuilderReport report = policyBuilder
				.validate(baseStatement, new ConfigurationRuleset(), new LogicRuleset(),
						new SavedPolicyRuleset());
		if (report.hasErrors() || report.hasViolations()) {
			List<String> messages = report.getErrors().stream().map(PolicyBuilderError::getMessage)
					.collect(Collectors.toList());
			messages.addAll(report.getViolations().stream().map(RuleViolation::getMessage)
					.collect(Collectors.toList()));
			throw new BaseStatementException(
					"Base statement validation failed: " + String.join(", ", messages));
			// TODO improve this to display errors in correct portion of base statement
		}
	}
}
