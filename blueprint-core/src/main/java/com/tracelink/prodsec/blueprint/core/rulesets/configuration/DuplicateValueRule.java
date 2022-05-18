package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.HashSet;
import java.util.Set;

/**
 * Detects when any two clauses in a policy or any two statements in a clause
 * are duplicated.
 *
 * @author csmith
 */
public class DuplicateValueRule extends AbstractPolicyRule {

	public DuplicateValueRule() {
		super("Duplicate Value Rule");
	}

	@Override
	public PolicyBuilderReport visit(Policy node, PolicyBuilderReport report) {
		checkClauseDuplicates(node, report);
		return super.visit(node, report);
	}

	private void checkClauseDuplicates(Policy node, PolicyBuilderReport report) {
		Set<PolicyClause> clauseSet = new HashSet<>();
		for (PolicyClause clause : node.getClauses()) {
			if (clauseSet.contains(clause)) {
				report.addViolation(this, clause,
						"This clause is a duplicate and should be removed from the policy");
			}
			clauseSet.add(clause);
		}
	}

	@Override
	public PolicyBuilderReport visit(PolicyClause node, PolicyBuilderReport report) {
		checkStatementDuplicates(node, report);
		return super.visit(node, report);
	}

	private void checkStatementDuplicates(PolicyClause node, PolicyBuilderReport report) {
		Set<ConfiguredStatement> baseStatementSet = new HashSet<>();
		for (ConfiguredStatement statement : node.getStatements()) {
			if (baseStatementSet.contains(statement)) {
				report.addViolation(this, statement,
						"This statement is a duplicate and should be removed from the clause");
			}
			baseStatementSet.add(statement);
		}
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.INFO;
	}
}
