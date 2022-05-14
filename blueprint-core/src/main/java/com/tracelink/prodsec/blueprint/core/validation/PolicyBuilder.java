package com.tracelink.prodsec.blueprint.core.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyRuleset;

/**
 * Primary handler of policy business logic. Validates Policies and generates their equivalent Rego
 * expressions
 *
 * @author csmith
 */
public class PolicyBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyBuilder.class);

	private Template policyTemplate;

	public PolicyBuilder() {
		TemplateLoader templateLoader = new ClassPathTemplateLoader("/templates/", ".hbs");
		Handlebars handlebars = new Handlebars(templateLoader).with(EscapingStrategy.NOOP);
		handlebars.registerHelper("configuredArg",
			((Helper<ArgumentType>) (argumentType, options) -> argumentType
				.generateRego(options.param(0))));
		try {
			policyTemplate = handlebars.compile("policy");
		} catch (HandlebarsException | IOException | NullPointerException e) {
			LOGGER.error("Cannot compile policy Rego template", e);
		}
	}

	/**
	 * Validate that the policy is configured correctly and run all rulesets on the
	 * policy. The policy report may return errors or violations if the policy is
	 * misconfigured, in which case the rulesets are not run. The report may return
	 * errors or violations also if a ruleset detects a problem with the policy.
	 *
	 * @param policy             the policy to check
	 * @param additionalRulesets any rulesets to run on the policy, or null/empty if
	 *                           none should be runs
	 * @return a {@link PolicyReport} indicating any issues found in the policy.
	 */
	public PolicyReport validate(Policy policy, PolicyRuleset... additionalRulesets) {
		PolicyReport report = new PolicyReport(policy);
		// Perform basic validation on policy object
		report = basicValidation(policy, report);
		if (report.hasViolations() || report.hasErrors()) {
			return report;
		}
		if (additionalRulesets != null) {
			for (PolicyRuleset ruleset : additionalRulesets) {
				report = ruleset.apply(policy, report);
				if (ruleset.shouldStopOnFirstFailure() && (report.hasViolations() || report
					.hasErrors())) {
					break;
				}
			}
		}

		return report;
	}

	private PolicyReport basicValidation(Policy policy, PolicyReport report) {
		PolicyValidator validator = new PolicyValidator();
		return validator.visit(policy, report);
	}

	/**
	 * Generate the Rego policy expressions for this {@link Policy}
	 *
	 * @param policy the policy to use to generate Rego expressions
	 * @return the valid Rego expression, or null if something went wrong
	 */
	public String generateRego(Policy policy) {
		Map<String, Object> templateArgs = new HashMap<>();
		templateArgs.put("policy", policy);
		templateArgs.put("dependencies", policy.getAllDependentFunctions());
		try {
			return policyTemplate.apply(templateArgs);
		} catch (HandlebarsException | IOException | NullPointerException e) {
			LOGGER.error("Cannot generate Rego", e); // TODO return as error in wrapper object
			return null;
		}
	}
}
