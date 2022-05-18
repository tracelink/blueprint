package com.tracelink.prodsec.blueprint.core.report;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;

public class RuleViolationTest {

	@Test
	public void testGetters() {
		Policy policy = new Policy();
		RuleViolation ruleViolation = new RuleViolation(policy, "policy", BDDMockito.mock(
				AbstractPolicyRule.class), "message");
		Assert.assertEquals(policy, ruleViolation.getNode());
		Assert.assertEquals("policy", ruleViolation.getLocation());
	}

}
