package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ConstraintValidationRuleTest extends AbstractRuleTest {

	@Test
	public void testPolicyConstraintValidation() {
		Policy policy = new Policy();
		policy.setPolicyType("myCustom");
		policy.setClauses(Collections.singletonList(new PolicyClause()));
		PolicyBuilderReport report = new PolicyBuilderReport(policy);
		report = new ConstraintValidationRule().visit(policy, report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage()
						.contains("A policy clause must have at least one configured statement"));
	}

	@Override
	protected void prepareTests() {
		BaseStatementFunction function = PolicyMaker.createValidFunction();
		addCase("functionValid", function, 0, 0);

		BaseStatementFunction function1 = PolicyMaker.createValidFunction();
		function1.setName(" ");
		addCase("functionBlankName", function1, 1, 0, (r -> Assert.assertEquals(
				"Name cannot be blank", r.getViolations().get(0).getMessage())));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new ConstraintValidationRule();
	}
}
