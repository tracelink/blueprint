package com.tracelink.prodsec.blueprint.app.auth.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

/**
 * Describes the User Database entry
 *
 * @author csmith
 */
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private long id;

	@Column(name = "sso_id")
	private String ssoId;

	@Column(name = "username")
	@NotEmpty(message = "Please provide a username")
	private String username;

	@Column(name = "enabled")
	private int enabled;

	@Column(name = "password")
	private String password;

	@Transient
	private String passwordConfirmation;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<RoleEntity> roles = new HashSet<>();

	@Column(name = "last_login")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	@Column(name = "last_modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModified;

	@Column(name = "created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	public long getId() {
		return id;
	}

	public String getSsoId() {
		return ssoId;
	}

	public void setSsoId(String ssoId) {
		this.ssoId = ssoId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirmation() {
		return passwordConfirmation;
	}

	public void setPasswordConfirmation(String passwordConfirmation) {
		this.passwordConfirmation = passwordConfirmation;
	}

	public Set<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleEntity> roles) {
		this.roles = roles;
	}

	public String getRolesString() {
		return roles.stream().map(RoleEntity::getRoleName).collect(Collectors.joining(", "));
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
