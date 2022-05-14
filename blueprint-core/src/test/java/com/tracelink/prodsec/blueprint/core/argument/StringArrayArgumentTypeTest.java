package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.argument.StringArrayArgumentType;

public class StringArrayArgumentTypeTest {

	@Test
	public void testMatchesArgument() {
		Assert.assertFalse(new StringArrayArgumentType().matchesArgument(null, false));
		Assert.assertTrue(new StringArrayArgumentType().matchesArgument(" bar, bar ", false));
		Assert.assertFalse(new StringArrayArgumentType().matchesArgument(" bar, bar ", true));
	}

	@Test
	public void testGenerateRego() {
		Assert.assertNull(new StringArrayArgumentType().generateRego(null));
		Assert.assertEquals("[\"foo\"]", new StringArrayArgumentType().generateRego("foo"));
		Assert.assertEquals("[\"foo\", \"bar\"]",
			new StringArrayArgumentType().generateRego("foo,bar"));
	}

}
