package com.tracelink.prodsec.blueprint.core.rules;

import java.util.ArrayList;
import java.util.List;

import com.tracelink.prodsec.blueprint.core.policy.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.policy.Policy;

/**
 * A report object for holding any {@link RuleViolation} and {@link PolicyError}
 * objects found during a ruleset scan of a {@link Policy}
 *
 * @author csmith
 */
public class PolicyReport {

	private final List<RuleViolation> violations = new ArrayList<>();
	private final List<PolicyError> errors = new ArrayList<>();
	private final Policy policy;

	public PolicyReport(Policy policy) {
		this.policy = policy;
	}

	public Policy getPolicy() {
		return this.policy;
	}

	/**
	 * Adds the supplied values as a violation to this report
	 *
	 * @param rule    the rule that was violated
	 * @param node    the node on which the violation occurred
	 * @param message the message indicating why the violation occurred
	 */
	public void addViolation(AbstractPolicyRule rule, AbstractPolicyNode node, String message) {
		violations.add(new RuleViolation(node, rule, message));
	}

	public List<RuleViolation> getViolations() {
		return this.violations;
	}

	public boolean hasViolations() {
		return !violations.isEmpty();
	}

	/**
	 * Adds the supplied values as an error to this report
	 *
	 * @param node    the node on which the violation occurred
	 * @param message the message indicating why the violation occurred
	 */
	public void addErrorNode(AbstractPolicyNode node, String message) {
		errors.add(new PolicyError(node, message));
	}

	/**
	 * Adds the supplied values as an error to this report
	 *
	 * @param location the string location on which the violation occurred
	 * @param message  the message indicating why the violation occurred
	 */
	public void addErrorLocation(String location, String message) {
		errors.add(new PolicyError(location, message));
	}

	public List<PolicyError> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}
