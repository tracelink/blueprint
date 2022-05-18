package com.tracelink.prodsec.blueprint.app.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.core.policy.Policy;

/**
 * Entity for a policy.
 *
 * @author mcool
 */
@Entity
@Table(name = "policy")
public class PolicyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "policy_id")
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "author")
	private String author;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "policy_type_id", nullable = false)
	private PolicyTypeEntity policyType;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "policy_id", nullable = false)
	private List<PolicyClauseEntity> clauses = new ArrayList<>();

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

	public PolicyTypeEntity getPolicyType() {
		return policyType;
	}

	public void setPolicyType(PolicyTypeEntity policyType) {
		this.policyType = policyType;
	}

	public List<PolicyClauseEntity> getClauses() {
		return clauses;
	}

	public void setClauses(List<PolicyClauseEntity> clauses) {
		if (clauses != null) {
			this.clauses.clear();
			this.clauses.addAll(clauses);
		}
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public Policy toCore() {
		Policy policy = new Policy();
		policy.setName(name);
		policy.setAuthor(author);
		if (policyType != null) {
			policy.setPolicyType(policyType.getName());
		}
		policy.setClauses(clauses.stream().map(PolicyClauseEntity::toCore)
				.collect(Collectors.toUnmodifiableList()));
		return policy;
	}

	/**
	 * Converts this entity object to a DTO to be displayed in the UI.
	 *
	 * @return the DTO representation of this entity
	 */
	public PolicyDto toDto() {
		PolicyDto dto = new PolicyDto();
		dto.setClauses(clauses.stream().map(PolicyClauseEntity::toDto)
				.collect(Collectors.toUnmodifiableList()));
		dto.setPolicyType(policyType.getName());
		return dto;
	}
}
