package com.tracelink.prodsec.blueprint.app.exception;

/**
 * Exception thrown during startup for validation problems with the imported base statements.
 *
 * @author mcool
 */
public class BaseStatementImportException extends RuntimeException {

	private static final long serialVersionUID = 3962679732480124413L;

	public BaseStatementImportException(String message) {
		super(message);
	}
}
