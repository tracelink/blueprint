package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.List;
import java.util.Objects;

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
	public PolicyBuilderReport visit(PolicyClause node, PolicyBuilderReport report) {
		List<ConfiguredStatement> statements = node.getStatements();
		for (int i = 0; i < statements.size(); i++) {
			for (int j = i + 1; j < statements.size(); j++) {
				if (isComplement(statements.get(i), statements.get(j))) {
					report.addViolation(this, node,
							"This clause is unsatisfiable. Two of the statements are equivalent and one of them is negated");
					return super.visit(node, report);
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
		return RuleSeverity.ERROR;
	}

}
