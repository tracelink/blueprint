package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRuleTest;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.FalsifiabilityRule;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;

import java.util.Arrays;

public class FalsifiabilityRuleTest extends AbstractPolicyRuleTest {


	@Override
	protected void prepareTests() {
		Policy isFalsifiable = makeFalsifiablePolicy();
		addCase("Found Falsifiable", makeFalsifiablePolicy(), 1, 0);
		addCase("Not Falsifiable", PolicyMaker.createValidPolicy(), 0, 0);
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.HIGH;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new FalsifiabilityRule();
	}


	// (A AND NOT B) OR (NOT A AND C) OR (B AND NOT C) OR (A AND B) OR (NOT B AND NOT C)
	private Policy makeFalsifiablePolicy() {
		Policy policy = PolicyMaker.createValidBasicPolicy();
		policy.setPolicyType(new PolicyType("System"));

		//A case
		BaseStatement aBase = PolicyMaker.createValidBaseStatement();
		aBase.setName("A BASE");

		//B case
		BaseStatement bBase = PolicyMaker.createValidBaseStatement();
		bBase.setName("B BASE");

		//C case
		BaseStatement cBase = PolicyMaker.createValidBaseStatement();
		cBase.setName("C BASE");

		PolicyClause clause1 = new PolicyClause(policy);
		clause1.setStatements(Arrays.asList(
			makeStmt(clause1, aBase, false),
			makeStmt(clause1, bBase, true)));

		PolicyClause clause2 = new PolicyClause(policy);
		clause2.setStatements(Arrays.asList(
			makeStmt(clause2, aBase, true),
			makeStmt(clause2, cBase, false)));

		PolicyClause clause3 = new PolicyClause(policy);
		clause3.setStatements(Arrays.asList(
			makeStmt(clause3, bBase, false),
			makeStmt(clause3, cBase, true)));

		PolicyClause clause4 = new PolicyClause(policy);
		clause4.setStatements(Arrays.asList(
			makeStmt(clause4, aBase, false),
			makeStmt(clause4, bBase, false)));

		PolicyClause clause5 = new PolicyClause(policy);
		clause5.setStatements(Arrays.asList(
			makeStmt(clause5, bBase, true),
			makeStmt(clause5, cBase, true)));

		policy.setClauses(Arrays.asList(clause1, clause2, clause3, clause4, clause5));

		return policy;
	}

	private ConfiguredStatement makeStmt(PolicyClause clause, BaseStatement base, boolean negated) {
		ConfiguredStatement stmt = new ConfiguredStatement(clause);
		stmt.setBaseStatement(base);
		stmt.setNegated(negated);
		return stmt;
	}
}
