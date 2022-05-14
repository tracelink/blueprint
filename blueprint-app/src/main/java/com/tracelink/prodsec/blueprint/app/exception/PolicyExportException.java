package com.tracelink.prodsec.blueprint.app.exception;

/**
 * Exception thrown during policy export for validation problems with the policy model.
 *
 * @author mcool
 */
public class PolicyExportException extends PolicyException {

	private static final long serialVersionUID = -1459704208770300266L;

	public PolicyExportException(String message) {
		super("Cannot export policy. " + message);
	}
}
