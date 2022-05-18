package com.tracelink.prodsec.blueprint.app.rulesets;

import com.tracelink.prodsec.blueprint.core.rulesets.PreconfiguredRuleset;

/**
 * Contains rule for basic constraint validation for policies that are being saved to the database.
 *
 * @author mcool
 */
public class SavedPolicyRuleset extends PreconfiguredRuleset {

	public SavedPolicyRuleset() {
		super("Saved Policy Ruleset");
		addPreconfiguredRule(new SavedPolicyRule());
	}
}
