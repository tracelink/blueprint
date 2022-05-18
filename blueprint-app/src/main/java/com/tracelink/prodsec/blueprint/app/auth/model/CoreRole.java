package com.tracelink.prodsec.blueprint.app.auth.model;

/**
 * Enum for the roles that are included in policy builder by default to allow access to various
 * features.
 *
 * @author csmith
 */
public enum CoreRole {

	USER(CoreRole.USER_ROLE, "Basic privileges", true),
	BASE_STATEMENT_EDITOR(CoreRole.BASE_STMT_EDITOR_ROLE, "Edit base statements", false),
	FUNCTION_EDITOR(CoreRole.FUNCTION_EDITOR_ROLE, "Edit functions", false),
	ADMIN(CoreRole.ADMIN_ROLE, "Manage the system", false);

	public static final String USER_ROLE = "User";
	public static final String BASE_STMT_EDITOR_ROLE = "BaseStatementEditor";
	public static final String FUNCTION_EDITOR_ROLE = "FunctionEditor";
	public static final String ADMIN_ROLE = "Admin";

	private final String name;
	private final String desc;
	private final boolean defaultRole;

	CoreRole(String name, String desc, boolean defaultRole) {
		this.name = name;
		this.desc = desc;
		this.defaultRole = defaultRole;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return desc;
	}

	public boolean isDefaultRole() {
		return defaultRole;
	}

}
