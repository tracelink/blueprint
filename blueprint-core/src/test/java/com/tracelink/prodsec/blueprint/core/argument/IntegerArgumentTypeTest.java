package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.argument.IntegerArgumentType;

public class IntegerArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new IntegerArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new IntegerArgumentType().matchesArgument("2", false));
		Assert.assertFalse(new IntegerArgumentType().matchesArgument("2.3", false));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new IntegerArgumentType().generateRego(null));
		Assert.assertEquals("12", new IntegerArgumentType().generateRego("12"));
	}

}
