package com.tracelink.prodsec.blueprint.core.visitor;

/**
 * Extension of the {@link AbstractPolicyNode} class that represents the root node of the node
 * traversal for visitor patterns, depending upon the context of the validation.
 *
 * @author mcool
 */
public abstract class AbstractRootNode extends AbstractPolicyNode {

	@Override
	public AbstractRootNode getRoot() {
		if (getParent() == null) {
			return this;
		} else {
			return getParent().getRoot();
		}
	}

}
