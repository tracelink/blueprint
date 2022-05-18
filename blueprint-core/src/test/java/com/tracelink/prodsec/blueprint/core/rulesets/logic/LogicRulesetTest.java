package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRulesetTest;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;

public class LogicRulesetTest extends AbstractRulesetTest {

	@Override
	protected PolicyRuleset makeRuleset() {
		return new LogicRuleset();
	}

	@Override
	protected void prepareTests() {
		addCase(PolicyMaker.createValidPolicy(), 0, 0);
	}
}
