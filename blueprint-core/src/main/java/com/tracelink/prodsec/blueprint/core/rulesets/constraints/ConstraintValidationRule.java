package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

/**
 * Detects basic data problems in a policy such as missing, incomplete, or unmatched data
 *
 * @author csmith
 */
class ConstraintValidationRule extends AbstractPolicyRule {

	private static final Validator VALIDATOR = Validation.byDefaultProvider().configure()
			.messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory()
			.getValidator();

	protected ConstraintValidationRule() {
		super("Constraint Validation Rule");
	}

	@Override
	public PolicyBuilderReport visit(Policy node, PolicyBuilderReport report) {
		checkNodeConstraints(node, report);
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		checkNodeConstraints(node, report);
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		checkNodeConstraints(node, report);
		return super.visit(node, report);
	}

	void checkNodeConstraints(AbstractPolicyNode node, PolicyBuilderReport report,
			Class<?>... groups) {
		// Validate policy for basic constraints
		Set<ConstraintViolation<AbstractPolicyNode>> violations = VALIDATOR.validate(node, groups);
		// Construct policy rule violation from each constraint violation
		violations.forEach(violation -> {
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
			report.addViolation(this, node, node.getLocation() + "." + propertyBuilder,
					violation.getMessage());
		});
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
