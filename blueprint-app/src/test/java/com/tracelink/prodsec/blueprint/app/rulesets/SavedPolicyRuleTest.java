package com.tracelink.prodsec.blueprint.app.rulesets;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SavedPolicyRuleTest {

	private SavedPolicyRule rule;

	@Before
	public void setup() {
		rule = new SavedPolicyRule();
	}

	@Test
	public void testBlankNameAndAuthor() {
		Policy root = new Policy();
		PolicyBuilderReport report = new PolicyBuilderReport(root);
		root.accept(rule, report);
		Assert.assertEquals(2, report.getViolations().size());
		Assert.assertEquals(0, report.getErrors().size());
		MatcherAssert.assertThat(report.getViolations(), Matchers.hasItem(Matchers.allOf(
				Matchers.hasProperty("message", Matchers.is("Name cannot be blank")),
				Matchers.hasProperty("location", Matchers.is("policy.name")))));
		MatcherAssert.assertThat(report.getViolations(), Matchers.hasItem(Matchers.allOf(
				Matchers.hasProperty("message", Matchers.is("Author cannot be blank")),
				Matchers.hasProperty("location", Matchers.is("policy.author")))));
	}

	@Test
	public void testInvalidName() {
		Policy root = new Policy();
		root.setName("Name1");
		root.setAuthor("user");
		PolicyBuilderReport report = new PolicyBuilderReport(root);
		root.accept(rule, report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertEquals(0, report.getErrors().size());
		Assert.assertEquals(
				"The policy name 'Name1' is invalid. Policy names must only contain letters and spaces, with the first letter of each word capitalized",
				report.getViolations().get(0).getMessage());
		Assert.assertEquals("policy.name", report.getViolations().get(0).getLocation());
	}

}
