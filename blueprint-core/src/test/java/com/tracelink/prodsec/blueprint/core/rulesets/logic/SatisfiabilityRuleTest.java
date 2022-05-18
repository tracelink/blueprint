package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;

public class SatisfiabilityRuleTest extends AbstractRuleTest {


	@Override
	protected void prepareTests() {
		Policy unsatisfiable = makeUnsatisfiablePolicy();
		addCase("Unsatisfiable", unsatisfiable, 1, 0, (r -> Assert
				.assertEquals(RuleSeverity.ERROR,
						r.getViolations().get(0).getRule().getSeverity())), (r -> Assert
				.assertEquals(
						"This clause is unsatisfiable. Two of the statements are equivalent and one of them is negated",
						r.getViolations().get(0).getMessage())));
		Policy satisfiable = PolicyMaker.createValidPolicy();
		addCase("Satisfiable", satisfiable, 0, 0);
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new SatisfiabilityRule();
	}

	private Policy makeUnsatisfiablePolicy() {
		Policy policy = PolicyMaker.createValidBasicPolicy();
		PolicyClause clause = new PolicyClause();
		BaseStatement baseStatement = PolicyMaker.createValidBaseStatement();

		ConfiguredStatement statement = new ConfiguredStatement();
		statement.setBaseStatement(baseStatement);
		statement.setNegated(true);

		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement);
		statement2.setNegated(false);

		clause.setStatements(Arrays.asList(statement, statement2));

		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

}
