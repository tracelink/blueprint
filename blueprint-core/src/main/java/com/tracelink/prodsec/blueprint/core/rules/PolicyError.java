package com.tracelink.prodsec.blueprint.core.rules;

import com.tracelink.prodsec.blueprint.core.policy.AbstractPolicyNode;

/**
 * An error object for reporting an issue with a rule. Errors should generally
 * be treated as unexpected conditions that prevent a rule from being run
 * properly.
 *
 * @author csmith
 */
public class PolicyError {

	private final String message;
	private final String location;

	public PolicyError(AbstractPolicyNode node, String errorMessage) {
		this.location = node.getLocation();
		this.message = errorMessage;
	}

	public PolicyError(String location, String errorMessage) {
		this.location = location;
		this.message = errorMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getLocation() {
		return location;
	}

}
