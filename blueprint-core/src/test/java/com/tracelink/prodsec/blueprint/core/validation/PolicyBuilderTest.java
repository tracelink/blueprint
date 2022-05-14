package com.tracelink.prodsec.blueprint.core.validation;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.validation.ValidationRuleset;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class PolicyBuilderTest {

	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(PolicyBuilder.class);

	@Test
	public void testValidateSimple() {
		Policy policy = PolicyMaker.createValidPolicy();
		PolicyReport report = new PolicyBuilder().validate(policy);
		Assert.assertFalse(report.hasViolations());
	}

	@Test
	public void testValidateBasicFail() {
		Policy policy = PolicyMaker.createValidPolicy();
		policy.setPolicyType(null);
		PolicyReport report = new PolicyBuilder().validate(policy);
		Assert.assertTrue(report.hasErrors());
	}

	@Test
	public void testValidateBasicRuleset() {
		Policy policy = PolicyMaker.createValidPolicy();
		PolicyRuleset ruleset = new ValidationRuleset(true);
		PolicyReport report = new PolicyBuilder().validate(policy, ruleset);
		Assert.assertFalse(report.hasErrors());
		Assert.assertFalse(report.hasViolations());
	}

	@Test
	public void testGenerateRego() {
		Policy policy = createValidPolicy();
		Assert.assertEquals(
			"default allow = false\n\nallow {\n\tnot function_name([\"foo\", \"bar\"], \"foo\")\n\ttautology\n}\n\nfunction_name(array, value) {\n\tarray[_] == value\n}\n\ntautology {\n\t1 == 1\n}\n\n",
			new PolicyBuilder().generateRego(policy));
	}

	@Test
	public void testGenerateRegoBasic() {
		Policy policy = new Policy();
		policy.setPolicyType(new PolicyType("User"));
		String rego = new PolicyBuilder().generateRego(policy);
		Assert.assertTrue(rego.contains("default allow = false\n\n"));
	}

	private Policy createValidPolicy() {
		Policy policy = new Policy();
		PolicyClause clause = new PolicyClause(policy);

		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("function_name");
		function1.setDescription("A Rego function");
		function1.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		function1.setExpression("array[_] == value");
		function1.setParameters(Arrays.asList("array", "value"));
		function1.setDependencies(Collections.emptySet());

		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setConstant(false);
		argument1.setDescription("An argument");
		argument1.setType(ArgumentType.getTypeForName("stringArray"));

		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setConstant(true);
		argument2.setDescription("A constant argument");
		argument2.setValue("\"foo\"");

		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setName("Base Statement");
		baseStatement.setDescription("A base statement");
		baseStatement.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function1);
		baseStatement.setArguments(Arrays.asList(argument1, argument2));

		ConfiguredStatement statement1 = new ConfiguredStatement(clause);
		statement1.setBaseStatement(baseStatement);
		statement1.setNegated(true);
		statement1.setArgumentValues(Collections.singletonList("foo,bar"));

		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("tautology");
		function2.setDescription("A Rego function with no arguments");
		function2.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		function2.setExpression("1 == 1");
		function2.setParameters(Collections.emptyList());
		function2.setDependencies(Collections.emptySet());

		BaseStatement baseStatement2 = new BaseStatement();
		baseStatement2.setName("Base Statement");
		baseStatement2.setDescription("A base statement with no arguments");
		baseStatement2.setPolicyTypes(Collections.singleton(new PolicyType("System")));
		baseStatement2.setNegationAllowed(false);
		baseStatement2.setFunction(function2);
		baseStatement2.setArguments(Collections.emptyList());

		ConfiguredStatement statement2 = new ConfiguredStatement(clause);
		statement2.setBaseStatement(baseStatement2);
		statement2.setNegated(false);
		statement2.setArgumentValues(Collections.emptyList());

		clause.setStatements(Arrays.asList(statement1, statement2));

		policy.setPolicyType(new PolicyType("System"));
		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

}
