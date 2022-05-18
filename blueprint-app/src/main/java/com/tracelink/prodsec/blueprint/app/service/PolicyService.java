package com.tracelink.prodsec.blueprint.app.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementDto;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyRepository;
import com.tracelink.prodsec.blueprint.app.rulesets.SavedPolicyRuleset;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.core.PolicyBuilder;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderError;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleViolation;
import com.tracelink.prodsec.blueprint.core.rulesets.configuration.ConfigurationRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.LogicRuleset;

/**
 * Service to validate policies and export them to Rego. Also allows for the
 * import of policies from JSON to the UI.
 *
 * @author mcool
 */
@Service
public class PolicyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyService.class);
	private final BaseStatementService baseStatementService;
	private final PolicyTypeService policyTypeService;
	private final PolicyRepository policyRepository;
	private final PolicyBuilder policyBuilder;
	private final ObjectMapper objectMapper;

	public PolicyService(@Autowired BaseStatementService baseStatementService,
			@Autowired PolicyTypeService policyTypeService,
			@Autowired PolicyRepository policyRepository) {
		this.baseStatementService = baseStatementService;
		this.policyTypeService = policyTypeService;
		this.policyRepository = policyRepository;
		this.policyBuilder = PolicyBuilder.getInstance();
		this.objectMapper = new ObjectMapper();
	}

	/*
	 * Policy repository methods
	 */

	/**
	 * Gets a map of policy names to policy ids.
	 *
	 * @return policy name and id map
	 */
	public Map<String, Long> getPolicies() {
		return policyRepository.findAll().stream().collect(Collectors
				.toMap(PolicyEntity::getName, PolicyEntity::getId, (v1, v2) -> v1, TreeMap::new));
	}

	/**
	 * Gets the policy with the given id.
	 *
	 * @param id the id of the policy to retrieve
	 * @return the policy with the given id
	 * @throws PolicyElementNotFoundException if no such policy exists
	 */
	public PolicyEntity getPolicy(Long id) throws PolicyElementNotFoundException {
		PolicyEntity policy = policyRepository.findById(id).orElse(null);
		if (policy == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no policy with the id ''{0}''", id));
		}
		return policy;
	}

	/**
	 * Gets the policy with the given name, or null.
	 *
	 * @param name the name of the policy to retrieve
	 * @return the policy with the given name
	 * @throws PolicyElementNotFoundException if no such policy exists
	 */
	public PolicyEntity getPolicy(String name) throws PolicyElementNotFoundException {
		PolicyEntity policy = policyRepository.findByName(name);
		if (policy == null) {
			throw new PolicyElementNotFoundException(MessageFormat
					.format("There is no policy with the name ''{0}''", name));
		}
		return policy;
	}

	/*
	 * Policy management methods
	 */

	/**
	 * Saves the given policy in the database, after validating it. Uses the given name and author
	 * for the entity. Overwrites an existing policy with the same name if the authors match.
	 *
	 * @param policyDto the policy to save to the database
	 * @throws PolicyElementNotFoundException if any of the policy types or base statements does
	 *                                        not exist
	 * @throws PolicyException                if the policy name is blank, if the user cannot edit
	 *                                        the policy, or if the policy is invalid
	 */
	public void savePolicy(PolicyDto policyDto)
			throws PolicyElementNotFoundException, PolicyException {
		// Map policy to an existing or new entity
		PolicyEntity policyEntity = convertPolicyDtoToEntity(policyDto);
		// Ensure the user has permission to save this base statement if it already exists
		if (!policyEntity.getAuthor().equalsIgnoreCase(policyDto.getAuthor())) {
			throw new PolicyException(
					"A policy with that name already exists and cannot be overwritten");
		}
		// Perform validation on base statement and save
		validatePolicy(policyEntity.toCore(), true);
		policyRepository.saveAndFlush(policyEntity);
	}

	/**
	 * Deletes the policy with the given id, if it exists.
	 *
	 * @param policyId the id of the policy to delete
	 * @param user     the user performing the delete
	 * @return the deleted policy entity
	 * @throws PolicyElementNotFoundException if no policy with the given id exists
	 * @throws PolicyException                if the policy id is invalid or if the user is not the
	 *                                        author of the
	 *                                        policy
	 */
	public PolicyEntity deletePolicy(Long policyId, String user)
			throws PolicyElementNotFoundException, PolicyException {
		PolicyEntity policy = getPolicy(policyId);
		if (!policy.getAuthor().equals(user)) {
			throw new PolicyException("You are not the author of the policy");
		}
		policyRepository.delete(policy);
		policyRepository.flush();
		return policy;
	}

	/**
	 * Imports the policy as JSON and validates the form. Does not perform
	 * validation on policy content, which happens during export. Returns a
	 * {@link PolicyDto} object that can be rendered by the UI.
	 *
	 * @param policyJson the JSON of the policy to be imported
	 * @return a policy DTO to render in the UI
	 * @throws PolicyException                if the JSON is not correctly formatted
	 * @throws PolicyElementNotFoundException if any of the policy types or base statements does not
	 *                                        exist
	 */
	public PolicyDto importPolicy(String policyJson)
			throws PolicyException, PolicyElementNotFoundException {
		PolicyDto policyDto;
		try {
			policyDto = objectMapper.readValue(policyJson, PolicyDto.class);
		} catch (JsonProcessingException e) {
			LOGGER.info("Policy import failed: {}", e.getMessage());
			throw new PolicyException(
					"The imported policy is not formatted correctly. Check the log for more details");
		}
		PolicyEntity policyEntity = convertPolicyDtoToEntity(policyDto);
		validatePolicy(policyEntity.toCore(), false);
		return policyEntity.toDto();
	}

	/**
	 * Exports the given policy DTO to a Rego policy, if it passes validation.
	 *
	 * @param policyDto the policy to export to Rego
	 * @return the string containing the Rego policy
	 * @throws PolicyException                if the policy, clauses or base statements are invalid
	 * @throws PolicyElementNotFoundException if referenced base statements or policy types do not
	 *                                        exist
	 */
	public String exportPolicy(PolicyDto policyDto)
			throws PolicyException, PolicyElementNotFoundException {
		PolicyEntity policyEntity = convertPolicyDtoToEntity(policyDto);
		Policy policy = policyEntity.toCore();
		validatePolicy(policy, false);
		return policyBuilder.generateRego(policy);
	}

	/**
	 * Gets a set of updated base statements for the given policy, if any of the
	 * referenced base statement are not the latest released version.
	 *
	 * @param policy the policy to get updated base statements for
	 * @return set of versioned names of latest base statements
	 */
	public Set<String> getUpdatedBaseStatements(PolicyEntity policy) {
		return policy.getClauses().stream().map(PolicyClauseEntity::getStatements)
				.flatMap(List::stream).map(ConfiguredStatementEntity::getBaseStatement)
				.map(baseStatement -> baseStatementService
						.getUpdatedBaseStatement(baseStatement.getName(),
								baseStatement.getVersion()))
				.filter(Optional::isPresent).map(Optional::get)
				.map(BaseStatementEntity::getVersionedName).collect(Collectors.toSet());
	}

	/*
	 * Conversion methods to/from core, DTO and Entity objects
	 */

	private PolicyEntity convertPolicyDtoToEntity(PolicyDto policyDto)
			throws PolicyElementNotFoundException {
		PolicyEntity policyEntity = policyRepository.findByName(policyDto.getName());
		if (policyEntity == null) {
			policyEntity = new PolicyEntity();
			policyEntity.setName(policyDto.getName());
			policyEntity.setAuthor(policyDto.getAuthor());
		}
		PolicyTypeEntity policyType = policyTypeService.getPolicyType(policyDto.getPolicyType());
		policyEntity.setPolicyType(policyType);
		List<PolicyClauseEntity> clauses = new ArrayList<>();
		for (PolicyClauseDto clause : policyDto.getClauses()) {
			clauses.add(convertClauseDtoToEntity(clause));
		}
		policyEntity.setClauses(clauses);
		return policyEntity;
	}

	private PolicyClauseEntity convertClauseDtoToEntity(PolicyClauseDto clauseDto)
			throws PolicyElementNotFoundException {
		PolicyClauseEntity clause = new PolicyClauseEntity();
		List<ConfiguredStatementEntity> statements = new ArrayList<>();
		for (ConfiguredStatementDto statement : clauseDto.getStatements()) {
			statements.add(convertStatementDtoToEntity(statement));
		}
		clause.setStatements(statements);
		return clause;
	}

	private ConfiguredStatementEntity convertStatementDtoToEntity(
			ConfiguredStatementDto statementDto) throws PolicyElementNotFoundException {
		ConfiguredStatementEntity statement = new ConfiguredStatementEntity();
		BaseStatementEntity baseStatement = baseStatementService
				.getBaseStatement(statementDto.getBaseStatementName());
		statement.setBaseStatement(baseStatement);
		if (baseStatement.isNegationAllowed()) {
			statement.setNegated(statementDto.isNegated());
		} else {
			statement.setNegated(false);
		}
		statement.setArgumentValues(statementDto.getArgumentValues());
		return statement;
	}

	/*
	 * Policy validation methods
	 */

	private void validatePolicy(Policy policy, boolean save) throws PolicyException {
		PolicyBuilderReport report;
		if (save) {
			report = policyBuilder.validate(policy, new ConfigurationRuleset(), new LogicRuleset(),
					new SavedPolicyRuleset());
		} else {
			report = policyBuilder.validate(policy, new ConfigurationRuleset(), new LogicRuleset());
		}
		if (report.hasErrors() || report.hasViolations()) {
			List<String> messages = report.getErrors().stream().map(PolicyBuilderError::getMessage)
					.collect(Collectors.toList());
			messages.addAll(report.getViolations().stream().map(RuleViolation::getMessage)
					.collect(Collectors.toList()));
			throw new PolicyException("Policy validation failed: " + String.join(", ", messages));
			// TODO improve this to display errors in correct portion of policy
		}
	}
}
