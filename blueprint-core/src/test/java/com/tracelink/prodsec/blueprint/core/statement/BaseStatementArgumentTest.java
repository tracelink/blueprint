package com.tracelink.prodsec.blueprint.core.statement;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

public class BaseStatementArgumentTest {

	@Test
	public void testEquals() {
		BaseStatementArgument argument = new BaseStatementArgument();
		Assert.assertEquals(argument, argument);
		Assert.assertFalse(argument.equals(new BaseStatement()));
	}

	@Test
	public void testEqualsDifferentConstant() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setConstant(true);
		BaseStatementArgument argument2 = new BaseStatementArgument();
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentUniqueItems() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setUniqueItems(true);
		BaseStatementArgument argument2 = new BaseStatementArgument();
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentOrderedItems() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setOrderedItems(true);
		BaseStatementArgument argument2 = new BaseStatementArgument();
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentValue() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setValue("v1");
		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setValue("v2");
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentDescription() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setValue("v");
		argument1.setDescription("d1");
		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setValue("v");
		argument2.setDescription("d2");
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentType() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setValue("v");
		argument1.setDescription("d");
		argument1.setType(ArgumentType.getTypeForName("boolean"));
		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setValue("v");
		argument2.setDescription("d");
		argument2.setType(ArgumentType.getTypeForName("number"));
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsDifferentEnumValues() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setValue("v");
		argument1.setDescription("d");
		argument1.setType(ArgumentType.getTypeForName("boolean"));
		argument1.setEnumValues(Collections.singleton("e1"));
		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setValue("v");
		argument2.setDescription("d");
		argument2.setType(ArgumentType.getTypeForName("boolean"));
		argument2.setEnumValues(Collections.singleton("e2"));
		Assert.assertNotEquals(argument1, argument2);
	}

	@Test
	public void testEqualsAllSame() {
		BaseStatementArgument argument1 = new BaseStatementArgument();
		argument1.setValue("v");
		argument1.setDescription("d");
		argument1.setType(ArgumentType.getTypeForName("boolean"));
		argument1.setEnumValues(Collections.singleton("e"));
		BaseStatementArgument argument2 = new BaseStatementArgument();
		argument2.setValue("v");
		argument2.setDescription("d");
		argument2.setType(ArgumentType.getTypeForName("boolean"));
		argument2.setEnumValues(Collections.singleton("e"));
		Assert.assertEquals(argument1, argument2);
	}

	@Test
	public void testGetters() {
		BaseStatementArgument argument = new BaseStatementArgument();
		argument.setDescription("description");
		argument.setEnumValues(Collections.singleton("foo"));
		argument.setUniqueItems(true);

		Assert.assertEquals("description", argument.getDescription());
		Assert.assertEquals(1, argument.getEnumValues().size());
		Assert.assertTrue(argument.getEnumValues().contains("foo"));
		Assert.assertTrue(argument.hasUniqueItems());
	}

}
