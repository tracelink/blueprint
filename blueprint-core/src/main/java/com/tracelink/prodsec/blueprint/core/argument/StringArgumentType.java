package com.tracelink.prodsec.blueprint.core.argument;

/**
 * Implementation of an {@link ArgumentType} for a string argument.
 *
 * @author mcool
 */
public class StringArgumentType extends ArgumentType {

	StringArgumentType() {
		super("string", "String", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		return configuredArgument != null;// TODO Validation for injected code?
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateRego(String configuredArgument) {
		if (configuredArgument == null) {
			return null;
		}
		return "\"" + configuredArgument + "\"";
	}
}
