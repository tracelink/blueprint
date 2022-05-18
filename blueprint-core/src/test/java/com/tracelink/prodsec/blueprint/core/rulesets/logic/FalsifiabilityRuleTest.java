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

public class FalsifiabilityRuleTest extends AbstractRuleTest {


	@Override
	protected void prepareTests() {
		Policy isFalsifiable = makeFalsifiablePolicy();
		addCase("Found Falsifiable", makeFalsifiablePolicy(), 1, 0);
		addCase("Not Falsifiable", PolicyMaker.createValidPolicy(), 0, 0);
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new FalsifiabilityRule();
	}


	// (A AND NOT B) OR (NOT A AND C) OR (B AND NOT C) OR (A AND B) OR (NOT B AND NOT C)
	private Policy makeFalsifiablePolicy() {
		Policy policy = PolicyMaker.createValidBasicPolicy();
		policy.setPolicyType("System");

		//A case
		BaseStatement aBase = PolicyMaker.createValidBaseStatement();
		aBase.setName("A BASE");

		//B case
		BaseStatement bBase = PolicyMaker.createValidBaseStatement();
		bBase.setName("B BASE");

		//C case
		BaseStatement cBase = PolicyMaker.createValidBaseStatement();
		cBase.setName("C BASE");

		PolicyClause clause1 = new PolicyClause();
		clause1.setStatements(Arrays.asList(
				makeStmt(aBase, false),
				makeStmt(bBase, true)));

		PolicyClause clause2 = new PolicyClause();
		clause2.setStatements(Arrays.asList(
				makeStmt(aBase, true),
				makeStmt(cBase, false)));

		PolicyClause clause3 = new PolicyClause();
		clause3.setStatements(Arrays.asList(
				makeStmt(bBase, false),
				makeStmt(cBase, true)));

		PolicyClause clause4 = new PolicyClause();
		clause4.setStatements(Arrays.asList(
				makeStmt(aBase, false),
				makeStmt(bBase, false)));

		PolicyClause clause5 = new PolicyClause();
		clause5.setStatements(Arrays.asList(
				makeStmt(bBase, true),
				makeStmt(cBase, true)));

		policy.setClauses(Arrays.asList(clause1, clause2, clause3, clause4, clause5));

		return policy;
	}

	private ConfiguredStatement makeStmt(BaseStatement base, boolean negated) {
		ConfiguredStatement stmt = new ConfiguredStatement();
		stmt.setBaseStatement(base);
		stmt.setNegated(negated);
		return stmt;
	}
}
