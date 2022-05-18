package com.tracelink.prodsec.blueprint.core.rulesets.constraints;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.report.RuleSeverity;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyRule;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Identifies functions that have cyclic dependencies on other functions to prevent issues with Rego
 * generation.
 *
 * @author mcool
 */
public class CyclicDependenciesRule extends AbstractPolicyRule {

	protected CyclicDependenciesRule() {
		super("Cyclic Dependencies Rule");
	}

	@Override
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		// Evaluate each dependency separately
		for (BaseStatementFunction function : node.getDependencies()) {
			// Create set of visited functions and dependencies to visit
			Set<String> visited = new HashSet<>();
			visited.add(node.getName());
			Stack<BaseStatementFunction> toVisit = new Stack<>();
			toVisit.add(function);
			// DFS of dependencies to identify cycles
			while (!toVisit.empty()) {
				BaseStatementFunction dependency = toVisit.pop();
				if (!visited.add(dependency.getName())) {
					report.addViolation(this, node, MessageFormat.format(
							"The function ''{0}'' has a cyclic dependency on the function ''{1}''",
							node.getName(), dependency.getName()));
					break;
				}
				toVisit.addAll(dependency.getDependencies());
			}
		}
		return super.visit(node, report);
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.ERROR;
	}
}
