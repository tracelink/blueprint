package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRuleTest;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.validation.DuplicateValueRule;

import java.util.Arrays;
import org.junit.Assert;

public class DuplicateValueRuleTest extends AbstractPolicyRuleTest {

	@Override
	protected void prepareTests() {
		Policy dupeClausePolicy = PolicyMaker.createValidPolicy();
		PolicyClause clause = dupeClausePolicy.getClauses().get(0);
		dupeClausePolicy.setClauses(Arrays.asList(clause, clause));
		addCase("dupeClause", dupeClausePolicy, 1, 0,
			(r -> Assert
				.assertTrue("dupeClause: Wrong message: " + r.getViolations().get(0).getMessage(),
					r.getViolations().get(0).getMessage().contains("clause is a duplicate"))));

		Policy dupeStatementPolicy = PolicyMaker.createValidPolicy();
		PolicyClause clause2 = dupeStatementPolicy.getClauses().get(0);
		ConfiguredStatement statement = clause2.getStatements().get(0);
		clause2.setStatements(Arrays.asList(statement, statement));
		addCase("dupeStatement", dupeStatementPolicy, 1, 0,
			(r -> Assert.assertTrue(
				"dupeStatement: Wrong message: " + r.getViolations().get(0).getMessage(),
				r.getViolations().get(0).getMessage()
					.contains("duplicate and should be removed from the clause"))));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.INFO;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new DuplicateValueRule();
	}
}
