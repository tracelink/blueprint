package com.tracelink.prodsec.blueprint.app.rulesets;

import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;
import org.junit.Assert;
import org.junit.Test;

public class SavedPolicyRulesetTest {

	@Test
	public void testConstructor() {
		PolicyRuleset ruleset = new SavedPolicyRuleset();
		Assert.assertEquals("Saved Policy Ruleset", ruleset.getName());
		Assert.assertEquals(1, ruleset.getRules().size());
		Assert.assertTrue(ruleset.getRules().get(0) instanceof SavedPolicyRule);
	}
}
