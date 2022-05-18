package com.tracelink.prodsec.blueprint.app.auth.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity description for the Role entity. Holds information about the Role name
 *
 * @author csmith
 */
@Entity
@Table(name = "roles")
public class RoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private long id;

	@Column(name = "name")
	private String roleName;

	@Column(name = "description")
	private String description;

	@Column(name = "default_role")
	private boolean defaultRole;

	public long getId() {
		return id;
	}

	public String getRoleName() {
		return roleName;
	}

	public RoleEntity setRoleName(String roleName) {
		this.roleName = roleName;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public RoleEntity setDescription(String description) {
		this.description = description;
		return this;
	}

	public boolean isDefaultRole() {
		return defaultRole;
	}

	public RoleEntity setDefaultRole(boolean defaultRole) {
		this.defaultRole = defaultRole;
		return this;
	}

}
