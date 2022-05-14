package com.tracelink.prodsec.blueprint.core.statement;

import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

/**
 * Model Object for a base statement argument.
 *
 * @author mcool
 */
public class BaseStatementArgument {

	@NotNull(message = "Constant cannot be null", groups = {ConstantArgument.class,
		ConfiguredArgument.class}) // TODO test this, groups?
	private boolean constant;
	@NotBlank(message = "Value cannot be blank for a constant argument", groups = ConstantArgument.class)
	private String value;
	@NotBlank(message = "Description cannot be blank for a configured argument", groups = ConfiguredArgument.class)
	private String description;
	@NotNull(message = "Argument type cannot be null for a configured argument", groups = ConfiguredArgument.class)
	private ArgumentType type;
	private Set<@NotBlank(message = "Enumerated values cannot be blank for a configured argument", groups = ConfiguredArgument.class) String> enumValues;
	private boolean uniqueItems;
	private boolean orderedItems;

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArgumentType getType() {
		return type;
	}

	public void setType(ArgumentType type) {
		this.type = type;
	}

	public Set<String> getEnumValues() {
		return enumValues;
	}

	public void setEnumValues(Set<String> enumValues) {
		this.enumValues = enumValues;
	}

	public boolean hasUniqueItems() {
		return uniqueItems;
	}

	public void setUniqueItems(boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}

	public boolean hasOrderedItems() {
		return orderedItems;
	}

	public void setOrderedItems(boolean orderedItems) {
		this.orderedItems = orderedItems;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseStatementArgument argument = (BaseStatementArgument) o;
		return constant == argument.constant && uniqueItems == argument.uniqueItems
			&& orderedItems == argument.orderedItems && Objects
			.equals(value, argument.value) && Objects
			.equals(description, argument.description) && Objects
			.equals(type, argument.type) && Objects
			.equals(enumValues, argument.enumValues);
	}

	@Override
	public int hashCode() {
		return Objects
			.hash(constant, value, description, type, enumValues, uniqueItems, orderedItems);
	}
}
