package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Assert;

public class CyclicDependenciesRuleTest extends AbstractRuleTest {


	@Override
	protected void prepareTests() {
		BaseStatementFunction function1 = PolicyMaker.createValidFunction();
		BaseStatementFunction function2 = PolicyMaker.createValidFunction();
		function2.setName("second_function");
		function1.setDependencies(Collections.singleton(function2));
		function2.setDependencies(Collections.singleton(function2));
		addCase("cyclicDependencies", function1, 1, 0,
				(r -> Assert.assertEquals(
						"The function 'function_name' has a cyclic dependency on the function 'second_function'",
						r.getViolations().get(0).getMessage())));

		BaseStatementFunction function3 = PolicyMaker.createValidFunction();
		function3.setName("third_function");
		function3.setDependencies(Collections.singleton(function3));
		addCase("selfReferential", function3, 1, 0,
				(r -> Assert.assertEquals(
						"The function 'third_function' has a cyclic dependency on the function 'third_function'",
						r.getViolations().get(0).getMessage())));
		BaseStatementFunction function4 = PolicyMaker.createValidFunction();
		function4.setName("fourth_function");
		BaseStatementFunction function5 = PolicyMaker.createValidFunction();
		function5.setName("fifth_function");
		BaseStatementFunction function6 = PolicyMaker.createValidFunction();
		function6.setName("sixth_function");
		// Function 4 depends on both, function 5 depends on function 6
		function4.setDependencies(new HashSet<>(Arrays.asList(function5, function6)));
		function5.setDependencies(Collections.singleton(function6));
		addCase("sameDependencies", function4, 0, 0);
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new CyclicDependenciesRule();
	}
}
