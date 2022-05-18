package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Model for a single policy clause in a policy.
 *
 * @author mcool
 */
public class PolicyClause extends AbstractPolicyNode {

	private Policy parent;
	private int index;
	@NotEmpty(message = "A policy clause must have at least one configured statement")
	private List<@NotNull(message = "Configured statements cannot be null") @Valid ConfiguredStatement> statements = new ArrayList<>();

	public void setParent(Policy parent) {
		this.parent = parent;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public List<ConfiguredStatement> getStatements() {
		return statements;
	}

	public void setStatements(List<ConfiguredStatement> statements) {
		// Set parent and index
		statements.forEach(statement -> statement.setParent(this));
		IntStream.range(0, statements.size()).forEach(idx -> statements.get(idx).setIndex(idx));
		this.statements = statements;
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
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	public Policy getParent() {
		return parent;
	}

	@Override
	protected String getLocationIdentifier() {
		return "clauses[" + index + "]";
	}

}
