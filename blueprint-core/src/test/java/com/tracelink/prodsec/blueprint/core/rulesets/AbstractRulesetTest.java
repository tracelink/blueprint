package com.tracelink.prodsec.blueprint.core.rulesets;

import com.tracelink.prodsec.blueprint.core.PolicyBuilder;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractRulesetTest {

	private final List<RulesetTestCase> testCases = new ArrayList<>();

	@Before
	public void setup() {
		prepareTests();
	}

	protected void addCase(AbstractRootNode rootNode, int expectedViolations, int expectedErrors,
			Consumer<PolicyBuilderReport>... policyChecks) {
		RulesetTestCase testCase = new RulesetTestCase();
		testCase.rootNode = rootNode;
		testCase.expectedViolations = expectedViolations;
		testCase.expectedErrors = expectedErrors;
		testCase.policyChecks =
				policyChecks == null ? new ArrayList<>() : Arrays.asList(policyChecks);
		testCases.add(testCase);
	}

	@Test
	public void runTestCases() {
		for (RulesetTestCase rtc : testCases) {
			PolicyBuilderReport report = PolicyBuilder.getInstance()
					.validate(rtc.rootNode, makeRuleset());
			Assert.assertEquals(MessageFormat
							.format("expected {0} errors, but got {1}", rtc.expectedErrors,
									report.getErrors().size()), rtc.expectedErrors,
					report.getErrors().size());
			Assert.assertEquals(MessageFormat
							.format("expected {0} violations, but got {1}", rtc.expectedViolations,
									report.getViolations().size()), rtc.expectedViolations,
					report.getViolations().size());
			for (Consumer<PolicyBuilderReport> reportConsumer : rtc.policyChecks) {
				reportConsumer.accept(report);
			}
		}
	}

	protected abstract PolicyRuleset makeRuleset();

	protected abstract void prepareTests();

	static class RulesetTestCase {

		AbstractRootNode rootNode;
		int expectedViolations;
		int expectedErrors;
		List<Consumer<PolicyBuilderReport>> policyChecks;
	}
}
