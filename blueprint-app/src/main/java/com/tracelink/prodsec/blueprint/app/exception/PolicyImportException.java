package com.tracelink.prodsec.blueprint.app.exception;

/**
 * Exception thrown during policy import if the provided policy model is malformed.
 *
 * @author mcool
 */
public class PolicyImportException extends PolicyException {

	private static final long serialVersionUID = 2413967261356828425L;

	public PolicyImportException(String message) {
		super(message);
	}
}
