package com.tracelink.prodsec.blueprint.core.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.policy.PolicyType;

/**
 * Model Object for a Base Statement
 *
 * @author mcool
 */
public class BaseStatement {

	@NotBlank(message = "Name cannot be blank")
	private String name;
	@NotBlank(message = "Description cannot be blank")
	private String description;
	@NotNull(message = "Negation cannot be null")
	private boolean negationAllowed;
	@NotEmpty(message = "A base statement must be valid for at least one policy type")
	private Set<@NotNull(message = "Policy types cannot be null") PolicyType> policyTypes;
	@NotNull(message = "Evaluated function cannot be null")
	@Valid
	private BaseStatementFunction function;
	private List<@NotNull(message = "Arguments cannot be null") @Valid BaseStatementArgument> arguments = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNegationAllowed() {
		return negationAllowed;
	}

	public void setNegationAllowed(boolean negationAllowed) {
		this.negationAllowed = negationAllowed;
	}

	public Set<PolicyType> getPolicyTypes() {
		return policyTypes;
	}

	public void setPolicyTypes(Set<PolicyType> policyTypes) {
		this.policyTypes = policyTypes;
	}

	public BaseStatementFunction getFunction() {
		return function;
	}

	public void setFunction(BaseStatementFunction function) {
		this.function = function;
	}

	public List<BaseStatementArgument> getArguments() {
		return arguments;
	}

	public void setArguments(List<BaseStatementArgument> arguments) {
		this.arguments = arguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseStatement that = (BaseStatement) o;
		return negationAllowed == that.negationAllowed && Objects.equals(name, that.name)
			&& Objects.equals(description, that.description) && Objects
			.equals(policyTypes, that.policyTypes) && Objects
			.equals(function, that.function) && Objects.equals(arguments, that.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, negationAllowed, policyTypes, function, arguments);
	}
}
