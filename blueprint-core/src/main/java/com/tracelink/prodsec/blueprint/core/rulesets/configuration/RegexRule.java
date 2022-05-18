package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.text.MessageFormat;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Identifies base statements with invalid names and functions with invalid names or parameters.
 *
 * @author mcool
 */
public class RegexRule extends AbstractPolicyRule {

	private static final Pattern ALPHABET_SPACE_PATTERN = Pattern
			.compile("^[A-Z][A-Za-z]*( [A-Z][A-Za-z]*)*$");
	private static final String ALPHABET_SPACE_MESSAGE = "must only contain letters and spaces, with the first letter of each word capitalized";
	private static final Pattern REGO_VARIABLE_PATTERN = Pattern
			.compile("^[A-Za-z_]+[A-Za-z0-9_]*$");
	private static final String REGO_VARIABLE_MESSAGE = "must only contain letters, numbers and underscores and cannot start with a number";

	protected RegexRule() {
		super("Regex Rule");
	}

	@Override
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		// Ensure base statement name matches the regex pattern
		if (!ALPHABET_SPACE_PATTERN.matcher(node.getName()).matches()) {
			report.addViolation(this, node, MessageFormat
					.format("The base statement name ''{0}'' is invalid. Base statement names {1}",
							node.getName(), ALPHABET_SPACE_MESSAGE));
		}
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		// Ensure function name matches the regex pattern
		if (!REGO_VARIABLE_PATTERN.matcher(node.getName()).matches()) {
			report.addViolation(this, node, MessageFormat
					.format("The base statement function name ''{0}'' is invalid. Function names {1}",
							node.getName(), REGO_VARIABLE_MESSAGE));
		}
		// Ensure function parameters match the regex pattern
		Set<String> invalidParameters = node.getParameters().stream()
				.filter(param -> StringUtils.isNotBlank(param)
						&& !REGO_VARIABLE_PATTERN.matcher(param).matches())
				.collect(Collectors.toSet());
		if (!invalidParameters.isEmpty()) {
			report.addViolation(this, node, MessageFormat
					.format("The following parameters for the base statement function ''{0}'' are invalid: ''{1}'' Function parameters {2}",
							node.getName(), String.join("', '", invalidParameters),
							REGO_VARIABLE_MESSAGE));
		}
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
