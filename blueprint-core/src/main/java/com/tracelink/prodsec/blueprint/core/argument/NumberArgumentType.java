package com.tracelink.prodsec.blueprint.core.argument;

/**
 * Implementation of an {@link ArgumentType} for a number argument, including decimals.
 *
 * @author mcool
 */
public class NumberArgumentType extends ArgumentType {

	NumberArgumentType() {
		super("number", "number", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		if (configuredArgument == null) {
			return false;
		}
		try {
			Float.parseFloat(configuredArgument);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
