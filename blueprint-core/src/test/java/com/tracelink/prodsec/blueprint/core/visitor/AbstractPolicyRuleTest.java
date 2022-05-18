package com.tracelink.prodsec.blueprint.core.visitor;

import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;
import org.junit.Assert;
import org.junit.Test;

public class AbstractPolicyRuleTest {

	@Test
	public void testGetRuleset() {
		PolicyRuleset ruleset = new PolicyRuleset("Test Ruleset");
		AbstractPolicyRule rule = new AbstractPolicyRule("Test Rule") {
			@Override
			public RuleSeverity getSeverity() {
				return RuleSeverity.INFO;
			}
		};
		rule.setRuleset(ruleset);
		Assert.assertEquals(ruleset, rule.getRuleset());
	}

}
