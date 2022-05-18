package com.tracelink.prodsec.blueprint.core.visitor;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import org.junit.Assert;
import org.junit.Test;

public class AbstractPolicyNodeTest {

	@Test
	public void testGetRoot() {
		Policy policy = PolicyMaker.createValidPolicy();
		Assert.assertEquals(policy, policy.getClauses().get(0).getStatements().get(0).getRoot());
	}

}
