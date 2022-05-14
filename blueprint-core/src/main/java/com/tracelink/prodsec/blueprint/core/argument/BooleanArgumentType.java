package com.tracelink.prodsec.blueprint.core.argument;

/**
 * Implementation of an {@link ArgumentType} for a boolean argument.
 *
 * @author mcool
 */
public class BooleanArgumentType extends ArgumentType {

	BooleanArgumentType() {
		super("boolean", "boolean", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		if (configuredArgument == null) {
			return false;
		}
		return "true".equals(configuredArgument) || "false".equals(configuredArgument);
	}
}
