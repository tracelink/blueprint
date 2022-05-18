package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Model for a policy.
 *
 * @author mcool
 */
public class Policy extends AbstractRootNode {

	private String name;
	private String author;
	@NotBlank(message = "A policy must have a policy type")
	private String policyType;
	@NotEmpty(message = "A policy must have at least one clause")
	private List<@NotNull(message = "Policy clauses cannot be null") @Valid PolicyClause> clauses = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPolicyType() {
		return this.policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

	public List<PolicyClause> getClauses() {
		return clauses;
	}

	public void setClauses(List<PolicyClause> clauses) {
		// Set parent and index
		clauses.forEach(clause -> clause.setParent(this));
		IntStream.range(0, clauses.size()).forEach(idx -> clauses.get(idx).setIndex(idx));
		this.clauses = clauses;
	}

	public Set<BaseStatementFunction> getAllDependentFunctions() {
		return clauses.stream()
				.flatMap(clause -> clause.getStatements().stream())
				.map(ConfiguredStatement::getAllDependentFunctions)
				.flatMap(Set::stream).collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public Iterable<PolicyClause> children() {
		return clauses;
	}

	@Override
	public AbstractPolicyNode getParent() {
		return null;
	}

	@Override
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	protected String getLocationIdentifier() {
		return "policy";
	}

}
