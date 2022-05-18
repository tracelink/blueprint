package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.rulesets.PreconfiguredRuleset;

/**
 * Contains rule for basic constraint validation for policy elements. This ruleset always stops on
 * first failure.
 *
 * @author mcool
 */
public class ConstraintRuleset extends PreconfiguredRuleset {

	public ConstraintRuleset() {
		super("Constraint Ruleset", true);
		addPreconfiguredRule(new ConstraintValidationRule());
		addPreconfiguredRule(new ArgumentsConfigurationRule());
		addPreconfiguredRule(new CyclicDependenciesRule());
	}
}
