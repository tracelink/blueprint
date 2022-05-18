package com.tracelink.prodsec.blueprint.core.argument;

/**
 * Implementation of an {@link ArgumentType} for an integer argument.
 *
 * @author mcool
 */
public class IntegerArgumentType extends ArgumentType {

	IntegerArgumentType() {
		super("integer", "Integer", false);
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
			Integer.parseInt(configuredArgument);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	

}
