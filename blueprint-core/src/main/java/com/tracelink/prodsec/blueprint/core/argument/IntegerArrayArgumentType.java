package com.tracelink.prodsec.blueprint.core.argument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of an {@link ArgumentType} for an integer array argument. Implements {@link
 * ArgumentType#getArrayItems(String)} to get a list of integers from the configured argument.
 *
 * @author mcool
 */
public class IntegerArrayArgumentType extends ArgumentType {

	IntegerArrayArgumentType() {
		super("integerArray", "integer array", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		try {
			List<Integer> integers = getArrayItems(configuredArgument);
			if (integers == null) {
				return false;
			}
			if (uniqueItems) {
				Set<Integer> uniqueIntegers = new HashSet<>(integers);
				return integers.size() == uniqueIntegers.size();
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateRego(String configuredArgument) {
		try {
			List<Integer> integers = getArrayItems(configuredArgument);
			if (integers == null) {
				return null;
			}
			return "[" + integers.stream().map(String::valueOf).collect(Collectors.joining(", "))
				+ "]";
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getArrayItems(String configuredArgument) {
		if (configuredArgument == null) {
			return null;
		}
		return Arrays.stream(configuredArgument.split(",")).map(String::trim).map(Integer::parseInt)
			.collect(Collectors.toList());
	}
}
