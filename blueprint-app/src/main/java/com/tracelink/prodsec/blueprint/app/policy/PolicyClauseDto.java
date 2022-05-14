package com.tracelink.prodsec.blueprint.app.policy;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO for a single policy clause in a policy. Used to transfer data from the UI and perform basic
 * validation.
 *
 * @author mcool
 */
public class PolicyClauseDto {

	@NotEmpty(message = "A policy clause must have at least one configured statement")
	private List<@NotNull(message = "Configured statements cannot be null") @Valid ConfiguredStatementDto> statements = new ArrayList<>();

	public List<ConfiguredStatementDto> getStatements() {
		return statements;
	}

	public void setStatements(List<ConfiguredStatementDto> statements) {
		this.statements = statements;
	}
}
