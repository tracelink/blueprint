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

	@Column(name = "parameter")
	private String parameter;

	@Column(name = "description")
	private String description;

	@Column(name = "arg_type")
	@Convert(converter = ArgumentTypeConverter.class)
	private ArgumentType type;

	@Column(name = "enum_values")
	@Convert(converter = StringSetConverter.class)
	private Set<String> enumValues;

	@Column(name = "array_unordered")
	private boolean arrayUnordered;

	@Column(name = "array_unique")
	private boolean arrayUnique;

	public long getId() {
		return id;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
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

	public boolean isArrayUnordered() {
		return arrayUnordered;
	}

	public void setArrayUnordered(boolean arrayUnordered) {
		this.arrayUnordered = arrayUnordered;
	}

	public boolean isArrayUnique() {
		return arrayUnique;
	}

	public void setArrayUnique(boolean arrayUnique) {
		this.arrayUnique = arrayUnique;
	}

	/**
	 * Creates a revision of this base statement argument, copying all fields except the id.
	 *
	 * @return the new revision of this base statement argument
	 */
	public BaseStatementArgumentEntity toRevision() {
		BaseStatementArgumentEntity entity = new BaseStatementArgumentEntity();
		entity.setParameter(parameter);
		entity.setDescription(description);
		entity.setType(type);
		entity.setEnumValues(enumValues);
		entity.setArrayUnordered(arrayUnordered);
		entity.setArrayUnique(arrayUnique);
		return entity;
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatementArgument toCore() {
		BaseStatementArgument argument = new BaseStatementArgument();
		argument.setParameter(parameter);
		argument.setDescription(description);
		argument.setType(type);
		argument.setEnumValues(enumValues);
		argument.setArrayUnordered(arrayUnordered);
		argument.setArrayUnique(arrayUnique);
		return argument;
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public BaseStatementArgumentDto toDto() {
		BaseStatementArgumentDto dto = new BaseStatementArgumentDto();
		dto.setParameter(parameter);
		dto.setDescription(description);
		dto.setType(type);
		dto.setEnumValues(enumValues);
		dto.setArrayUnordered(arrayUnordered);
		dto.setArrayUnique(arrayUnique);
		return dto;
	}
}
