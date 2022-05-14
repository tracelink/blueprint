package com.tracelink.prodsec.blueprint.app.policy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tracelink.prodsec.blueprint.core.policy.PolicyType;

/**
 * Represents the type of the policy. Used to determine which base statements and function can be
 * included in a policy.
 *
 * @author mcool
 */
@Entity
@Table(name = "policy_types")
public class PolicyTypeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "policy_type_id")
	private long id;

	@Column(name = "name")
	private String name;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Converts this entity object to a core object for validation and export.
	 *
	 * @return the core representation of this entity
	 */
	public PolicyType toCore() {
		return new PolicyType(name);
	}
}
