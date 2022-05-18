package com.tracelink.prodsec.blueprint.core.report;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import org.junit.Assert;
import org.junit.Test;

public class PolicyBuilderErrorTest {

	@Test
	public void testErrorNode() {
		Policy policy = new Policy();
		PolicyBuilderError error = new PolicyBuilderError(policy, "Error");
		Assert.assertEquals("policy", error.getLocation());
		Assert.assertEquals("Error", error.getMessage());
	}

	@Test
	public void testErrorLocation() {
		PolicyBuilderError error = new PolicyBuilderError("foo", "Error");
		Assert.assertEquals("foo", error.getLocation());
		Assert.assertEquals("Error", error.getMessage());
	}

}
