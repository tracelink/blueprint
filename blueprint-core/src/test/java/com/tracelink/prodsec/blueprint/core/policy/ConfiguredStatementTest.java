package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;

public class ConfiguredStatementTest {

	@Test
	public void testGetAllDependentFunctions() {
		BaseStatementFunction function1 = BDDMockito.mock(BaseStatementFunction.class);
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("func2");
		BaseStatementFunction function3 = new BaseStatementFunction();
		function3.setName("func3");
		BDDMockito.when(function1.getName()).thenReturn("func1");
		BDDMockito.when(function1.getAllDependencies()).thenReturn(Set.of(function2, function3));
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setFunction(function1);

		ConfiguredStatement statement = new ConfiguredStatement();
		statement.setBaseStatement(baseStatement);
		Set<BaseStatementFunction> dependentFunctions = statement.getAllDependentFunctions();

		Assert.assertEquals(3, dependentFunctions.size());
		Assert.assertTrue(
				dependentFunctions.containsAll(Set.of(function1, function2, function3)));
	}

	@Test
	public void testEquals() {
		ConfiguredStatement statement = new ConfiguredStatement();
		Assert.assertEquals(statement, statement);
		Assert.assertFalse(statement.equals(new BaseStatement()));
	}

	@Test
	public void testEqualsHashCodeDifferentNegatedValue() {
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setNegated(true);
		ConfiguredStatement statement2 = new ConfiguredStatement();
		Assert.assertNotEquals(statement1, statement2);
		Assert.assertNotEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeDifferentBaseStatements() {
		BaseStatement baseStatement1 = new BaseStatement();
		baseStatement1.setNegationAllowed(true);
		BaseStatement baseStatement2 = new BaseStatement();
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement1);
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement2);

		Assert.assertNotEquals(statement1, statement2);
		Assert.assertNotEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeNoArgumentInfo() {
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setArgumentValues(Collections.singletonList("foo"));
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setArgumentValues(Collections.singletonList("foo"));

		Assert.assertEquals(statement1, statement2);
		Assert.assertEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeDifferentNumArgumentValues() {
		BaseStatement baseStatement = new BaseStatement();
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement);
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement);
		statement2.setArgumentValues(Collections.singletonList("foo"));

		Assert.assertNotEquals(statement1, statement2);
		Assert.assertNotEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeDifferentArgumentValues() {
		ArgumentType argumentType = BDDMockito.mock(ArgumentType.class);
		BDDMockito.when(argumentType.isArrayType()).thenReturn(false);
		BaseStatementArgument baseStatementArgument = new BaseStatementArgument();
		baseStatementArgument.setType(argumentType);
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Collections.singletonList(baseStatementArgument));
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement);
		statement1.setArgumentValues(Collections.singletonList("foo"));
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement);
		statement2.setArgumentValues(Collections.singletonList("bar"));

		Assert.assertNotEquals(statement1, statement2);
		Assert.assertNotEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeArrayArgumentValues() {
		ArgumentType argumentType = BDDMockito.mock(ArgumentType.class);
		BDDMockito.when(argumentType.isArrayType()).thenReturn(true);
		List items1 = Arrays.asList("foo", "bar");
		BDDMockito.when(argumentType.getArrayItems("foo,bar")).thenReturn(items1);
		List items2 = Arrays.asList("bar", "foo");
		BDDMockito.when(argumentType.getArrayItems("bar,foo")).thenReturn(items2);
		BaseStatementArgument baseStatementArgument = new BaseStatementArgument();
		baseStatementArgument.setType(argumentType);
		baseStatementArgument.setArrayUnordered(true);
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Collections.singletonList(baseStatementArgument));
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement);
		statement1.setArgumentValues(Collections.singletonList("foo,bar"));
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement);
		statement2.setArgumentValues(Collections.singletonList("bar,foo"));

		Assert.assertEquals(statement1, statement2);
		Assert.assertEquals(statement1.hashCode(), statement2.hashCode());
	}

	@Test
	public void testEqualsHashCodeArrayArgumentValuesCannotGetItems() {
		ArgumentType argumentType = BDDMockito.mock(ArgumentType.class);
		BDDMockito.when(argumentType.isArrayType()).thenReturn(true);
		BDDMockito.doThrow(NumberFormatException.class).when(argumentType).getArrayItems("foo,bar");
		BaseStatementArgument baseStatementArgument = new BaseStatementArgument();
		baseStatementArgument.setType(argumentType);
		baseStatementArgument.setArrayUnordered(true);
		BaseStatement baseStatement = new BaseStatement();
		baseStatement.setArguments(Collections.singletonList(baseStatementArgument));
		ConfiguredStatement statement1 = new ConfiguredStatement();
		statement1.setBaseStatement(baseStatement);
		statement1.setArgumentValues(Collections.singletonList("foo,bar"));
		ConfiguredStatement statement2 = new ConfiguredStatement();
		statement2.setBaseStatement(baseStatement);
		statement2.setArgumentValues(Collections.singletonList("bar,foo"));

		Assert.assertNotEquals(statement1, statement2);
		Assert.assertNotEquals(statement1.hashCode(), statement2.hashCode());
	}

}
