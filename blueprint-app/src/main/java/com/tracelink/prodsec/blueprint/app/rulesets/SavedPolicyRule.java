package com.tracelink.prodsec.blueprint.app.rulesets;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;

/**
 * Identifies missing or invalid values for fields that are required to save a policy to the
 * database.
 *
 * @author mcool
 */
public class SavedPolicyRule extends AbstractPolicyRule {

	private static final Pattern ALPHABET_SPACE_PATTERN = Pattern
			.compile("^[A-Z][A-Za-z]*( [A-Z][A-Za-z]*)*$");
	private static final String ALPHABET_SPACE_MESSAGE = "must only contain letters and spaces, with the first letter of each word capitalized";

	protected SavedPolicyRule() {
		super("Saved Policy Rule");
	}


	@Override
	public PolicyBuilderReport visit(Policy node, PolicyBuilderReport report) {
		// Ensure policy name and author are not blank
		if (StringUtils.isBlank(node.getName())) {
			report.addViolation(this, node, node.getLocation() + ".name", "Name cannot be blank");
		}
		if (StringUtils.isBlank(node.getAuthor())) {
			report.addViolation(this, node, node.getLocation() + ".author",
					"Author cannot be blank");
		}
		// Ensure policy name matches the regex pattern
		if (node.getName() != null && !ALPHABET_SPACE_PATTERN.matcher(node.getName()).matches()) {
			report.addViolation(this, node, node.getLocation() + ".name", MessageFormat
					.format("The policy name ''{0}'' is invalid. Policy names {1}",
							node.getName(), ALPHABET_SPACE_MESSAGE));
		}
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
