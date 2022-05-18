package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;

public class MatchPolicyTypeRuleTest extends AbstractRuleTest {

	@Override
	protected void prepareTests() {
		Policy typeMismatch = PolicyMaker.createValidPolicy();
		Set<String> policyTypes = typeMismatch.getClauses().get(0).getStatements().get(0)
				.getBaseStatement().getPolicyTypes();
		String mismatch = "testingOnly";
		Assert.assertFalse(policyTypes.contains(mismatch));
		typeMismatch.setPolicyType(mismatch);
		addCase("typeMismatchPolicy", typeMismatch, 1, 0,
				(r -> Assert
						.assertTrue("typeMismatch: Wrong message " + r.getViolations().get(0)
										.getMessage(),
								r.getViolations().get(0).getMessage()
										.contains("not compatible with the policy type"))));

		BaseStatement baseStatement = PolicyMaker.createValidBaseStatement();
		baseStatement.getFunction().setPolicyTypes(Collections.singleton(mismatch));
		addCase("typeMismatchBaseStatement", baseStatement, 1, 0,
				(r -> Assert
						.assertTrue("typeMismatch: Wrong message " + r.getViolations().get(0)
										.getMessage(),
								r.getViolations().get(0).getMessage()
										.contains(
												"not compatible with the policy types 'System'"))));

		BaseStatementFunction function = PolicyMaker.createValidFunction();
		BaseStatementFunction dependency = PolicyMaker.createValidFunction();
		dependency.setName("dependency");
		dependency.setPolicyTypes(Collections.singleton(mismatch));
		function.setDependencies(Collections.singleton(dependency));
		addCase("typeMismatchFunction", function, 1, 0,
				(r -> Assert
						.assertTrue("typeMismatch: Wrong message " + r.getViolations().get(0)
										.getMessage(),
								r.getViolations().get(0).getMessage()
										.contains(
												"not compatible with the policy types 'System'"))));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.WARN;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new MatchPolicyTypeRule();
	}
}
