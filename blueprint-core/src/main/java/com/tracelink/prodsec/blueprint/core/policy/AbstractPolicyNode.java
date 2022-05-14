package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.PolicyVisitor;

/**
 * Top-level policy node to handle common accessors for all node implementations.
 *
 * This is used to handle node traversal and visitor patterns
 *
 * @author csmith
 */
public abstract class AbstractPolicyNode {

	private Policy root = null;

	/**
	 * Return an iterable of the children of this node. This iterable must be
	 * ordered so that subsequent calls call the underlying
	 * nodes in the same order
	 *
	 * @return an ordered iterable of child nodes
	 */
	public abstract Iterable<? extends AbstractPolicyNode> children();

	/**
	 * Return the parent of this node, or null if this is the root
	 * {@linkplain Policy} node
	 *
	 * @return the parent of this node, or null if this is the root
	 */
	public abstract AbstractPolicyNode getParent();

	/**
	 * Walk through all children of this node in order and dispatch the visitor to
	 * it. If the node has no or null children from the {@link #children()} method,
	 * then this immediately returns the supplied report
	 *
	 * @param visitor the visitor to dispatch to the child nodes
	 * @param report  a policy report for the child nodes to use
	 * @return the report object after possibly being updated by children
	 */
	public PolicyReport childrenAccept(PolicyVisitor visitor, PolicyReport report) {
		Iterable<? extends AbstractPolicyNode> children = children();
		if (children != null) {
			for (AbstractPolicyNode child : children) {
				report = child.accept(visitor, report);
			}
		}
		return report;
	}

	/**
	 * The second dispatch where this node will dispatch itself to the visitor.
	 *
	 * @param visitor the visitor object to dispatch to
	 * @param report  the report object to collect information
	 * @return the report object
	 */
	public abstract PolicyReport accept(PolicyVisitor visitor, PolicyReport report);

	/**
	 * Lazily get the root Policy for this node
	 *
	 * @return the root Policy
	 */
	public Policy getRoot() {
		if (root == null) {
			root = getParent().getRoot();
		}
		return root;
	}

	/**
	 * The location identifier is a marker that a node uses to reference itself.
	 * This should take on a jsonpath-like structure denoting both the nodes type as
	 * well as its relative location to its parent
	 *
	 * @return the location of this specific node
	 */
	protected abstract String getLocationIdentifier();

	/**
	 * get the location with parent context of this node
	 *
	 * @return the contextual location of this node
	 */
	public String getLocation() {
		if (getParent() == null) {
			return getLocationIdentifier();
		}
		return getParent().getLocationIdentifier() + "." + getLocationIdentifier();
	}

}
