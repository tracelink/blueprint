package com.tracelink.prodsec.blueprint.core.rulesets;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ruleset holds rule objects that will be run on a {@link Policy}
 *
 * @author csmith
 */
public class PolicyRuleset {

	private final String name;
	private final List<AbstractPolicyRule> rules = new ArrayList<>();
	private final boolean stopOnFirstFailure;

	/**
	 * Create a Ruleset with the given name. Defaults {@link #stopOnFirstFailure} to
	 * false
	 *
	 * @param name the ruleset name
	 */
	public PolicyRuleset(String name) {
		this(name, false);
	}

	/**
	 * Create a Ruleset with the given name and setting for stopping the rule
	 * analysis on first failure
	 *
	 * @param name               the ruleset name
	 * @param stopOnFirstFailure whether the analysis should stop if any rule has a
	 *                           failure
	 */
	public PolicyRuleset(String name, boolean stopOnFirstFailure) {
		this.name = name;
		this.stopOnFirstFailure = stopOnFirstFailure;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * True if the ruleset is configured to stop on the first error or violation in the rules
	 *
	 * @return true if the ruleset is configured to stop on the first error or violation in the rules
	 */
	public boolean shouldStopOnFirstFailure() {
		return stopOnFirstFailure;
	}

	/**
	 * Add a rule to this ruleset. The rule's ruleset value will be set as a side effect, so only
	 * newly-constructed rules should be added here
	 *
	 * @param rule the rule to be added to this ruleset
	 */
	public void addRule(AbstractPolicyRule rule) {
		rule.setRuleset(this);
		this.rules.add(rule);
	}

	public List<AbstractPolicyRule> getRules() {
		return Collections.unmodifiableList(rules);
	}

	/**
	 * Apply the given root node to all rules in this ruleset and return the report detailing the
	 * result
	 *
	 * @param rootNode the root node object to report on
	 * @param report   the report object detailing what happened before this call
	 * @return a report object detailing the results of the rules
	 */
	public PolicyBuilderReport apply(AbstractRootNode rootNode, PolicyBuilderReport report) {
		for (AbstractPolicyRule rule : getRules()) {
			report = rootNode.accept(rule, report);
			if (stopOnFirstFailure && (report.hasErrors() || report.hasViolations())) {
				return report;
			}
		}
		return report;
	}
}
