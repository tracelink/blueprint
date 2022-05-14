package com.tracelink.prodsec.blueprint.core.policy;


import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;

public class PolicyTest {

	@Test
	public void testGetters() {
		Policy policy = new Policy();
		policy.setPolicyType(new PolicyType("User"));
		PolicyClause clause = new PolicyClause(policy);

		policy.setClauses(Collections.singletonList(clause));

		Assert.assertEquals(new PolicyType("User"), policy.getPolicyType());
		Assert.assertEquals(1, policy.getClauses().size());
		Assert.assertEquals(clause, policy.getClauses().get(0));
	}

}
