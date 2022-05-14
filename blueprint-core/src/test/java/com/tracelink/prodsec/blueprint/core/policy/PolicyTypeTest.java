package com.tracelink.prodsec.blueprint.core.policy;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

public class PolicyTypeTest {

	@Test
	public void testEquals() {
		PolicyType policyType = new PolicyType("foo");
		Assert.assertEquals(policyType, policyType);
		Assert.assertFalse(policyType.equals(new BaseStatementFunction()));
	}

	@Test
	public void testGetName() {
		PolicyType policyType = new PolicyType("foo");
		Assert.assertEquals("foo", policyType.getName());
	}

}
