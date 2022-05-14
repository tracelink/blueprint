package com.tracelink.prodsec.blueprint.core.argument;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An abstract representation of the type of an argument for a base statement. Contains the name
 * and display name for the argument type as well as a boolean indicating if the argument type is
 * an array type.
 *
 * @author mcool
 */
public abstract class ArgumentType {

	private final String name;
	private final String displayName;
	private final boolean arrayType;

	public ArgumentType(String name, String displayName, boolean arrayType) {
		this.name = name;
		this.displayName = displayName;
		this.arrayType = arrayType;
	}

	@JsonValue
	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isArrayType() {
		return arrayType;
	}

	/**
	 * Determines whether this argument type matches the given configured argument. Array types may
	 * also perform validation based on whether the argument must have unique items.
	 *
	 * @param configuredArgument the argument value to match against this type
	 * @param uniqueItems        whether the argument must contain unique values (if an array type)
	 * @return true if the configured argument is valid, false otherwise
	 */
	public abstract boolean matchesArgument(String configuredArgument, boolean uniqueItems);

	/**
	 * Generates the Rego value for the given configured argument for this argument type.
	 *
	 * @param configuredArgument the argument value to generate Rego for
	 * @return the Rego expression
	 */
	public String generateRego(String configuredArgument) {
		return configuredArgument;
	}

	/**
	 * Gets the list of items from the given argument value array. If the argument is not an array
	 * type, the default is to throw an {@link UnsupportedOperationException}.
	 *
	 * @param configuredArgument the argument value to parse array items from
	 * @return list of items from the array
	 */
	public List<?> getArrayItems(String configuredArgument) {
		throw new UnsupportedOperationException(
			"A " + getDisplayName() + " is not an array argument type");
	}

	@JsonCreator
	public static ArgumentType getTypeForName(String name) {
		List<ArgumentType> argumentTypes = Arrays
			.asList(new StringArgumentType(), new NumberArgumentType(),
				new IntegerArgumentType(), new BooleanArgumentType(),
				new StringArrayArgumentType(), new NumberArrayArgumentType(),
				new IntegerArrayArgumentType());

		for (ArgumentType argumentType : argumentTypes) {
			if (argumentType.getName().equals(name)) {
				return argumentType;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ArgumentType that = (ArgumentType) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
