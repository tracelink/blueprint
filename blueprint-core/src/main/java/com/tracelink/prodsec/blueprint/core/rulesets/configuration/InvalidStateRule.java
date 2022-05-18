package com.tracelink.prodsec.blueprint.core.rulesets.configuration;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.text.MessageFormat;

/**
 * Identifies cases where base statements or functions that are not in the released state are
 * referenced in a policy node.
 *
 * @author mcool
 */
public class InvalidStateRule extends AbstractPolicyRule {


	protected InvalidStateRule() {
		super("Invalid State Rule");
	}

	@Override
	public PolicyBuilderReport visit(ConfiguredStatement node, PolicyBuilderReport report) {
		// Check for deprecated or draft base statement
		if (!node.getBaseStatement().getState().equals(PolicyElementState.RELEASED)) {
			String message = MessageFormat
					.format("The base statement ''{0}'' is in the {1} state. Please update to a released version or remove",
							node.getBaseStatement().getVersionedName(),
							node.getBaseStatement().getState().getName().toLowerCase());
			report.addViolation(this, node, node.getLocation() + ".baseStatement", message);
		}
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		// Check for deprecated or draft function
		if (!node.getFunction().getState().equals(PolicyElementState.RELEASED)) {
			String message = MessageFormat
					.format("The function ''{0}'' is in the {1} state. Please update to a released version or remove",
							node.getFunction().getVersionedName(),
							node.getFunction().getState().getName().toLowerCase());
			report.addViolation(this, node, node.getLocation() + ".function", message);
		}
		return super.visit(node, report);
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		// Check for deprecated or draft dependencies
		node.getDependencies().forEach(dependency -> {
			if (!dependency.getState().equals(PolicyElementState.RELEASED)) {
				String message = MessageFormat
						.format("The dependency ''{0}'' is in the {1} state. Please update to a released version or remove",
								dependency.getVersionedName(),
								dependency.getState().getName().toLowerCase());
				report.addViolation(this, node, node.getLocation() + ".dependencies", message);
			}
		});
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
