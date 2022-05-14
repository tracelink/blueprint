package com.tracelink.prodsec.blueprint.core.rulesets;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.validation.PolicyBuilder;

public abstract class AbstractRulesetTest {

	private List<RulesetTestCase> testCases = new ArrayList<>();

	@Before
	public void setup() {
		prepareTests();
	}

	protected void addCase(Policy policy, int expectedViolations, int expectedErrors,
		Consumer<PolicyReport>... policyChecks) {
		RulesetTestCase testCase = new RulesetTestCase();
		testCase.policy = policy;
		testCase.expectedViolations = expectedViolations;
		testCase.expectedErrors = expectedErrors;
		testCase.policyChecks =
			policyChecks == null ? new ArrayList<>() : Arrays.asList(policyChecks);
		testCases.add(testCase);
	}

	@Test
	public void runTestCases() {
		PolicyRuleset ruleset = makeRuleset();
		for (RulesetTestCase rtc : testCases) {
			PolicyReport report = new PolicyBuilder().validate(rtc.policy, makeRuleset());
			Assert.assertEquals(MessageFormat
				.format("expected {0} errors, but got {1}", rtc.expectedErrors,
					report.getErrors().size()), rtc.expectedErrors, report.getErrors().size());
			Assert.assertEquals(MessageFormat
					.format("expected {0} violations, but got {1}", rtc.expectedViolations,
						report.getViolations().size()), rtc.expectedViolations,
				report.getViolations().size());
			for (Consumer<PolicyReport> reportConsumer : rtc.policyChecks) {
				reportConsumer.accept(report);
			}
		}
	}

	protected abstract PolicyRuleset makeRuleset();

	protected abstract void prepareTests();

	class RulesetTestCase {

		Policy policy;
		int expectedViolations;
		int expectedErrors;
		List<Consumer<PolicyReport>> policyChecks;
	}
}
