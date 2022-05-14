package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.argument.StringArgumentType;

public class StringArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new StringArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new StringArgumentType().matchesArgument("foo", false));
		Assert.assertTrue(new StringArgumentType().matchesArgument("bar", true));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new StringArgumentType().generateRego(null));
		Assert.assertEquals("\"foo\"", new StringArgumentType().generateRego("foo"));
	}

}
