package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.rulesets.PreconfiguredRuleset;

/**
 * Contains rules for logical failures in Policies
 *
 * @author csmith
 */
public class LogicRuleset extends PreconfiguredRuleset {

	public LogicRuleset() {
		super("Logic Ruleset");
		addPreconfiguredRule(new FalsifiabilityRule());
		addPreconfiguredRule(new SatisfiabilityRule());
	}

}
