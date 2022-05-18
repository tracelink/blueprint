package com.tracelink.prodsec.blueprint.core;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.configuration.ConfigurationRuleset;
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
		PolicyBuilderReport report = PolicyBuilder.getInstance()
				.validate(policy);
		Assert.assertFalse(report.hasViolations());
	}

	@Test
	public void testValidateBasicFail() {
		Policy policy = PolicyMaker.createValidPolicy();
		policy.setPolicyType(null);
		PolicyBuilderReport report = PolicyBuilder.getInstance()
				.validate(policy);
		Assert.assertTrue(report.hasViolations());
	}

	@Test
	public void testValidateBasicRuleset() {
		Policy policy = PolicyMaker.createValidPolicy();
		PolicyRuleset ruleset = new ConfigurationRuleset();
		PolicyBuilderReport report = PolicyBuilder.getInstance().validate(policy, ruleset);
		Assert.assertFalse(report.hasErrors());
		Assert.assertFalse(report.hasViolations());
	}

	@Test
	public void testGenerateRego() {
		Policy policy = createValidPolicy();
		Assert.assertEquals(
				"package blueprint\n\ndefault allow = false\n\nallow {\n\tnot function_name([\"foo\", \"bar\"], \"foo\")\n\ttautology\n}\n\nfunction_name(array, value) {\n\tarray[_] == value\n}\n\ntautology {\n\t1 == 1\n}\n\n",
				PolicyBuilder.getInstance().generateRego(policy));
	}

	@Test
	public void testGenerateRegoBasic() {
		Policy policy = new Policy();
		policy.setPolicyType("User");
		String rego = PolicyBuilder.getInstance().generateRego(policy);
		Assert.assertNotNull(rego);
		Assert.assertTrue(rego.contains("default allow = false\n\n"));
	}

	private Policy createValidPolicy() {
		Policy policy = new Policy();
		PolicyClause clause = new PolicyClause();

		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("function_name");
		function1.setDescription("A Rego function");
		function1.setPolicyTypes(Collections.singleton("System"));
		function1.setExpression("array[_] == value");
		function1.setParameters(Arrays.asList("array", "value"));
		function1.setDependencies(Collections.emptySet());

		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setDescription("An argument");
		argument1.setType(ArgumentType.getTypeForName("stringArray"));

		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setDescription("A constant argument");
		argument2.setType(ArgumentType.getTypeForName("string"));

		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setName("Base Statement");
		baseStatement.setDescription("A base statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function1);
		baseStatement.setArguments(Arrays.asList(argument1, argument2));

		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement);
		statement1.setNegated(true);
		statement1.setArgumentValues(Arrays.asList("foo,bar", "foo"));

		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("tautology");
		function2.setDescription("A Rego function with no arguments");
		function2.setPolicyTypes(Collections.singleton("System"));
		function2.setExpression("1 == 1");
		function2.setParameters(Collections.emptyList());
		function2.setDependencies(Collections.emptySet());

		BaseStatement baseStatement2 = new BaseStatement();
		baseStatement2.setName("Base Statement");
		baseStatement2.setDescription("A base statement with no arguments");
		baseStatement2.setPolicyTypes(Collections.singleton("System"));
		baseStatement2.setNegationAllowed(false);
		baseStatement2.setFunction(function2);
		baseStatement2.setArguments(Collections.emptyList());

		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement2);
		statement2.setNegated(false);
		statement2.setArgumentValues(Collections.emptyList());

		clause.setStatements(Arrays.asList(statement1, statement2));

		policy.setPolicyType("System");
		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

}
