package com.tracelink.prodsec.blueprint.core.statement;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tracelink.prodsec.blueprint.core.policy.PolicyType;

/**
 * Model Object for a base statement function.
 *
 * @author mcool
 */
public class BaseStatementFunction {

	private String name;
	private String description;
	private Set<PolicyType> policyTypes;
	private List<String> parameters;
	private String expression;
	private Set<BaseStatementFunction> dependencies;

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

	public Set<PolicyType> getPolicyTypes() {
		return policyTypes;
	}

	public void setPolicyTypes(Set<PolicyType> policyTypes) {
		this.policyTypes = policyTypes;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Set<BaseStatementFunction> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<BaseStatementFunction> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseStatementFunction function = (BaseStatementFunction) o;
		return Objects.equals(name, function.name) && Objects
			.equals(description, function.description) && Objects
			.equals(policyTypes, function.policyTypes) && Objects
			.equals(parameters, function.parameters) && Objects
			.equals(expression, function.expression) && Objects
			.equals(dependencies, function.dependencies);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, policyTypes, parameters, expression, dependencies);
	}

	public Set<BaseStatementFunction> getAllDependencies() {
		Set<BaseStatementFunction> allDependencies = new HashSet<>(dependencies);
		allDependencies.addAll(dependencies.stream().map(BaseStatementFunction::getAllDependencies)
			.flatMap(Set::stream).collect(Collectors.toSet()));
		return allDependencies;
	}
}
