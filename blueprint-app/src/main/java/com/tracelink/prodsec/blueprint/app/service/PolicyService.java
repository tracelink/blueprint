package com.tracelink.prodsec.blueprint.app.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyImportException;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementDto;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.rules.PolicyError;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.RuleViolation;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.LogicRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.validation.ValidationRuleset;
import com.tracelink.prodsec.blueprint.core.validation.PolicyBuilder;

/**
 * Service to validate policies and export them to Rego. Also allows for the
 * import of policies from JSON to the UI.
 *
 * @author mcool
 */
@Service
public class PolicyService {

	private final BaseStatementService baseStatementService;
	private final PolicyRepository policyRepository;
	private final PolicyBuilder policyBuilder;
	private final ObjectMapper objectMapper;

	public PolicyService(@Autowired BaseStatementService baseStatementService,
			@Autowired PolicyRepository policyRepository) {
		this.baseStatementService = baseStatementService;
		this.policyRepository = policyRepository;
		this.policyBuilder = new PolicyBuilder();
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * Imports the policy as JSON and validates the form. Does not perform
	 * validation on policy content, which happens during export. Returns a
	 * {@link PolicyDto} object that can be rendered by the UI.
	 *
	 * @param policyJson the JSON of the policy to be imported
	 * @return a policy DTO to render in the UI
	 * @throws PolicyImportException if the JSON is not correctly formatted
	 */
	public PolicyDto importPolicy(String policyJson) throws PolicyImportException {
		PolicyDto policy;
		try {
			policy = objectMapper.readValue(policyJson, PolicyDto.class);
		} catch (JsonProcessingException e) {
			throw new PolicyImportException(
					"The imported policy is not formatted correctly: " + e.getMessage());
		}
		if (StringUtils.isBlank(policy.getPolicyType())) {
			throw new PolicyImportException("The imported policy must have a policy type");
		}
		return policy;
	}

	/**
	 * Exports the given policy DTO to a Rego policy, if it passes validation.
	 *
	 * @param policyDto the policy to export to Rego
	 * @return the string containing the Rego policy
	 * @throws PolicyException if the policy, clauses or base statements are invalid
	 */
	public String exportPolicy(PolicyDto policyDto) throws PolicyException {
		Policy policy = validatePolicy(policyDto);
		return policyBuilder.generateRego(policy);
	}

	private Policy validatePolicy(PolicyDto policyDto) throws PolicyException {
		Policy policy = convertPolicyDtoToCore(policyDto);
		PolicyReport report = policyBuilder.validate(policy, new LogicRuleset(false), new ValidationRuleset(false));
		if (report.hasErrors() || report.hasViolations()) {
			List<String> messages = report.getErrors().stream().map(PolicyError::getMessage)
					.collect(Collectors.toList());
			messages.addAll(report.getViolations().stream().map(RuleViolation::getMessage)
					.collect(Collectors.toList()));
			throw new PolicyException("Policy validation failed: " + String.join(", ", messages));
			// TODO improve this to display errors in correct portion of policy
		}
		return policy;
	}

	/*
	 * Conversion methods to/from core, DTO and Entity objects
	 */

	private Policy convertPolicyDtoToCore(PolicyDto policyDto) {
		Policy policy = new Policy();
		PolicyTypeEntity policyType = baseStatementService.getPolicyType(policyDto.getPolicyType());
		if (policyType != null) {
			policy.setPolicyType(policyType.toCore());
		}
		List<PolicyClause> policyClauses = policyDto.getClauses().stream()
				.map(c -> convertClauseDtoToCore(c, policy))
				.collect(Collectors.toList());
		policy.setClauses(policyClauses);
		return policy;
	}

	private PolicyClause convertClauseDtoToCore(PolicyClauseDto policyClauseDto, Policy policy) {
		PolicyClause policyClause = new PolicyClause(policy);
		List<ConfiguredStatement> statements = policyClauseDto.getStatements().stream()
				.map(s -> convertStatementDtoToCore(s, policyClause)).collect(Collectors.toList());
		policyClause.setStatements(statements);
		return policyClause;
	}

	private ConfiguredStatement convertStatementDtoToCore(ConfiguredStatementDto statementDto,
			PolicyClause policyClause) {
		ConfiguredStatement statement = new ConfiguredStatement(policyClause);
		BaseStatementEntity baseStatement = baseStatementService
				.getBaseStatement(statementDto.getBaseStatementName());
		if (baseStatement != null) {
			statement.setBaseStatement(baseStatement.toCore());
			if (baseStatement.isNegationAllowed()) {
				statement.setNegated(statementDto.isNegated());
			} else {
				statement.setNegated(false);
			}
		}
		statement.setArgumentValues(statementDto.getArgumentValues());
		return statement;
	}

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
	 * Gets the policy with the given id, or null.
	 *
	 * @param id the id of the policy to retrieve
	 * @return the policy with the given id
	 */
	public PolicyEntity getPolicy(Long id) {
		return policyRepository.findById(id).orElse(null);
	}

	/**
	 * Gets the policy with the given name, or null.
	 *
	 * @param name the name of the policy to retrieve
	 * @return the policy with the given name
	 */
	public PolicyEntity getPolicy(String name) {
		return policyRepository.findByName(name);
	}

	/**
	 * Saves the given policy in the database, after validating it. Uses the given name and author
	 * for the entity. Overwrites an existing policy with the same name if the authors match.
	 *
	 * @param policyDto the policy to save to the database
	 * @param name      the name of the policy
	 * @param author    the author of the policy
	 * @throws PolicyException if the policy name is blank, if the user cannot edit the policy, or
	 *                         if the policy is invalid
	 */
	public void savePolicy(PolicyDto policyDto, String name, String author)
			throws PolicyException {
		Policy policy = validatePolicy(policyDto);
		if (StringUtils.isBlank(name)) {
			throw new PolicyException("A policy name cannot be blank");
		}
		PolicyEntity policyEntity = policyRepository.findByName(name);
		if (policyEntity != null && !policyEntity.getAuthor().equalsIgnoreCase(author)) {
			throw new PolicyException(
					"A policy with that name already exists and cannot be overwritten");
		}

		if (policyEntity == null) {
			policyEntity = convertCoreToPolicyEntity(policy);
		} else {
			policyEntity
					.setClauses(policy.getClauses().stream().map(this::convertCoreToClauseEntity)
							.collect(Collectors.toList()));
		}
		policyEntity.setName(name);
		policyEntity.setAuthor(author);
		PolicyTypeEntity policyType = baseStatementService.getPolicyType(policyDto.getPolicyType());
		policyEntity.setPolicyType(policyType);
		policyRepository.saveAndFlush(policyEntity);
	}

	/**
	 * Deletes the policy with the given id, if it exists.
	 *
	 * @param policyId the id of the policy to delete
	 * @param user     the user performing the delete
	 * @return the deleted policy entity
	 * @throws PolicyException if the policy id is invalid or if the user is not the author of the
	 *                         policy
	 */
	public PolicyEntity deletePolicy(Long policyId, String user) throws PolicyException {
		PolicyEntity policy = policyRepository.findById(policyId).orElse(null);
		if (policy == null) {
			throw new PolicyException("Invalid policy id");
		}
		if (!policy.getAuthor().equals(user)) {
			throw new PolicyException("You are not the author of the policy");
		}
		policyRepository.delete(policy);
		policyRepository.flush();
		return policy;
	}

	private PolicyEntity convertCoreToPolicyEntity(Policy policy) {
		PolicyEntity policyEntity = new PolicyEntity();
		policyEntity.setClauses(policy.getClauses().stream().map(this::convertCoreToClauseEntity)
				.collect(Collectors.toList()));
		return policyEntity;
	}

	private PolicyClauseEntity convertCoreToClauseEntity(PolicyClause policyClause) {
		PolicyClauseEntity policyClauseEntity = new PolicyClauseEntity();
		policyClauseEntity.setStatements(policyClause.getStatements().stream()
				.map(this::convertCoreToStatementEntity).collect(Collectors.toList()));
		return policyClauseEntity;
	}

	private ConfiguredStatementEntity convertCoreToStatementEntity(ConfiguredStatement statement) {
		BaseStatementEntity baseStatementEntity = baseStatementService
				.getBaseStatement(statement.getBaseStatement().getName());
		ConfiguredStatementEntity statementEntity = new ConfiguredStatementEntity();
		statementEntity.setBaseStatement(baseStatementEntity);
		if (baseStatementEntity.isNegationAllowed()) {
			statementEntity.setNegated(statement.isNegated());
		} else {
			statementEntity.setNegated(false);
		}
		statementEntity.setArgumentValues(statement.getArgumentValues());
		return statementEntity;
	}
}
