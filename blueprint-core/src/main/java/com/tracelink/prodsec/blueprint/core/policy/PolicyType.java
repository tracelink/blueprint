package com.tracelink.prodsec.blueprint.core.policy;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the type of the policy. Used to determine which base statements and function can be
 * included in a policy.
 *
 * @author mcool
 */
public class PolicyType {

	private final String name;

	public PolicyType(String name) {
		this.name = name;
	}

	@JsonValue
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PolicyType type = (PolicyType) o;
		return Objects.equals(name, type.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
