package com.tracelink.prodsec.blueprint.core.rulesets.validation;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rules.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRulesetTest;
import com.tracelink.prodsec.blueprint.core.rulesets.validation.ValidationRuleset;

public class ValidationRulesetTest extends AbstractRulesetTest {

	@Override
	protected PolicyRuleset makeRuleset() {
		return new ValidationRuleset(false);
	}

	@Override
	protected void prepareTests() {
		addCase(PolicyMaker.createValidPolicy(), 0, 0);
	}
}
