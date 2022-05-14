package com.tracelink.prodsec.blueprint.app.statement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.converter.StringListConverter;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

/**
 * Entity for a base statement function. Contains methods to convert this object to a DTO and to
 * write this function in Rego code.
 *
 * @author mcool
 */
@Entity
@Table(name = "base_statement_functions")
public class BaseStatementFunctionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "function_id")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "policy_type_function", joinColumns = @JoinColumn(name = "function_id"),
			inverseJoinColumns = @JoinColumn(name = "policy_type_id"))
	@OrderBy("name asc")
	private Set<PolicyTypeEntity> policyTypes = new HashSet<>();

	@Column(name = "parameters")
	@Convert(converter = StringListConverter.class)
	private List<String> parameters;

	@Column(name = "expression")
	private String expression;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "function_dependency", joinColumns = @JoinColumn(name = "function_id"), inverseJoinColumns = @JoinColumn(name = "dependency_id"))
	private Set<BaseStatementFunctionEntity> dependencies = new HashSet<>();

	public long getId() {
		return id;
	}

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

	public Set<PolicyTypeEntity> getPolicyTypes() {
		return policyTypes;
	}

	public void setPolicyTypes(Set<PolicyTypeEntity> policyTypes) {
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

	public Set<BaseStatementFunctionEntity> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<BaseStatementFunctionEntity> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (!parameters.isEmpty()) {
			sb.append("(");
			sb.append(String.join(", ", parameters));
			sb.append(")");
		}
		sb.append(" {\n\t");
		sb.append(expression);
		sb.append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatementFunction toCore() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName(name);
		function.setDescription(description);
		function.setPolicyTypes(
				policyTypes.stream().map(policyType -> new PolicyType(policyType.getName()))
						.collect(Collectors.toSet()));
		function.setParameters(parameters);
		function.setExpression(expression);
		function.setDependencies(dependencies.stream()
				.map(BaseStatementFunctionEntity::toCore)
				.collect(Collectors.toSet()));
		return function;
	}
}
