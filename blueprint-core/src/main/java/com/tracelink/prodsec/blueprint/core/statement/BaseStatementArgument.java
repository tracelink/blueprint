package com.tracelink.prodsec.blueprint.core.statement;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model Object for a base statement argument.
 *
 * @author mcool
 */
public class BaseStatementArgument extends AbstractPolicyNode {

	private BaseStatement parent;
	private int index;

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

	public void setParent(BaseStatement parent) {
		this.parent = parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	@Override
	public Iterable<? extends AbstractPolicyNode> children() {
		return null;
	}

	@Override
	public BaseStatement getParent() {
		return parent;
	}

	@Override
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	protected String getLocationIdentifier() {
		return "arguments[" + index + "]";
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
		return arrayUnique == argument.arrayUnique
				&& arrayUnordered == argument.arrayUnordered
				&& Objects.equals(parameter, argument.parameter)
				&& Objects.equals(description, argument.description)
				&& Objects.equals(type, argument.type)
				&& Objects.equals(enumValues, argument.enumValues);
	}

	@Override
	public int hashCode() {
		return Objects
				.hash(parameter, description, type, enumValues, arrayUnique, arrayUnordered);
	}
}
