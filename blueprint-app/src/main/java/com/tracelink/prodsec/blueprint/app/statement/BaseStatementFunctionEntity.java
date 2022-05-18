package com.tracelink.prodsec.blueprint.app.statement;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.converter.StringListConverter;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

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

	@Column(name = "author")
	private String author;

	@Column(name = "version")
	private int version;

	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private PolicyElementState state;

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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "function")
	private Set<BaseStatementEntity> baseStatements;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "dependencies")
	private Set<BaseStatementFunctionEntity> dependents;

	public BaseStatementFunctionEntity() {

	}

	public BaseStatementFunctionEntity(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public PolicyElementState getState() {
		return state;
	}

	public void setState(PolicyElementState state) {
		this.state = state;
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

	/**
	 * Returns the base statements that are dependent upon this function.
	 *
	 * @return all dependent base statements of this function
	 */
	public Set<BaseStatementEntity> getBaseStatements() {
		return baseStatements;
	}

	/**
	 * Returns the functions that are dependent upon this function.
	 *
	 * @return all dependent functions of this function
	 */
	public Set<BaseStatementFunctionEntity> getDependents() {
		return dependents;
	}

	public boolean isReferenced() {
		return !baseStatements.isEmpty() || !dependents.isEmpty();
	}

	public String getVersionedName() {
		return name + ":" + version;
	}

	/**
	 * Creates a revision of this function, copying all fields except the id, name, author, version,
	 * and state. Sets the version to one more than the current version, and the state to draft.
	 *
	 * @return the new revision of this function
	 */
	public BaseStatementFunctionEntity toRevision() {
		BaseStatementFunctionEntity entity = new BaseStatementFunctionEntity();
		entity.setName(name);
		entity.setVersion(version + 1);
		entity.setState(PolicyElementState.DRAFT);
		entity.setDescription(description);
		entity.setPolicyTypes(new HashSet<>(policyTypes));
		entity.setParameters(parameters);
		entity.setExpression(expression);
		entity.setDependencies(new HashSet<>(dependencies));
		return entity;
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatementFunction toCore() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName(name);
		function.setAuthor(author);
		function.setVersion(version);
		function.setState(state);
		function.setDescription(description);
		function.setPolicyTypes(
				policyTypes.stream().filter(Objects::nonNull).map(PolicyTypeEntity::getName)
						.collect(Collectors.toSet()));
		function.setParameters(parameters);
		function.setExpression(expression);
		function.setDependencies(dependencies.stream().filter(Objects::nonNull)
				.map(BaseStatementFunctionEntity::toCore).collect(Collectors.toSet()));
		return function;
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public BaseStatementFunctionDto toDto() {
		BaseStatementFunctionDto dto = new BaseStatementFunctionDto();
		dto.setName(name);
		dto.setAuthor(author);
		dto.setVersion(version);
		dto.setState(state);
		dto.setDescription(description);
		dto.setPolicyTypes(
				policyTypes.stream().filter(Objects::nonNull).map(PolicyTypeEntity::getName)
						.collect(Collectors.toSet()));
		dto.setParameters(parameters);
		dto.setExpression(expression);
		dto.setDependencies(dependencies.stream().filter(Objects::nonNull)
				.map(BaseStatementFunctionEntity::getVersionedName).collect(Collectors.toSet()));
		return dto;
	}
}
