package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import java.text.MessageFormat;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;

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
	public PolicyReport visit(ConfiguredStatement node, PolicyReport report) {
		PolicyType policyType = node.getRoot().getPolicyType();
		if (!node.getBaseStatement().getPolicyTypes().contains(policyType)) {
			report.addViolation(this, node,
				MessageFormat
					.format("The base statement {0} is not compatible with the policy type {1}",
						node.getBaseStatement().getName(), policyType.getName()));
		}
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.MEDIUM;
	}
}
