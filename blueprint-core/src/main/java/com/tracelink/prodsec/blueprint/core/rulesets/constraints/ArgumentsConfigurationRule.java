package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Checks for configuration issues with arguments, such as an incorrect number of arguments or
 * arguments with the wrong type.
 *
 * @author mcool
 */
public class ArgumentsConfigurationRule extends AbstractPolicyRule {

	protected ArgumentsConfigurationRule() {
		super("Arguments Configuration Rule");
	}

	@Override
	public PolicyBuilderReport visit(ConfiguredStatement node, PolicyBuilderReport report) {
		if (correctConfiguredArgumentCounts(node, report)) {
			validateConfiguredArguments(node, report);
		}
		return super.visit(node, report);
	}

	private boolean correctConfiguredArgumentCounts(ConfiguredStatement node,
			PolicyBuilderReport report) {
		BaseStatement baseStatement = node.getBaseStatement();
		if (baseStatement != null && baseStatement.getArguments() != null
				&& node.getArgumentValues() != null) {
			// Ensure the statement has the correct number of arguments for the base statement
			if (baseStatement.getArguments().size() != node.getArgumentValues().size()) {
				report.addViolation(this, node,
						MessageFormat.format(
								"The base statement ''{0}'' requires {1} arguments but {2} arguments were provided",
								baseStatement.getName(), baseStatement.getArguments().size(),
								node.getArgumentValues().size()));
				return false;
			}
			return true;
		}
		return false;
	}

	private void validateConfiguredArguments(ConfiguredStatement node, PolicyBuilderReport report) {
		BaseStatement baseStatement = node.getBaseStatement();
		IntStream.range(0, baseStatement.getArguments().size()).forEach(i ->
				validateConfiguredArgument(baseStatement.getArguments().get(i),
						node.getArgumentValues().get(i), report, node, i));
	}

	private void validateConfiguredArgument(BaseStatementArgument baseStatementArgument,
			String argumentValue, PolicyBuilderReport report, ConfiguredStatement node, int index) {
		// Check that the argument matches the required type
		if (!baseStatementArgument.getType()
				.matchesArgument(argumentValue, baseStatementArgument.isArrayUnique())) {
			String message = "This argument must be a {0}" + (baseStatementArgument.isArrayUnique()
					? " with unique items" : "");
			report.addViolation(this, node, node.getLocation() + ".argumentValues[" + index + "]",
					MessageFormat.format(message,
							baseStatementArgument.getType().getDisplayName().toLowerCase()));
		}
		// If there are enum values, make sure that the values match
		if (baseStatementArgument.getEnumValues() != null && !baseStatementArgument.getEnumValues()
				.isEmpty()) {
			List<String> items;
			if (baseStatementArgument.getType().isArrayType()) {
				try {
					items = baseStatementArgument.getType()
							.getArrayItems(argumentValue)
							.stream()
							.map(Object::toString)
							.collect(Collectors.toList());
				} catch (NumberFormatException e) {
					items = Collections.emptyList();
				}
			} else {
				items = Collections.singletonList(argumentValue);
			}
			if (items.isEmpty()) {
				report.addViolation(this, node,
						node.getLocation() + ".argumentValues[" + index + "]", MessageFormat
								.format("Cannot get array items of type {0} from configured argument {1}",
										baseStatementArgument.getType().getDisplayName()
												.toLowerCase(), argumentValue));
				return;
			}
			if (!baseStatementArgument.getEnumValues().containsAll(items)) {
				report.addViolation(this, node,
						node.getLocation() + ".argumentValues[" + index + "]", MessageFormat
								.format("The argument {0} must match the enumerated values {1}",
										argumentValue, baseStatementArgument.getEnumValues()));
			}
		}
	}

	@Override
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		checkBaseStatementArgumentsCount(node, report);
		return super.visit(node, report);
	}

	private void checkBaseStatementArgumentsCount(BaseStatement node, PolicyBuilderReport report) {
		BaseStatementFunction function = node.getFunction();
		if (function != null && function.getParameters() != null && node.getArguments() != null) {
			// Ensure the base statement has the correct number of arguments for the evaluated function
			if (node.getArguments().size() != node.getFunction().getParameters().size()) {
				report.addViolation(this, node, MessageFormat
						.format("The number of arguments defined does not match the number of parameters for the function ''{0}''",
								node.getFunction().getName()));
			}
		}
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementArgument node, PolicyBuilderReport report) {
		checkParameters(node, report);
		checkEnumValues(node, report);
		return super.visit(node, report);
	}

	private void checkParameters(BaseStatementArgument node, PolicyBuilderReport report) {
		BaseStatement parent = node.getParent();
		if (parent != null && parent.getFunction() != null) {
			List<String> parameters = parent.getFunction().getParameters();
			if (parameters != null && node.getIndex() < parameters.size()) {
				if (!node.getParameter().equals(parameters.get(node.getIndex()))) {
					report.addViolation(this, node, node.getLocation() + ".parameter", MessageFormat
							.format("The parameter ''{0}'' does not match the function parameter ''{1}''",
									node.getParameter(), parameters.get(node.getIndex())));
				}
			}
		}
	}

	private void checkEnumValues(BaseStatementArgument node, PolicyBuilderReport report) {
		if (node.getType() != null && node.getEnumValues() != null) {
			// Ensure provided enum values match the given type
			ArgumentType argumentType = node.getType().getBaseType();
			if (!node.getEnumValues().stream()
					.allMatch(value -> argumentType.matchesArgument(value, false))) {
				report.addViolation(this, node, node.getLocation() + ".enumValues",
						MessageFormat.format("The enum values do not match the type ''{0}''",
								argumentType.getDisplayName().toLowerCase()));
			}
		}
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
