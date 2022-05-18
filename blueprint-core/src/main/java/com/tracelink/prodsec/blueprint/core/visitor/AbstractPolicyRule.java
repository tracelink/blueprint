package com.tracelink.prodsec.blueprint.core.visitor;

import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;

/**
 * An abstract rule that uses the {@link PolicyVisitor} to apply a rule to all
 * nodes. A rule must also include a reference to its ruleset and a severity if
 * the rule is broken and must be reported on
 *
 * @author csmith
 */
public abstract class AbstractPolicyRule extends PolicyVisitor {

	private PolicyRuleset owningRuleset;
	private final String name;

	protected AbstractPolicyRule(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public PolicyRuleset getRuleset() {
		return this.owningRuleset;
	}

	public void setRuleset(PolicyRuleset ruleset) {
		this.owningRuleset = ruleset;
	}

	/**
	 * The severity of this rule if broken or found
	 *
	 * @return the rule's severity
	 */
	public abstract RuleSeverity getSeverity();


}
