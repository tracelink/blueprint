package com.tracelink.prodsec.blueprint.app.statement;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

/**
 * DTO for a base statement argument. Used to transfer data to the UI and to perform validation
 * during startup when base statements are imported. Contains a method to convert this object to an
 * entity.
 *
 * @author mcool
 */
public class BaseStatementArgumentDto {

	@NotBlank(message = "Parameter cannot be blank")
	private String parameter;
	@NotBlank(message = "Description cannot be blank")
	private String description;
	@NotNull(message = "Argument type cannot be null")
	private ArgumentType type;
	// enumValues may be null or empty
	private Set<@NotBlank(message = "Enumerated values cannot be blank") String> enumValues;
	private boolean arrayUnordered;
	private boolean arrayUnique;

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

	public boolean isArrayUnique() {
		return arrayUnique;
	}

	public void setArrayUnique(boolean arrayUnique) {
		this.arrayUnique = arrayUnique;
	}

	public boolean isArrayUnordered() {
		return arrayUnordered;
	}

	public void setArrayUnordered(boolean arrayUnordered) {
		this.arrayUnordered = arrayUnordered;
	}
}
