package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRuleTest;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentsConfigurationRuleTest extends AbstractRuleTest {

	@Test
	public void testConfiguredStatementValidationArgCounts() {
		PolicyClause clause = new PolicyClause();
		ConfiguredStatement stmt = new ConfiguredStatement();
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setType(ArgumentType.getTypeForName("number"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);
		stmt.setArgumentValues(Arrays.asList());

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage()
						.contains("but 0 arguments were provided"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidity() {
		PolicyClause clause = new PolicyClause();
		ConfiguredStatement stmt = new ConfiguredStatement();
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setType(ArgumentType.getTypeForName("number"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);
		stmt.setArgumentValues(Arrays.asList("foo"));

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage().contains("must be a number"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityArrayMismatch() {
		PolicyClause clause = new PolicyClause();
		ConfiguredStatement stmt = new ConfiguredStatement();
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setEnumValues(new HashSet<>(Arrays.asList("1", "2")));
		arg.setType(ArgumentType.getTypeForName("numberArray"));
		stmt.setArgumentValues(Arrays.asList("1"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage()
						.contains("1 must match the enumerated values [1, 2]"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityArrayEmpty() {
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setEnumValues(new HashSet<>(Arrays.asList("1", "2")));
		arg.setType(ArgumentType.getTypeForName("numberArray"));
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Arrays.asList(arg));

		ConfiguredStatement stmt = new ConfiguredStatement();
		stmt.setBaseStatement(baseStatement);
		stmt.setArgumentValues(Arrays.asList("T"));
		PolicyClause clause = new PolicyClause();
		clause.setStatements(Arrays.asList(stmt));

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertEquals(2, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage().contains("must be a number array"));
		Assert.assertTrue(report.getViolations().get(1).getMessage(),
				report.getViolations().get(1).getMessage().contains(
						"Cannot get array items of type number array from configured argument T"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityArrayUnique() {
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setEnumValues(new HashSet<>(Arrays.asList("1.0", "2.0")));
		arg.setType(ArgumentType.getTypeForName("numberArray"));
		arg.setArrayUnique(true);
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Collections.singletonList(arg));

		ConfiguredStatement stmt = new ConfiguredStatement();
		stmt.setBaseStatement(baseStatement);
		stmt.setArgumentValues(Collections.singletonList("1,1"));
		PolicyClause clause = new PolicyClause();
		clause.setStatements(Collections.singletonList(stmt));

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertEquals(1, report.getViolations().size());
		Assert.assertTrue(report.getViolations().get(0).getMessage(),
				report.getViolations().get(0).getMessage()
						.contains("This argument must be a number array with unique items"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityEnum() {
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setEnumValues(new HashSet<>(Arrays.asList("1.0", "2.0")));
		arg.setType(ArgumentType.getTypeForName("number"));
		arg.setArrayUnique(true);
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Collections.singletonList(arg));

		ConfiguredStatement stmt = new ConfiguredStatement();
		stmt.setBaseStatement(baseStatement);
		stmt.setArgumentValues(Collections.singletonList("2.0"));
		PolicyClause clause = new PolicyClause();
		clause.setStatements(Collections.singletonList(stmt));

		PolicyBuilderReport report = new PolicyBuilderReport(new Policy());
		report = stmt.accept(makeRule(), report);
		Assert.assertTrue(report.getViolations().isEmpty());
	}

	@Override
	protected void prepareTests() {
		Policy policy = PolicyMaker.createValidPolicy();
		policy.getClauses().get(0)
				.setStatements(Collections.singletonList(new ConfiguredStatement()));
		addCase("configuredStatementNoBaseStatement", policy, 0, 0);
		BaseStatement baseStatement1 = PolicyMaker.createValidBaseStatement();
		baseStatement1.setArguments(Collections.emptyList());
		addCase("baseStatementArgumentsCount", baseStatement1, 1, 0, (r -> Assert.assertEquals(
				"The number of arguments defined does not match the number of parameters for the function 'function_name'",
				r.getViolations().get(0).getMessage())));

		BaseStatement baseStatement2 = PolicyMaker.createValidBaseStatement();
		baseStatement2.getArguments().get(0).setParameter("foo");
		addCase("baseStatementArgumentsCount", baseStatement2, 1, 0, (r -> Assert.assertEquals(
				"The parameter 'foo' does not match the function parameter 'param'",
				r.getViolationsForLocation("baseStatement.arguments[0].parameter").get(0)
						.getMessage())));

		BaseStatement baseStatement3 = PolicyMaker.createValidBaseStatement();
		baseStatement3.getArguments().get(0).setType(ArgumentType.getTypeForName("integer"));
		baseStatement3.getArguments().get(0).setEnumValues(Collections.singleton("foo"));
		addCase("baseStatementArgumentsCount", baseStatement3, 1, 0, (r -> Assert.assertEquals(
				"The enum values do not match the type 'integer'",
				r.getViolationsForLocation("baseStatement.arguments[0].enumValues").get(0)
						.getMessage())));
	}

	@Override
	protected RuleSeverity expectedSeverity() {
		return RuleSeverity.ERROR;
	}

	@Override
	protected AbstractPolicyRule makeRule() {
		return new ArgumentsConfigurationRule();
	}
}
