package com.tracelink.prodsec.blueprint.app.auth;

/**
 * Exception to be thrown for errors encountered while performing CRUD operations on a user.
 *
 * @author csmith
 */
public class UserAccountException extends Exception {

	private static final long serialVersionUID = 3690591357288345714L;

	public UserAccountException(String msg) {
		super(msg);
	}
}
