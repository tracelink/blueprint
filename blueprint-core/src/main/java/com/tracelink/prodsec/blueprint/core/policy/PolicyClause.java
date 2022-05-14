package com.tracelink.prodsec.blueprint.core.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyVisitor;

/**
 * Model for a single policy clause in a policy.
 *
 * @author mcool
 */
public class PolicyClause extends AbstractPolicyNode {

	@NotEmpty(message = "A policy clause must have at least one configured statement")
	private List<@NotNull(message = "Configured statements cannot be null") @Valid ConfiguredStatement> statements = new ArrayList<>();
	private final Policy policy;

	private int index;

	public PolicyClause(Policy parent) {
		this.policy = parent;
	}

	public List<ConfiguredStatement> getStatements() {
		return statements;
	}

	public void setStatements(List<ConfiguredStatement> statements) {
		IntStream.range(0, statements.size())
			.forEach(stmtIndex -> statements.get(stmtIndex).setIndex(stmtIndex));
		this.statements = statements;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PolicyClause that = (PolicyClause) o;
		return Objects.equals(statements, that.statements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(statements);
	}

	@Override
	public Iterable<ConfiguredStatement> children() {
		return statements;
	}

	@Override
	public PolicyReport accept(PolicyVisitor visitor, PolicyReport report) {
		return visitor.visit(this, report);
	}

	@Override
	public Policy getParent() {
		return policy;
	}

	@Override
	protected String getLocationIdentifier() {
		return "clauses[" + index + "]";
	}

}
