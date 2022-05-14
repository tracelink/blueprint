package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRuleTest;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.validation.MatchPolicyTypeRule;

import java.util.Set;
import org.junit.Assert;

public class MatchPolicyTypeRuleTest extends AbstractPolicyRuleTest {

	@Override
	protected void prepareTests() {
		Policy typeMismatch = PolicyMaker.createValidPolicy();
		Set<PolicyType> policyTypes = typeMismatch.getClauses().get(0).getStatements().get(0)
			.getBaseStatement().getPolicyTypes();
		PolicyType mismatch = new PolicyType("testingOnly");
		Assert.assertFalse(policyTypes.contains(mismatch));
		typeMismatch.setPolicyType(mismatch);
		addCase("typeMismatch", typeMismatch, 1, 0,
			(r -> Assert
				.assertTrue("typeMismatch: Wrong message " + r.getViolations().get(0).getMessage(),
					r.getViolations().get(0).getMessage()
						.contains("not compatible with the policy type"))));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.MEDIUM;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new MatchPolicyTypeRule();
	}
}
