package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;

/**
 * Determines whether {@linkplain ConfiguredStatement}s are complements of each
 * other. Complements have equivalent base statements and equivalent arguments,
 * but the opposite value for {@code negated}.
 */
public class SatisfiabilityRule extends AbstractPolicyRule {

	public SatisfiabilityRule() {
		super("Satisfiability Rule");
	}

	@Override
	public PolicyReport visit(PolicyClause node, PolicyReport report) {
		List<ConfiguredStatement> statements = node.getStatements();
		for (int i = 0; i < statements.size(); i++) {
			for (int j = i + 1; j < statements.size(); j++) {
				if (isComplement(statements.get(i), statements.get(j))) {
					report.addViolation(this, node, MessageFormat.format(
						"This clause is unsatisfiable. It contains both a statement {0} and its complement {1}",
						statements.get(i).getLocation(), statements.get(j).getLocation()));
				}
			}
		}
		return super.visit(node, report);
	}

	private boolean isComplement(ConfiguredStatement first, ConfiguredStatement second) {
		return first.isNegated() != second.isNegated()
			&& Objects.equals(first.getBaseStatement(), second.getBaseStatement())
			&& first.areArgumentsEqual(second);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.HIGH;
	}

}
