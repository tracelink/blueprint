package com.tracelink.prodsec.blueprint.core.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyVisitor;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

/**
 * Model for a policy.
 *
 * @author mcool
 */
public class Policy extends AbstractPolicyNode {

	@NotNull(message = "A policy must have a type")
	private PolicyType policyType;
	@NotEmpty(message = "A policy must have at least one clause")
	private List<@NotNull(message = "Policy clauses cannot be null") @Valid PolicyClause> clauses = new ArrayList<>();

	public PolicyType getPolicyType() {
		return this.policyType;
	}

	public void setPolicyType(PolicyType policyType) {
		this.policyType = policyType;
	}

	public List<PolicyClause> getClauses() {
		return clauses;
	}

	public void setClauses(List<PolicyClause> clauses) {
		IntStream.range(0, clauses.size()).forEach(idx -> clauses.get(idx).setIndex(idx));
		this.clauses = clauses;
	}

	public Set<BaseStatementFunction> getAllDependentFunctions() {
		return clauses.stream()
			.flatMap(clause -> clause.getStatements().stream())
			.map(ConfiguredStatement::getAllDependentFunctions)
			.flatMap(Set::stream).collect(Collectors.toSet());
	}

	@Override
	public Iterable<PolicyClause> children() {
		return clauses;
	}

	@Override
	public PolicyReport accept(PolicyVisitor visitor, PolicyReport report) {
		return visitor.visit(this, report);
	}

	@Override
	public AbstractPolicyNode getParent() {
		return null;
	}

	@Override
	public Policy getRoot() {
		return this;
	}

	@Override
	protected String getLocationIdentifier() {
		return "policy";
	}

}
