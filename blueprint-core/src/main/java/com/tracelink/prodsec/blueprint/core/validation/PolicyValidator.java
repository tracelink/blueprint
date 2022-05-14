package com.tracelink.prodsec.blueprint.core.validation;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyVisitor;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;

/**
 * Detects basic data problems in a policy such as missing, incomplete, or
 * unmatched data
 *
 * @author csmith
 */
class PolicyValidator extends PolicyVisitor {

	private static final Validator VALIDATOR = Validation.byDefaultProvider().configure()
		.messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory()
		.getValidator();

	@Override
	public PolicyReport visit(Policy node, PolicyReport report) {
		checkPolicyConstraints(node, report);
		return super.visit(node, report);
	}

	private void checkPolicyConstraints(Policy node, PolicyReport report) {
		// Validate policy for basic constraints
		Set<ConstraintViolation<Policy>> violations = VALIDATOR.validate(node);
		// Construct policy rule violation from each constraint violation
		for (ConstraintViolation<Policy> violation : violations) {
			Path path = violation.getPropertyPath();
			StringBuilder propertyBuilder = new StringBuilder();
			path.forEach(pathNode -> {
				if (pathNode.getKind().equals(ElementKind.PROPERTY)) {
					if (pathNode.isInIterable()) {
						propertyBuilder.append("[");
						propertyBuilder.append(pathNode.getIndex());
						propertyBuilder.append("]");
					}
					if (propertyBuilder.length() > 0) {
						propertyBuilder.append(".");
					}
					propertyBuilder.append(pathNode.getName());
				}
			});
			report.addErrorLocation(propertyBuilder.toString(), violation.getMessage());
		}
	}

	@Override
	public PolicyReport visit(ConfiguredStatement node, PolicyReport report) {
		checkStatementArgumentsExist(node, report);
		checkStatementArgumentCounts(node, report);
		checkStatementArgumentValidity(node, report);
		return super.visit(node, report);
	}

	private void checkStatementArgumentsExist(ConfiguredStatement node, PolicyReport report) {
		BaseStatement base = node.getBaseStatement();
		if (base == null || base.getArguments() == null) {
			report.addErrorNode(node,
				"Cannot validate arguments because there is no argument info in the base statement");
		}
	}

	private void checkStatementArgumentCounts(ConfiguredStatement node, PolicyReport report) {
		BaseStatement base = node.getBaseStatement();
		if (base != null && base.getArguments() != null) {
			if (base.getArguments().size() != node.getArgumentValues().size()) {
				report.addErrorNode(node,
					MessageFormat.format(
						"The base statement {0} requires {1} arguments but {2} arguments were provided",
						base.getName(), base.getArguments().size(),
						node.getArgumentValues().size()));
			}
		}
	}

	// Check that each configured argument is valid
	private void checkStatementArgumentValidity(ConfiguredStatement node, PolicyReport report) {
		BaseStatement base = node.getBaseStatement();
		if (base != null && base.getArguments() != null && base.getArguments().size() == node
			.getArgumentValues().size()) {
			IntStream.range(0, base.getArguments().size())
				.forEach(argIndex -> validateConfiguredArgument(base.getArguments().get(argIndex),
					node.getArgumentValues().get(argIndex), report, node.getParent().getIndex(),
					node.getIndex(), argIndex));
		}
	}

	private void validateConfiguredArgument(BaseStatementArgument requiredArgument,
		String configuredArgument,
		PolicyReport report, int clauseIndex, int stmtIndex, int argIndex) {
		// Check that the argument matches the required type
		if (!requiredArgument.getType()
			.matchesArgument(configuredArgument, requiredArgument.hasUniqueItems())) {
			report.addErrorLocation(
				MessageFormat
					.format("clauses[{0}].statements[{1}].argumentValues[{2}]", clauseIndex,
						stmtIndex,
						argIndex),
				MessageFormat.format("The argument {0} must be a {1}",
					configuredArgument, requiredArgument.getType().getDisplayName()));
		}
		// If there are enum values, make sure that the values match
		if (requiredArgument.getEnumValues() != null) {
			List<String> items;
			if (requiredArgument.getType().isArrayType()) {
				try {
					items = requiredArgument.getType().getArrayItems(configuredArgument).stream()
						.map(Object::toString)
						.collect(Collectors.toList());
				} catch (NumberFormatException e) {
					items = Collections.emptyList();
				}
			} else {
				items = Collections.singletonList(configuredArgument);
			}
			if (items.isEmpty()) {
				report.addErrorLocation(
					MessageFormat
						.format("clauses[{0}].statements[{1}].argumentValues[{2}]", clauseIndex,
							stmtIndex,
							argIndex),
					MessageFormat
						.format("Cannot get array items of type {0} from configured argument {1}",
							requiredArgument.getType().getDisplayName(), configuredArgument));
				return;
			}
			if (!requiredArgument.getEnumValues().containsAll(items)) {
				report.addErrorLocation(
					MessageFormat.format("clauses[{0}].statements[{1}].argumentValues[{2}]",
						clauseIndex, stmtIndex, argIndex), MessageFormat
						.format("The argument {0} must match the enumerated values {1}",
							configuredArgument, requiredArgument.getEnumValues()));
			}
		}
	}

}
