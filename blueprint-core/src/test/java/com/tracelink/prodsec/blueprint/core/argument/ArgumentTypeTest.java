package com.tracelink.prodsec.blueprint.core.argument;

import org.junit.Assert;
import org.junit.Test;

public class ArgumentTypeTest {

	@Test
	public void testEqualsHashCode() {
		ArgumentType argumentType = new BooleanArgumentType();
		Assert.assertEquals(argumentType, argumentType);
		Assert.assertNotEquals(argumentType.hashCode(), new NumberArgumentType().hashCode());
	}

	@Test
	public void testGetTypeForNameInvalid() {
		Assert.assertNull(ArgumentType.getTypeForName("Foo"));
	}

	@Test
	public void testGetters() {
		ArgumentType type = new ArgumentType("foo", "bar", true) {
			@Override
			public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
				return false;
			}
		};
		Assert.assertTrue(type.isArrayType());
		try {
			type.getArrayItems("foo");
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			Assert.assertEquals("A bar is not an array argument type", e.getMessage());
		}
	}
}
