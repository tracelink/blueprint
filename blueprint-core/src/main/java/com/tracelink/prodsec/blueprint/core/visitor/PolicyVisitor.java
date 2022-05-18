package com.tracelink.prodsec.blueprint.core.visitor;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;

/**
 * Visitor that visits each Policy node and all of its children iteratively
 *
 * @author csmith
 */
public abstract class PolicyVisitor {

	/**
	 * Visit a generalized node used for dispatching this visitor to the children of the node
	 *
	 * @param node   the node whose child nodes should be visited
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(AbstractPolicyNode node, PolicyBuilderReport report) {
		return node.childrenAccept(this, report);
	}

	/**
	 * Visit a Policy Node
	 *
	 * @param node   the Policy Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(Policy node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Policy Clause Node
	 *
	 * @param node   the Policy Clause Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(PolicyClause node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Configured Statement Node
	 *
	 * @param node   the Configured Statement Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(ConfiguredStatement node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Base Statement Node
	 *
	 * @param node   the Base Statement Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(BaseStatement node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Base Statement Function Node
	 *
	 * @param node   the Base Statement Function Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(BaseStatementFunction node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Base Statement Argument Node
	 *
	 * @param node   the Base Statement Argument Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyBuilderReport visit(BaseStatementArgument node, PolicyBuilderReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

}
