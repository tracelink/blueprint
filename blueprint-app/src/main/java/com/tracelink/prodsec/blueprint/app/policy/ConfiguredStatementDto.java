package com.tracelink.prodsec.blueprint.app.policy;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

/**
 * DTO for a single configured statement in a policy clause. Used to transfer data from the UI and
 * perform basic validation.
 *
 * @author mcool
 */
public class ConfiguredStatementDto {

	@NotBlank(message = "Base statement cannot be blank")
	private String baseStatementName;
	private boolean negated;
	private List<@NotBlank(message = "Argument values cannot be blank") String> argumentValues = new ArrayList<>();

	public String getBaseStatementName() {
		return baseStatementName;
	}

	public void setBaseStatementName(String baseStatementName) {
		this.baseStatementName = baseStatementName;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public List<String> getArgumentValues() {
		return argumentValues;
	}

	public void setArgumentValues(List<String> argumentValues) {
		this.argumentValues = argumentValues;
	}
}
