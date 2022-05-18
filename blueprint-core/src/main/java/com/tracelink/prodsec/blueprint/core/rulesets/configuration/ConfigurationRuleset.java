package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.rulesets.PreconfiguredRuleset;

/**
 * Contains rules for configuration errors found in policy nodes.
 *
 * @author csmith
 */
public class ConfigurationRuleset extends PreconfiguredRuleset {

	public ConfigurationRuleset() {
		super("Configuration Ruleset");
		addPreconfiguredRule(new DuplicateValueRule());
		addPreconfiguredRule(new MatchPolicyTypeRule());
		addPreconfiguredRule(new RegexRule());
		addPreconfiguredRule(new InvalidStateRule());
	}

}
