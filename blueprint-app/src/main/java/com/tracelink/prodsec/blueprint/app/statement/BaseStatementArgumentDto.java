package com.tracelink.prodsec.blueprint.app.statement;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.ConfiguredArgument;
import com.tracelink.prodsec.blueprint.core.statement.ConstantArgument;

/**
 * DTO for a base statement argument. Used to transfer data to the UI and to perform validation
 * during startup when base statements are imported. Contains a method to convert this object to an
 * entity.
 *
 * @author mcool
 */
public class BaseStatementArgumentDto {

	@NotNull(message = "Constant cannot be null", groups = {ConstantArgument.class,
			ConfiguredArgument.class})
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

	/**
	 * Converts this DTO object to an entity object to be stored in the database.
	 *
	 * @return the entity representation of this DTO
	 */
	public BaseStatementArgumentEntity toEntity() {
		BaseStatementArgumentEntity baseStatementArgument = new BaseStatementArgumentEntity();
		baseStatementArgument.setConstant(constant);
		baseStatementArgument.setValue(value);
		baseStatementArgument.setDescription(description);
		baseStatementArgument.setType(type);
		baseStatementArgument.setEnumValues(enumValues);
		return baseStatementArgument;
	}
}
