package com.tracelink.prodsec.blueprint.app.statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * DTO for a base statement function. Used to transfer data to the UI and to perform validation
 * during startup when base statement functions are imported. Contains a method to convert this
 * object to an entity.
 *
 * @author mcool
 */
public class BaseStatementFunctionDto {


	@NotBlank(message = "Name cannot be blank")
	private String name;
	private String author;
	@NotNull(message = "Version cannot be null")
	private int version;
	@NotNull(message = "State cannot be null")
	private PolicyElementState state;
	@NotBlank(message = "Description cannot be blank")
	private String description;
	@NotEmpty(message = "A function must be valid for at least one policy type")
	private Set<@NotBlank(message = "Policy types cannot be blank") String> policyTypes = new HashSet<>();
	private List<@NotBlank(message = "Parameters cannot be blank") String> parameters = new ArrayList<>();
	@NotBlank(message = "Expression cannot be blank")
	private String expression;
	private Set<@NotBlank(message = "Dependencies cannot be blank") String> dependencies = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public PolicyElementState getState() {
		return state;
	}

	public void setState(PolicyElementState state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getPolicyTypes() {
		return policyTypes;
	}

	public void setPolicyTypes(Set<String> policyTypes) {
		this.policyTypes = policyTypes;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<String> dependencies) {
		this.dependencies = dependencies;
	}
}
