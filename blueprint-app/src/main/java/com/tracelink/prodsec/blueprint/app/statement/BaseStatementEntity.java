package com.tracelink.prodsec.blueprint.app.statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

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
	@JoinTable(name = "policy_type_statement", joinColumns = @JoinColumn(name = "base_statement_id"),
			inverseJoinColumns = @JoinColumn(name = "policy_type_id"))
	@OrderBy("name asc")
	private Set<PolicyTypeEntity> policyTypes = new HashSet<>();

	@Column(name = "negation_allowed")
	private boolean negationAllowed;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "function_id")
	private BaseStatementFunctionEntity function;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "base_statement_id", nullable = false)
	private final List<BaseStatementArgumentEntity> arguments = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "baseStatement")
	private List<ConfiguredStatementEntity> configuredStatements;

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

	public boolean isNegationAllowed() {
		return negationAllowed;
	}

	public void setNegationAllowed(boolean negationAllowed) {
		this.negationAllowed = negationAllowed;
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
		if (arguments != null) {
			this.arguments.clear();
			this.arguments.addAll(arguments);
		}
	}

	public boolean isReferenced() {
		return !configuredStatements.isEmpty();
	}

	public String getVersionedName() {
		return name + ":" + version;
	}

	/**
	 * Creates a revision of this base statement, copying all fields except the id, name, author,
	 * version, and state. Sets the version to one more than the current version, and the state to
	 * draft.
	 *
	 * @return the new revision of this base statement
	 */
	public BaseStatementEntity toRevision() {
		BaseStatementEntity entity = new BaseStatementEntity();
		entity.setName(name);
		entity.setVersion(version + 1);
		entity.setState(PolicyElementState.DRAFT);
		entity.setDescription(description);
		entity.setPolicyTypes(new HashSet<>(policyTypes));
		entity.setNegationAllowed(negationAllowed);
		entity.setFunction(function);
		entity.setArguments(arguments.stream().filter(Objects::nonNull)
				.map(BaseStatementArgumentEntity::toRevision)
				.collect(Collectors.toList()));
		return entity;
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public BaseStatement toCore() {
		BaseStatement statement = new BaseStatement();
		statement.setName(name);
		statement.setAuthor(author);
		statement.setVersion(version);
		statement.setState(state);
		statement.setDescription(description);
		statement.setNegationAllowed(negationAllowed);
		statement.setPolicyTypes(
				policyTypes.stream().filter(Objects::nonNull).map(PolicyTypeEntity::getName)
						.collect(Collectors.toSet()));
		if (function != null) {
			statement.setFunction(function.toCore());
		}
		statement.setArguments(arguments.stream().filter(Objects::nonNull)
				.map(BaseStatementArgumentEntity::toCore).collect(Collectors.toList()));
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
		dto.setAuthor(author);
		dto.setVersion(version);
		dto.setState(state);
		dto.setDescription(description);
		dto.setNegationAllowed(negationAllowed);
		dto.setPolicyTypes(
				policyTypes.stream().filter(Objects::nonNull).map(PolicyTypeEntity::getName)
						.collect(Collectors.toSet()));
		if (function != null) {
			dto.setFunction(function.getVersionedName());
		}
		dto.setArguments(
				arguments.stream().filter(Objects::nonNull).map(BaseStatementArgumentEntity::toDto)
						.collect(Collectors.toList()));
		return dto;
	}
}
