package com.tracelink.prodsec.blueprint.core.report;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;

public class PolicyBuilderReportTest {

	@Test
	public void testGetRootNode() {
		Policy policy = new Policy();
		PolicyBuilderReport report = new PolicyBuilderReport(policy);
		Assert.assertEquals(policy, report.getRootNode());
	}

	@Test
	public void testHasViolationsLocation() {
		Policy policy = new Policy();
		PolicyBuilderReport report = new PolicyBuilderReport(policy);
		report.addViolation(BDDMockito.mock(AbstractPolicyRule.class), policy, "policy", "message");
		Assert.assertTrue(report.hasViolations("policy"));
		Assert.assertFalse(report.hasViolations("foo"));
	}

	@Test
	public void testGetViolationsForLocation() {
		Policy policy = new Policy();
		PolicyBuilderReport report = new PolicyBuilderReport(policy);
		report.addViolation(BDDMockito.mock(AbstractPolicyRule.class), policy, "policy", "message");
		Assert.assertEquals(1, report.getViolationsForLocation("policy").size());
		Assert.assertEquals("message",
				report.getViolationsForLocation("policy").get(0).getMessage());
	}

	@Test
	public void testAddErrorNode() {
		Policy policy = new Policy();
		PolicyBuilderReport report = new PolicyBuilderReport(policy);
		report.addErrorNode(policy, "message");
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertEquals("message", report.getErrors().get(0).getMessage());
	}

}
