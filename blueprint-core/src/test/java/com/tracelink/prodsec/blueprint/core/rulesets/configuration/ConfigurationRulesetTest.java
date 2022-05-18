package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRulesetTest;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;

public class ConfigurationRulesetTest extends AbstractRulesetTest {

	@Override
	protected PolicyRuleset makeRuleset() {
		return new ConfigurationRuleset();
	}

	@Override
	protected void prepareTests() {
		addCase(PolicyMaker.createValidPolicy(), 0, 0);
	}
}
