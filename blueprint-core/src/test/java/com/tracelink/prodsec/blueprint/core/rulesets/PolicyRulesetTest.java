package com.tracelink.prodsec.blueprint.core.rulesets;

import org.junit.Assert;
import org.junit.Test;

public class PolicyRulesetTest {

	@Test
	public void testGetName() {
		Assert.assertEquals("name", new PolicyRuleset("name").getName());
	}

}
