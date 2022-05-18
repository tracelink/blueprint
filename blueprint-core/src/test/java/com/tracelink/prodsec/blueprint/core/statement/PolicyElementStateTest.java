package com.tracelink.prodsec.blueprint.core.statement;

import org.junit.Assert;
import org.junit.Test;

public class PolicyElementStateTest {

	@Test
	public void testGetStateForName() {
		Assert.assertEquals(PolicyElementState.RELEASED,
				PolicyElementState.getStateForName("released"));
		Assert.assertNull(PolicyElementState.getStateForName("foo"));
	}

}
