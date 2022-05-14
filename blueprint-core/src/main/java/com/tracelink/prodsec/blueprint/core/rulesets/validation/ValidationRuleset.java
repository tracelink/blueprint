package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import com.tracelink.prodsec.blueprint.core.rules.PreconfiguredRuleset;

/**
 * Contains rules for validation errors found in Policies
 *
 * @author csmith
 */
public class ValidationRuleset extends PreconfiguredRuleset {

	public ValidationRuleset(boolean stopOnFirstFailure) {
		super("Validation Ruleset", stopOnFirstFailure);
		addPreconfiguredRule(new DuplicateValueRule());
		addPreconfiguredRule(new MatchPolicyTypeRule());
	}

}
