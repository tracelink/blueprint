package com.tracelink.prodsec.blueprint.core.argument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of an {@link ArgumentType} for a number array argument. Implements {@link
 * ArgumentType#getArrayItems(String)} to get a list of integers from the configured argument.
 *
 * @author mcool
 */
public class NumberArrayArgumentType extends ArgumentType {

	NumberArrayArgumentType() {
		super("numberArray", "number array", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesArgument(String configuredArgument, boolean uniqueItems) {
		try {
			List<Float> numbers = getArrayItems(configuredArgument);
			if (numbers == null) {
				return false;
			}
			if (uniqueItems) {
				Set<Float> uniqueNumbers = new HashSet<>(numbers);
				return numbers.size() == uniqueNumbers.size();
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
			List<Float> numbers = getArrayItems(configuredArgument);
			if (numbers == null) {
				return null;
			}
			return "[" + numbers.stream().map(String::valueOf).collect(Collectors.joining(", "))
				+ "]";
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public List<Float> getArrayItems(String configuredArgument) {
		if (configuredArgument == null) {
			return null;
		}
		return Arrays.stream(configuredArgument.split(",")).map(String::trim).map(Float::parseFloat)
			.collect(Collectors.toList());
	}
}
