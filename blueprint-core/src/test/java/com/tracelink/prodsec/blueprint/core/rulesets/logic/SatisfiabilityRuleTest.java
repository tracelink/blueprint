package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRuleTest;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.SatisfiabilityRule;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;

public class SatisfiabilityRuleTest extends AbstractPolicyRuleTest {


	@Override
	protected void prepareTests() {
		Policy unsatisfiable = makeUnsatisfiablePolicy();
		addCase("Unsatisfiable", unsatisfiable, 1, 0, (r -> Assert
			.assertEquals(RuleSeverity.HIGH, r.getViolations().get(0).getRule().getSeverity())));
		Policy satisfiable = PolicyMaker.createValidPolicy();
		addCase("Satisfiable", satisfiable, 0, 0);
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.HIGH;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new SatisfiabilityRule();
	}

	private Policy makeUnsatisfiablePolicy() {
		Policy policy = PolicyMaker.createValidBasicPolicy();
		PolicyClause clause = new PolicyClause(policy);
		BaseStatement baseStatement = PolicyMaker.createValidBaseStatement();

		ConfiguredStatement statement = new ConfiguredStatement(clause);
		statement.setBaseStatement(baseStatement);
		statement.setNegated(true);

		ConfiguredStatement statement2 = new ConfiguredStatement(clause);
		statement2.setBaseStatement(baseStatement);
		statement2.setNegated(false);

		clause.setStatements(Arrays.asList(statement, statement2));

		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

}
