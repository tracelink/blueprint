package com.tracelink.prodsec.blueprint.core.policy;


import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class PolicyTest {

	@Test
	public void testGetters() {
		Policy policy = new Policy();
		policy.setName("Name");
		policy.setAuthor("User");
		policy.setPolicyType("Foo");
		PolicyClause clause = new PolicyClause();

		policy.setClauses(Collections.singletonList(clause));

		Assert.assertEquals("Name", policy.getName());
		Assert.assertEquals("User", policy.getAuthor());
		Assert.assertEquals("Foo", policy.getPolicyType());
		Assert.assertEquals(1, policy.getClauses().size());
		Assert.assertEquals(clause, policy.getClauses().get(0));
	}

	@Test
	public void testSetClauses() {
		Policy policy = new Policy();
		PolicyClause clause0 = new PolicyClause();
		PolicyClause clause1 = new PolicyClause();
		policy.setClauses(Arrays.asList(clause0, clause1));
		Assert.assertEquals(policy, clause0.getParent());
		Assert.assertEquals(policy, clause1.getParent());
		Assert.assertEquals(0, clause0.getIndex());
		Assert.assertEquals(1, clause1.getIndex());
	}

}
