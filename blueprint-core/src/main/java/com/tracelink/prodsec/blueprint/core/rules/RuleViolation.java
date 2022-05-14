package com.tracelink.prodsec.blueprint.core.rules;

import com.tracelink.prodsec.blueprint.core.policy.AbstractPolicyNode;

/**
 * A violation object to hold information about a rule, it's failure, and the
 * location of the failure.
 *
 * @author csmith
 */
public class RuleViolation {

	private final AbstractPolicyNode node;
	private final AbstractPolicyRule rule;
	private final String message;

	public RuleViolation(AbstractPolicyNode node, AbstractPolicyRule rule, String message) {
		this.node = node;
		this.rule = rule;
		this.message = message;
	}

	public AbstractPolicyRule getRule() {
		return rule;
	}

	public String getMessage() {
		return message;
	}

	public AbstractPolicyNode getNode() {
		return node;
	}

	public String getLocation() {
		return node.getLocation();
	}
}
