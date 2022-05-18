package com.tracelink.prodsec.blueprint.core.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The state of a versioned policy element. Valid states are draft, released and deprecated.
 *
 * @author mcool
 */
public enum PolicyElementState {
	DRAFT("Draft"), RELEASED("Released"), DEPRECATED("Deprecated");

	private final String name;

	PolicyElementState(String name) {
		this.name = name;
	}

	@JsonValue
	public String getName() {
		return name;
	}

	@JsonCreator
	public static PolicyElementState getStateForName(String name) {
		for (PolicyElementState state : PolicyElementState.values()) {
			if (state.getName().equalsIgnoreCase(name)) {
				return state;
			}
		}
		return null;
	}
}
