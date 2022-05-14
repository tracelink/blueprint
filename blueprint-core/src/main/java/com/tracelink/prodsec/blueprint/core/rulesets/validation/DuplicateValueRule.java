package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;

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
	public PolicyReport visit(Policy node, PolicyReport report) {
		checkClauseDuplicates(node, report);
		return super.visit(node, report);
	}

	private void checkClauseDuplicates(Policy node, PolicyReport report) {
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
	public PolicyReport visit(PolicyClause node, PolicyReport report) {
		checkStatementDuplicates(node, report);
		return super.visit(node, report);
	}

	private void checkStatementDuplicates(PolicyClause node, PolicyReport report) {
		Set<ConfiguredStatement> baseStatementSet = new HashSet<>();
		for (ConfiguredStatement statement : node.getStatements()) {
			if (baseStatementSet.contains(statement)) {
				report.addViolation(this, statement,
					MessageFormat.format(
						"This statement {0} is a duplicate and should be removed from the clause",
						statement.getLocation()));
			}
			baseStatementSet.add(statement);
		}
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.INFO;
	}
}
