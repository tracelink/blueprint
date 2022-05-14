package com.tracelink.prodsec.blueprint.app.statement;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.converter.ArgumentTypeConverter;
import com.tracelink.prodsec.blueprint.app.converter.StringSetConverter;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;

/**
 * Entity for a base statement argument. Contains a method to convert this object to a DTO.
 *
 * @author mcool
 */
@Entity
@Table(name = "base_statement_arguments")
public class BaseStatementArgumentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "argument_id")
	private long id;

	@Column(name = "constant")
	private boolean constant;

	@Column(name = "constant_value")
	private String value;

	@Column(name = "description")
	private String description;

	@Column(name = "arg_type")
	@Convert(converter = ArgumentTypeConverter.class)
	private ArgumentType type;

	@Column(name = "enum_values")
	@Convert(converter = StringSetConverter.class)
	private Set<String> enumValues;

	@Column(name = "unique_items")
	private boolean uniqueItems;

	@Column(name = "ordered_items")
	private boolean orderedItems;

	public long getId() {
		return id;
	}

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
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatementArgument toCore() {
		BaseStatementArgument argument = new BaseStatementArgument();
		argument.setConstant(constant);
		argument.setValue(value);
		argument.setDescription(description);
		argument.setType(type);
		argument.setEnumValues(enumValues);
		argument.setUniqueItems(uniqueItems);
		argument.setOrderedItems(orderedItems);
		return argument;
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public BaseStatementArgumentDto toDto() {
		BaseStatementArgumentDto dto = new BaseStatementArgumentDto();
		dto.setConstant(constant);
		dto.setValue(value);
		dto.setDescription(description);
		dto.setType(type);
		dto.setEnumValues(enumValues);
		dto.setUniqueItems(uniqueItems);
		dto.setOrderedItems(orderedItems);
		return dto;
	}
}
