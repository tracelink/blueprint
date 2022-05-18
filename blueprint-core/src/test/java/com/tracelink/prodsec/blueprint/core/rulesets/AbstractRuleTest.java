package com.tracelink.prodsec.blueprint.core.rulesets;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractRuleTest {

	private final List<RuleTestCase> ruleTests = new ArrayList<>();

	protected void addCase(String name, AbstractRootNode rootNode, int expectedViolations,
			int expectedErrors, Consumer<PolicyBuilderReport>... policyChecks) {
		RuleTestCase testCase = new RuleTestCase();
		testCase.name = name;
		testCase.root = rootNode;
		testCase.expectedViolations = expectedViolations;
		testCase.expectedErrors = expectedErrors;
		testCase.policyChecks =
				policyChecks == null ? new ArrayList<>() : Arrays.asList(policyChecks);
		ruleTests.add(testCase);
	}

	@Before
	public void setup() {
		prepareTests();
	}


	@Test
	public void runTestCases() {
		for (RuleTestCase testCase : ruleTests) {
			PolicyBuilderReport report = new PolicyBuilderReport(testCase.root);
			AbstractPolicyRule rule = makeRule();
			testCase.root.accept(rule, report);
			Assert.assertEquals(
					report.getErrors().size() == 0 ?
							MessageFormat
									.format(testCase.name
													+ ": {0}, expected {1} errors, but got {2}",
											rule.getName(),
											testCase.expectedErrors, report.getErrors().size())
							: MessageFormat
									.format(testCase.name
													+ ": {0}, expected {1} errors, but got {2}. First Error: {3}",
											rule.getName(), testCase.expectedErrors,
											report.getErrors().size(),
											report.getErrors().get(0).getMessage()),
					testCase.expectedErrors, report.getErrors().size());

			Assert.assertEquals(
					report.getViolations().size() == 0 ?
							MessageFormat
									.format(testCase.name
													+ ": {0}, expected {1} violations, but got {2}.",
											rule.getName(), testCase.expectedViolations,
											report.getViolations().size()) :
							MessageFormat
									.format(testCase.name
													+ ": {0}, expected {1} violations, but got {2}. First Violation: {3}",
											rule.getName(), testCase.expectedViolations,
											report.getViolations().size(),
											report.getViolations().get(0).getMessage()),
					testCase.expectedViolations, report.getViolations().size());

			for (Consumer<PolicyBuilderReport> reportConsumer : testCase.policyChecks) {
				reportConsumer.accept(report);
			}
		}
	}

	@Test
	public void matchesSeverity() {
		Assert.assertEquals(expectedSeverity(), makeRule().getSeverity());
	}

	/**
	 * Implementations should use this to call {@link #addCase(String, AbstractRootNode, int, int,
	 * Consumer[])}
	 */
	protected abstract void prepareTests();

	protected abstract RuleSeverity expectedSeverity();

	protected abstract AbstractPolicyRule makeRule();

	static class RuleTestCase {

		String name;
		AbstractRootNode root;
		int expectedViolations;
		int expectedErrors;
		List<Consumer<PolicyBuilderReport>> policyChecks;
	}
}
