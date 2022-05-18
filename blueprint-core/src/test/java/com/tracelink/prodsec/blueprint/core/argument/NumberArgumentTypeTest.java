package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

public class NumberArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new NumberArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new NumberArgumentType().matchesArgument("2.3", false));
		Assert.assertFalse(new NumberArgumentType().matchesArgument("foo", false));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new NumberArgumentType().generateRego(null));
		Assert.assertEquals("1.2", new NumberArgumentType().generateRego("1.2"));
	}

}
