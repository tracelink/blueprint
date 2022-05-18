package com.tracelink.prodsec.blueprint.app.exception;

/**
 * Exception thrown for when a policy element cannot be found in the database
 *
 * @author mcool
 */
public class PolicyElementNotFoundException extends Exception {

	private static final long serialVersionUID = 5608504769425350010L;

	public PolicyElementNotFoundException(String message) {
		super(message);
	}
}
