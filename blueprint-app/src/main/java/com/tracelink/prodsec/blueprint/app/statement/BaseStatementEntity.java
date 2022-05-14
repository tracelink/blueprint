package com.tracelink.prodsec.blueprint.app.statement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.core.policy.PolicyType;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;

/**
 * Entity for a base statement. Contains a method to convert this object to a DTO.
 *
 * @author mcool
 */
@Entity
@Table(name = "base_statements")
public class BaseStatementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "base_statement_id")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "negation_allowed")
	private boolean negationAllowed;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "policy_type_statement", joinColumns = @JoinColumn(name = "base_statement_id"),
			inverseJoinColumns = @JoinColumn(name = "policy_type_id"))
	@OrderBy("name asc")
	private Set<PolicyTypeEntity> policyTypes;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "function_id")
	private BaseStatementFunctionEntity function;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "base_statement_id", nullable = false)
	private List<BaseStatementArgumentEntity> arguments;

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

	public boolean isNegationAllowed() {
		return negationAllowed;
	}

	public void setNegationAllowed(boolean negationAllowed) {
		this.negationAllowed = negationAllowed;
	}

	public Set<PolicyTypeEntity> getPolicyTypes() {
		return policyTypes;
	}

	public void setPolicyTypes(Set<PolicyTypeEntity> policyTypes) {
		this.policyTypes = policyTypes;
	}

	public BaseStatementFunctionEntity getFunction() {
		return function;
	}

	public void setFunction(BaseStatementFunctionEntity function) {
		this.function = function;
	}

	public List<BaseStatementArgumentEntity> getArguments() {
		return arguments;
	}

	public void setArguments(List<BaseStatementArgumentEntity> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatement toCore() {
		BaseStatement statement = new BaseStatement();
		statement.setName(name);
		statement.setDescription(description);
		statement.setNegationAllowed(negationAllowed);
		statement.setPolicyTypes(
				policyTypes.stream().map(PolicyTypeEntity::getName).map(PolicyType::new)
						.collect(Collectors.toSet()));
		statement.setFunction(function.toCore());
		statement.setArguments(arguments.stream().map(BaseStatementArgumentEntity::toCore)
				.collect(Collectors.toList()));
		return statement;
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public BaseStatementDto toDto() {
		BaseStatementDto dto = new BaseStatementDto();
		dto.setName(name);
		dto.setDescription(description);
		dto.setNegationAllowed(negationAllowed);
		dto.setPolicyTypes(
				policyTypes.stream().map(PolicyTypeEntity::getName).collect(Collectors.toSet()));
		dto.setFunction(function.getName());
		dto.setArguments(arguments.stream().filter(arg -> !arg.isConstant())
				.map(BaseStatementArgumentEntity::toDto).collect(Collectors.toList()));
		return dto;
	}
}
