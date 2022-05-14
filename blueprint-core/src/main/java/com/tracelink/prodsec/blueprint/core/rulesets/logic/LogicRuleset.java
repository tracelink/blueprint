package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.rules.PreconfiguredRuleset;

/**
 * Contains rules for logical failures in Policies
 *
 * @author csmith
 */
public class LogicRuleset extends PreconfiguredRuleset {

	public LogicRuleset(boolean stopOnFirstFailure) {
		super("Logic Ruleset", stopOnFirstFailure);
		addPreconfiguredRule(new FalsifiabilityRule());
		addPreconfiguredRule(new SatisfiabilityRule());
	}

}
