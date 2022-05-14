package com.tracelink.prodsec.blueprint.core.rules;

/**
 * A special Ruleset implementation of the {@linkplain PolicyRuleset} that only
 * permits initial configuration via
 * {@link #addPreconfiguredRule(AbstractPolicyRule)} but disallows normal
 * configuration via {@link #addRule(AbstractPolicyRule)}
 * <p>
 * This should be used for internally designed rulesets only
 *
 * @author csmith
 */
public class PreconfiguredRuleset extends PolicyRuleset {

	public PreconfiguredRuleset(String name) {
		super(name);
	}

	public PreconfiguredRuleset(String name, boolean stopOnFirstFailure) {
		super(name, stopOnFirstFailure);
	}

	/**
	 * Allows subclasses to configure a rule for this ruleset
	 *
	 * @param rule the rule to add to this ruleset
	 */
	protected final void addPreconfiguredRule(AbstractPolicyRule rule) {
		super.addRule(rule);
	}

	/**
	 * Any call to this method will throw a {@link RuntimeException}
	 */
	@Override
	public void addRule(AbstractPolicyRule rule) throws RuntimeException {
		throw new RuntimeException("You may not add additional rules to a built-in ruleset");
	}
}
