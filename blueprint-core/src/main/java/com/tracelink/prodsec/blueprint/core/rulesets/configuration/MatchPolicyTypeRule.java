package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.text.MessageFormat;
import java.util.Set;

/**
 * Detects any cases where a policy uses a configured statement for an
 * incompatible policy type
 *
 * @author csmith
 */
public class MatchPolicyTypeRule extends AbstractPolicyRule {

	public MatchPolicyTypeRule() {
		super("Statements Match Policy Type Rule");
	}

	@Override
	public PolicyBuilderReport visit(ConfiguredStatement node, PolicyBuilderReport report) {
		String policyType = node.getParent().getParent().getPolicyType();
		if (!node.getBaseStatement().getPolicyTypes().contains(policyType)) {
			report.addViolation(this, node,
					MessageFormat
							.format("The base statement ''{0}'' is not compatible with the policy type ''{1}''",
									node.getBaseStatement().getName(), policyType));
		}
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		Set<String> policyTypes = node.getPolicyTypes();
		if (!node.getFunction().getPolicyTypes().containsAll(policyTypes)) {
			report.addViolation(this, node, MessageFormat
					.format("The function ''{0}'' is not compatible with the policy types ''{1}''",
							node.getFunction().getName(), String.join("', '", policyTypes)));
		}
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		Set<String> policyTypes = node.getPolicyTypes();
		node.getDependencies().forEach(dependency -> {
			if (!dependency.getPolicyTypes().containsAll(policyTypes)) {
				report.addViolation(this, node, MessageFormat
						.format("The dependency ''{0}'' is not compatible with the policy types ''{1}''",
								dependency.getName(), String.join("', '", policyTypes)));
			}
		});
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.WARN;
	}
}
