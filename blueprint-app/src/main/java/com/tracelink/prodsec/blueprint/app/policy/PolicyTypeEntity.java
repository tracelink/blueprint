package com.tracelink.prodsec.blueprint.app.policy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

	public PolicyTypeEntity() {

	}

	public PolicyTypeEntity(String name) {
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
}
