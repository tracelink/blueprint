package com.tracelink.prodsec.blueprint.core.rulesets;

import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;

public class PreconfiguredRulesetTest {

	@Test
	public void testAddRule() {
		PreconfiguredRuleset ruleset = new PreconfiguredRuleset("name");
		try {
			ruleset.addRule(BDDMockito.mock(AbstractPolicyRule.class));
			Assert.fail();
		} catch (RuntimeException e) {
			Assert.assertEquals("You may not add additional rules to a built-in ruleset",
					e.getMessage());
		}
	}

}
