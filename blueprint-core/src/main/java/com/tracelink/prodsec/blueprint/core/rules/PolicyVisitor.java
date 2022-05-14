package com.tracelink.prodsec.blueprint.core.rules;

import com.tracelink.prodsec.blueprint.core.policy.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;

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
	public PolicyReport visit(AbstractPolicyNode node, PolicyReport report) {
		return node.childrenAccept(this, report);
	}

	/**
	 * Visit a Policy Node
	 *
	 * @param node   the Policy Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyReport visit(Policy node, PolicyReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Policy Clause Node
	 *
	 * @param node   the Policy Clause Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyReport visit(PolicyClause node, PolicyReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

	/**
	 * Visit a Configured Statement Node
	 *
	 * @param node   the Configured Statement Node
	 * @param report the report object detailing what has happened so far
	 * @return a report detailing what happened after this visit.
	 */
	public PolicyReport visit(ConfiguredStatement node, PolicyReport report) {
		return visit((AbstractPolicyNode) node, report);
	}

}
