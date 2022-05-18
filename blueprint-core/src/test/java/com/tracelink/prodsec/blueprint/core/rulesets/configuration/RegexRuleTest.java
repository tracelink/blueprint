package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Collections;
import org.junit.Assert;

public class RegexRuleTest extends AbstractRuleTest {


	@Override
	protected void prepareTests() {
		BaseStatement baseStatement = PolicyMaker.createValidBaseStatement();
		baseStatement.setName("base_statement");
		addCase("baseStatementName", baseStatement, 1, 0, (r ->
				Assert.assertEquals(
						"The base statement name 'base_statement' is invalid. Base statement names must only contain letters and spaces, with the first letter of each word capitalized",
						r.getViolations().get(0).getMessage())));

		BaseStatementFunction function = PolicyMaker.createValidFunction();
		function.setName("function name");
		function.setParameters(Collections.singletonList("param name"));
		addCase("functionNameAndParam", function, 2, 0, (r -> {
			Assert.assertEquals(
					"The base statement function name 'function name' is invalid. Function names must only contain letters, numbers and underscores and cannot start with a number",
					r.getViolations().get(0).getMessage());
			Assert.assertEquals(
					"The following parameters for the base statement function 'function name' are invalid: 'param name' Function parameters must only contain letters, numbers and underscores and cannot start with a number",
					r.getViolations().get(1).getMessage());
		}));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new RegexRule();
	}
}
