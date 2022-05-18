package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import java.util.Collections;

public class PolicyMaker {

	public static Policy createValidPolicy() {
		Policy policy = createValidBasicPolicy();
		PolicyClause clause = new PolicyClause();
		ConfiguredStatement statement = new ConfiguredStatement();
		statement.setArgumentValues(Collections.singletonList("value"));

		BaseStatement baseStatement = createValidBaseStatement();

		statement.setBaseStatement(baseStatement);
		statement.setNegated(true);

		clause.setStatements(Collections.singletonList(statement));

		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

	public static Policy createValidBasicPolicy() {
		Policy policy = new Policy();
		policy.setPolicyType("System");
		return policy;
	}

	public static BaseStatement createValidBaseStatement() {
		BaseStatementFunction function = createValidFunction();
		BaseStatementArgument argument = createValidArgument();

		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("user");
		baseStatement.setVersion(1);
		baseStatement.setState(PolicyElementState.RELEASED);
		baseStatement.setDescription("This is a base statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function);
		baseStatement.setArguments(Collections.singletonList(argument));
		return baseStatement;
	}

	public static BaseStatementFunction createValidFunction() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName("function_name");
		function.setAuthor("user");
		function.setVersion(1);
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("A Rego function");
		function.setPolicyTypes(Collections.singleton("System"));
		function.setExpression("1 == 1");
		function.setParameters(Collections.singletonList("param"));
		function.setDependencies(Collections.emptySet());
		return function;
	}

	public static BaseStatementArgument createValidArgument() {
		BaseStatementArgument argument = new BaseStatementArgument();
		argument.setParameter("param");
		argument.setType(ArgumentType.getTypeForName("string"));
		argument.setDescription("description");
		return argument;
	}
}
