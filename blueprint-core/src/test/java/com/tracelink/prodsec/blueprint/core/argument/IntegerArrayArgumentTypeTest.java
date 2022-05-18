package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

public class IntegerArrayArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new IntegerArrayArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new IntegerArrayArgumentType().matchesArgument("5,5", false));
		Assert.assertTrue(new IntegerArrayArgumentType().matchesArgument("3,5", true));
		Assert.assertFalse(new IntegerArrayArgumentType().matchesArgument("5,5", true));
		Assert.assertFalse(new IntegerArrayArgumentType().matchesArgument("2.3,4", false));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new IntegerArrayArgumentType().generateRego(null));
		Assert.assertNull(new IntegerArrayArgumentType().generateRego("12,foo"));
		Assert.assertEquals("[12]", new IntegerArrayArgumentType().generateRego("12"));
		Assert.assertEquals("[12, 14]", new IntegerArrayArgumentType().generateRego(" 12, 14 "));
	}

	@Test
	public void testGetBaseType() {
		Assert.assertEquals("integer", new IntegerArrayArgumentType().getBaseType().getName());
	}

}
