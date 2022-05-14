package com.tracelink.prodsec.blueprint.app.policy;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO for a policy. Used to transfer data from the UI and perform basic
 * validation.
 *
 * @author mcool
 */
public class PolicyDto {

	@NotEmpty(message = "A policy must have at least one clause")
	private List<@NotNull(message = "Policy clauses cannot be null") @Valid PolicyClauseDto> clauses = new ArrayList<>();
	@NotBlank(message = "A policy must have a type")
	private String policyType;

	public List<PolicyClauseDto> getClauses() {
		return clauses;
	}

	public void setClauses(List<PolicyClauseDto> clauses) {
		this.clauses = clauses;
	}

	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
}
