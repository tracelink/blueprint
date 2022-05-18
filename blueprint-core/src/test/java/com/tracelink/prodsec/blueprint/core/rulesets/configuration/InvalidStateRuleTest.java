package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Collections;
import org.junit.Assert;

public class InvalidStateRuleTest extends AbstractRuleTest {

	@Override
	protected void prepareTests() {
		Policy policy = PolicyMaker.createValidPolicy();
		BaseStatement baseStatement1 = policy.getClauses().get(0).getStatements().get(0)
				.getBaseStatement();
		baseStatement1.setState(PolicyElementState.DRAFT);
		addCase("draftBaseStatement", policy, 1, 0, (r ->
				Assert.assertEquals(
						"The base statement 'Base Statement:1' is in the draft state. Please update to a released version or remove",
						r.getViolationsForLocation("clauses[0].statements[0].baseStatement").get(0)
								.getMessage())));

		BaseStatement baseStatement2 = PolicyMaker.createValidBaseStatement();
		BaseStatementFunction function1 = baseStatement2.getFunction();
		function1.setState(PolicyElementState.DEPRECATED);
		addCase("deprecatedFunction", baseStatement2, 1, 0, (r ->
				Assert.assertEquals(
						"The function 'function_name:1' is in the deprecated state. Please update to a released version or remove",
						r.getViolationsForLocation("baseStatement.function").get(0)
								.getMessage())));

		BaseStatementFunction function2 = PolicyMaker.createValidFunction();
		BaseStatementFunction function3 = PolicyMaker.createValidFunction();
		function3.setName("dependency");
		function3.setState(PolicyElementState.DRAFT);
		function2.setDependencies(Collections.singleton(function3));
		addCase("draftDependency", function2, 1, 0, (r ->
				Assert.assertEquals(
						"The dependency 'dependency:1' is in the draft state. Please update to a released version or remove",
						r.getViolationsForLocation("function.dependencies").get(0)
								.getMessage())));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new InvalidStateRule();
	}
}
