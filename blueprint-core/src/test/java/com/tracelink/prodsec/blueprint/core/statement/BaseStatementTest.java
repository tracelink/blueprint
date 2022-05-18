package com.tracelink.prodsec.blueprint.core.statement;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class BaseStatementTest {

	@Test
	public void testEquals() {
		BaseStatement statement = new BaseStatement();
		Assert.assertEquals(statement, statement);
		Assert.assertFalse(statement.equals(new BaseStatementFunction()));
	}

	@Test
	public void testEqualsDifferentNegationAllowed() {
		BaseStatement statement1 = new BaseStatement();
		statement1.setNegationAllowed(true);
		BaseStatement statement2 = new BaseStatement();
		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsDifferentName() {
		BaseStatement statement1 = new BaseStatement();
		statement1.setName("s1");
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("s2");
		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsDifferentDescription() {
		BaseStatement statement1 = new BaseStatement();
		statement1.setName("n");
		statement1.setDescription("d1");
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("n");
		statement2.setDescription("d2");
		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsDifferentPolicyTypes() {
		BaseStatement statement1 = new BaseStatement();
		statement1.setName("n");
		statement1.setDescription("d");
		statement1.setPolicyTypes(Collections.singleton("User"));
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("n");
		statement2.setDescription("d");
		statement2.setPolicyTypes(Collections.singleton("System"));
		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsDifferentFunction() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("f1");
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("f2");

		BaseStatement statement1 = new BaseStatement();
		statement1.setName("n");
		statement1.setDescription("d");
		statement1.setPolicyTypes(Collections.singleton("User"));
		statement1.setFunction(function1);
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("n");
		statement2.setDescription("d");
		statement2.setPolicyTypes(Collections.singleton("User"));
		statement2.setFunction(function2);

		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsDifferentArguments() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setParameter("param");
		BaseStatementArgument argument2 = new BaseStatementArgument();
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName("f");

		BaseStatement statement1 = new BaseStatement();
		statement1.setName("n");
		statement1.setDescription("d");
		statement1.setPolicyTypes(Collections.singleton("User"));
		statement1.setFunction(function);
		statement1.setArguments(Collections.singletonList(argument1));
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("n");
		statement2.setDescription("d");
		statement2.setPolicyTypes(Collections.singleton("User"));
		statement2.setFunction(function);
		statement2.setArguments(Collections.singletonList(argument2));

		Assert.assertNotEquals(statement1, statement2);
	}

	@Test
	public void testEqualsAllSame() {
		BaseStatementArgument argument = new BaseStatementArgument();
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName("f");

		BaseStatement statement1 = new BaseStatement();
		statement1.setName("n");
		statement1.setDescription("d");
		statement1.setPolicyTypes(Collections.singleton("User"));
		statement1.setFunction(function);
		statement1.setArguments(Collections.singletonList(argument));
		BaseStatement statement2 = new BaseStatement();
		statement2.setName("n");
		statement2.setDescription("d");
		statement2.setPolicyTypes(Collections.singleton("User"));
		statement2.setFunction(function);
		statement2.setArguments(Collections.singletonList(argument));

		Assert.assertEquals(statement1, statement2);
	}

	@Test
	public void testGetters() {
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setName("name");
		baseStatement.setAuthor("author");
		baseStatement.setVersion(1);
		baseStatement.setDescription("description");
		baseStatement.setNegationAllowed(true);

		Assert.assertEquals("name", baseStatement.getName());
		Assert.assertEquals("author", baseStatement.getAuthor());
		Assert.assertEquals(1, baseStatement.getVersion());
		Assert.assertEquals("description", baseStatement.getDescription());
		Assert.assertTrue(baseStatement.isNegationAllowed());
	}

	@Test
	public void testGetRoot() {
		BaseStatement baseStatement = new BaseStatement();
		Assert.assertEquals(baseStatement, baseStatement.getRoot());
		ConfiguredStatement configuredStatement = new ConfiguredStatement();
		configuredStatement.setBaseStatement(baseStatement);
		PolicyClause clause = new PolicyClause();
		clause.setStatements(Collections.singletonList(configuredStatement));
		Policy policy = new Policy();
		policy.setClauses(Collections.singletonList(clause));
		Assert.assertEquals(policy, baseStatement.getRoot());
	}

}
