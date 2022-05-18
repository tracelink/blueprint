package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

public class NumberArrayArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new NumberArrayArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new NumberArrayArgumentType().matchesArgument("2,5.0", false));
		Assert.assertFalse(new NumberArrayArgumentType().matchesArgument("5,5.0", true));
		Assert.assertFalse(new NumberArrayArgumentType().matchesArgument("2.3,foo", false));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new NumberArrayArgumentType().generateRego(null));
		Assert.assertNull(new NumberArrayArgumentType().generateRego("1.2,bar"));
		Assert.assertEquals("[12.0, 1.4]", new NumberArrayArgumentType().generateRego(" 12, 1.4 "));
	}

	@Test
	public void testGetBaseType() {
		Assert.assertEquals("number", new NumberArrayArgumentType().getBaseType().getName());
	}

}
