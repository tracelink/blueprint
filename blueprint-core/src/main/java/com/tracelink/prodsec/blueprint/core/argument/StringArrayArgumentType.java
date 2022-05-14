package com.tracelink.prodsec.blueprint.core.argument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of an {@link ArgumentType} for a string array argument. Implements {@link
 * ArgumentType#getArrayItems(String)} to get a list of strings from the configured argument.
 *
 * @author mcool
 */
public class StringArrayArgumentType extends ArgumentType {

	StringArrayArgumentType() {
		super("stringArray", "string array", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		List<String> strings = getArrayItems(configuredArgument);
		if (strings == null) {
			return false;
		}
		// TODO Move this unique check elsewhere? Error to user is that the argument type is wrong if this check fails
		if (uniqueItems) {
			Set<String> uniqueStrings = new HashSet<>(strings);
			return strings.size() == uniqueStrings.size();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateRego(String configuredArgument) {
		List<String> strings = getArrayItems(configuredArgument);
		if (strings == null) {
			return null;
		}
		return "[\"" + String.join("\", \"", strings) + "\"]";

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getArrayItems(String configuredArgument) {
		if (configuredArgument == null) {
			return null;
		}
		return Arrays.stream(configuredArgument.split(",")).map(String::trim)
			.collect(Collectors.toList());
	}
}
