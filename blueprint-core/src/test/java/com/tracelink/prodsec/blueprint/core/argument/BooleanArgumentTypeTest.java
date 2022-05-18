package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

public class BooleanArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new BooleanArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new BooleanArgumentType().matchesArgument("true", false));
		Assert.assertTrue(new BooleanArgumentType().matchesArgument("false", true));
		Assert.assertFalse(new BooleanArgumentType().matchesArgument("foo", false));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new BooleanArgumentType().generateRego(null));
		Assert.assertEquals("true", new BooleanArgumentType().generateRego("true"));
	}

}
