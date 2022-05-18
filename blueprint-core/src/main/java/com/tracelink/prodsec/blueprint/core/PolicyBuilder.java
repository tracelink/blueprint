package com.tracelink.prodsec.blueprint.core;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.rulesets.PolicyRuleset;
import com.tracelink.prodsec.blueprint.core.rulesets.constraints.ConstraintRuleset;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary handler of policy business logic. Validates Policies and generates their equivalent Rego
 * expressions
 *
 * @author csmith
 */
public final class PolicyBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyBuilder.class);
	private static final PolicyBuilder INSTANCE = new PolicyBuilder();

	private Template policyTemplate;

	private PolicyBuilder() {
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

	public static PolicyBuilder getInstance() {
		return INSTANCE;
	}

	/**
	 * Validate that the root node is configured correctly and run all rulesets on the
	 * root node. The report will return errors or violations if a ruleset detects a problem with
	 * the root node or its children.
	 *
	 * @param rootNode           the root node to validate
	 * @param additionalRulesets any rulesets to run on the root node, or null/empty if none should
	 *                           be run
	 * @return a {@link PolicyBuilderReport} indicating any issues found in the root node or its children
	 */
	public PolicyBuilderReport validate(AbstractRootNode rootNode,
			PolicyRuleset... additionalRulesets) {
		PolicyBuilderReport report = new PolicyBuilderReport(rootNode);
		// Create list of rulesets to apply
		List<PolicyRuleset> rulesets = new ArrayList<>();
		rulesets.add(new ConstraintRuleset());
		rulesets.addAll(Arrays.asList(additionalRulesets));
		// Apply each ruleset
		for (PolicyRuleset ruleset : rulesets) {
			report = ruleset.apply(rootNode, report);
			if (ruleset.shouldStopOnFirstFailure()
					&& (report.hasViolations() || report.hasErrors())) {
				return report;
			}
		}
		return report;
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
