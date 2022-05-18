package com.tracelink.prodsec.blueprint.core.report;

import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A report object for holding any {@link RuleViolation} and {@link PolicyBuilderError}
 * objects found during a ruleset scan of a {@link AbstractRootNode}.
 *
 * @author csmith
 */
public class PolicyBuilderReport {

	private final Map<String, List<RuleViolation>> violations = new HashMap<>();
	private final List<PolicyBuilderError> errors = new ArrayList<>();
	private final AbstractRootNode rootNode;

	public PolicyBuilderReport(AbstractRootNode rootNode) {
		this.rootNode = rootNode;
	}

	public AbstractRootNode getRootNode() {
		return this.rootNode;
	}

	/**
	 * Adds the supplied values as a violation to this report
	 *
	 * @param rule    the rule that was violated
	 * @param node    the node on which the violation occurred
	 * @param message the message indicating why the violation occurred
	 */
	public void addViolation(AbstractPolicyRule rule, AbstractPolicyNode node, String message) {
		addViolation(rule, node, node.getLocation(), message);
	}

	/**
	 * Adds the supplied values as a violation to this report
	 *
	 * @param rule     the rule that was violated
	 * @param location the location of the violation
	 * @param node     the node on which the violation occurred
	 * @param message  the message indicating why the violation occurred
	 */
	public void addViolation(AbstractPolicyRule rule, AbstractPolicyNode node, String location,
			String message) {
		if (violations.containsKey(location)) {
			violations.get(location).add(new RuleViolation(node, location, rule, message));
		} else {
			List<RuleViolation> locationViolations = new ArrayList<>();
			locationViolations.add(new RuleViolation(node, location, rule, message));
			violations.put(location, locationViolations);
		}
	}

	public List<RuleViolation> getViolations() {
		return violations.values().stream().flatMap(List::stream).collect(Collectors.toList());
	}

	public boolean hasViolations() {
		return violations.values().stream().flatMap(List::stream).anyMatch(
				violation -> !violation.getRule().getSeverity().equals(RuleSeverity.INFO));
	}

	public boolean hasViolations(String location) {
		return violations.containsKey(location);
	}

	public List<RuleViolation> getViolationsForLocation(String location) {
		return violations.get(location);
	}

	/**
	 * Adds the supplied values as an error to this report
	 *
	 * @param node    the node on which the violation occurred
	 * @param message the message indicating why the violation occurred
	 */
	public void addErrorNode(AbstractPolicyNode node, String message) {
		errors.add(new PolicyBuilderError(node, message));
	}

	public List<PolicyBuilderError> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}
