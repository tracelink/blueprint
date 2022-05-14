package com.tracelink.prodsec.blueprint.core.validation;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.validation.PolicyValidator;

public class PolicyValidatorTest {

	@Test
	public void testPolicyConstraintValidation() {
		Policy policy = new Policy();
		policy.setPolicyType(new PolicyType("myCustom"));
		PolicyReport report = new PolicyReport(policy);
		report = new PolicyValidator().visit(policy, report);
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage()
				.contains("A policy must have at least one clause"));
	}

	@Test
	public void testConfiguredStatementValidationArgsExist() {
		ConfiguredStatement stmt = new ConfiguredStatement(null);
		PolicyReport report = new PolicyReport(new Policy());
		report = new PolicyValidator().visit(stmt, report);
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage().contains(
				"Cannot validate arguments because there is no argument info in the base statement"));
	}

	@Test
	public void testConfiguredStatementValidationArgCounts() {
		PolicyClause clause = new PolicyClause(null);
		ConfiguredStatement stmt = new ConfiguredStatement(clause);
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setValue("6");
		arg.setType(ArgumentType.getTypeForName("number"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);
		stmt.setArgumentValues(Arrays.asList());

		PolicyReport report = new PolicyReport(new Policy());
		report = new PolicyValidator().visit(stmt, report);
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage().contains("arguments were provided"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidity() {
		PolicyClause clause = new PolicyClause(null);
		ConfiguredStatement stmt = new ConfiguredStatement(clause);
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setValue("6");
		arg.setType(ArgumentType.getTypeForName("number"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);
		stmt.setArgumentValues(Arrays.asList("foo"));

		PolicyReport report = new PolicyReport(new Policy());
		report = new PolicyValidator().visit(stmt, report);
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage().contains("must be a number"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityArrayMismatch() {
		PolicyClause clause = new PolicyClause(null);
		ConfiguredStatement stmt = new ConfiguredStatement(clause);
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setValue("6");
		arg.setEnumValues(new HashSet<>(Arrays.asList("1", "2")));
		arg.setType(ArgumentType.getTypeForName("numberArray"));
		stmt.setArgumentValues(Arrays.asList("1"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);

		PolicyReport report = new PolicyReport(new Policy());
		report = new PolicyValidator().visit(stmt, report);
		Assert.assertEquals(1, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage()
				.contains("1 must match the enumerated values [1, 2]"));
	}

	@Test
	public void testConfiguredStatementValidationArgValidityArrayEmpty() {
		PolicyClause clause = new PolicyClause(null);
		ConfiguredStatement stmt = new ConfiguredStatement(clause);
		clause.setStatements(Arrays.asList(stmt));
		BaseStatement base = new BaseStatement();
		BaseStatementArgument arg = new BaseStatementArgument();
		arg.setValue("6");
		arg.setEnumValues(new HashSet<>(Arrays.asList("1", "2")));
		arg.setType(ArgumentType.getTypeForName("numberArray"));
		stmt.setArgumentValues(Arrays.asList("T"));
		base.setArguments(Arrays.asList(arg));
		stmt.setBaseStatement(base);

		PolicyReport report = new PolicyReport(new Policy());
		report = new PolicyValidator().visit(stmt, report);
		Assert.assertEquals(2, report.getErrors().size());
		Assert.assertTrue(report.getErrors().get(0).getMessage(),
			report.getErrors().get(0).getMessage().contains("must be a number array"));
		Assert.assertTrue(report.getErrors().get(1).getMessage(),
			report.getErrors().get(1).getMessage().contains(
				"Cannot get array items of type number array from configured argument T"));
	}
}
