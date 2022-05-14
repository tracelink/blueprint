package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import com.tracelink.prodsec.blueprint.core.policy.PolicyMaker;
import com.tracelink.prodsec.blueprint.core.rules.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.AbstractRulesetTest;
import com.tracelink.prodsec.blueprint.core.rulesets.logic.LogicRuleset;

public class LogicRulesetTest extends AbstractRulesetTest {

	@Override
	protected PolicyRuleset makeRuleset() {
		return new LogicRuleset(false);
	}

	@Override
	protected void prepareTests() {
		addCase(PolicyMaker.createValidPolicy(), 0, 0);
	}
}
