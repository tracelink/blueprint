package com.tracelink.prodsec.blueprint.app.exception;

/**
 * Exception thrown for general policy issues
 *
 * @author mcool
 */
public class PolicyException extends Exception {

	private static final long serialVersionUID = -1681063840148203064L;

	public PolicyException(String message) {
		super(message);
	}
}
