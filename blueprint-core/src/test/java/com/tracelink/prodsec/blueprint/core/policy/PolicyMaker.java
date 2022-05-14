package com.tracelink.prodsec.blueprint.core.policy;

import java.util.Collections;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

public class PolicyMaker {

	public static Policy createValidPolicy() {
		Policy policy = createValidBasicPolicy();
		PolicyClause clause = new PolicyClause(policy);
		ConfiguredStatement statement = new ConfiguredStatement(clause);

		BaseStatement baseStatement = createValidBaseStatement();

		statement.setBaseStatement(baseStatement);
		statement.setNegated(true);

		clause.setStatements(Collections.singletonList(statement));

		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

	public static Policy createValidBasicPolicy() {
		Policy policy = new Policy();
		policy.setPolicyType(new PolicyType("System"));
		return policy;
	}

	public static BaseStatement createValidBaseStatement() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName("function_name");
		function.setDescription("A Rego function");
		function.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		function.setExpression("1 == 1");
		function.setParameters(Collections.emptyList());
		function.setDependencies(Collections.emptySet());

		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setName("Base Statement");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function);
		return baseStatement;
	}
}
