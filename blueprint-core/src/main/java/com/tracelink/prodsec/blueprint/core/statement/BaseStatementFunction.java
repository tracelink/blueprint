package com.tracelink.prodsec.blueprint.core.statement;

import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Model Object for a base statement function.
 *
 * @author mcool
 */
public class BaseStatementFunction extends AbstractRootNode implements
		Comparable<BaseStatementFunction> {

	private BaseStatement parent;
	@NotBlank(message = "Name cannot be blank")
	private String name;
	@NotBlank(message = "Author cannot be blank")
	private String author;
	@NotNull(message = "Version cannot be null")
	private int version;
	@NotNull(message = "State cannot be null")
	private PolicyElementState state;
	@NotBlank(message = "Description cannot be blank")
	private String description;
	@NotEmpty(message = "A function must be valid for at least one policy type")
	private Set<@NotBlank(message = "Policy types cannot be blank") String> policyTypes;
	private List<@NotBlank(message = "Parameters cannot be blank") String> parameters;
	@NotBlank(message = "Expression cannot be blank")
	private String expression;
	@NotNull(message = "Dependencies list cannot be null")
	private Set<@NotNull(message = "Dependency cannot be null") BaseStatementFunction> dependencies;

	public void setParent(BaseStatement parent) {
		this.parent = parent;
	}

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

	public Set<BaseStatementFunction> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<BaseStatementFunction> dependencies) {
		this.dependencies = dependencies;
	}

	public String getVersionedName() {
		return name + ":" + version;
	}

	@Override
	public Iterable<? extends AbstractPolicyNode> children() {
		return null;
	}

	@Override
	public BaseStatement getParent() {
		return parent;
	}

	@Override
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	protected String getLocationIdentifier() {
		return "function";
	}

	@Override
	public int compareTo(BaseStatementFunction o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseStatementFunction function = (BaseStatementFunction) o;
		return Objects.equals(name, function.name)
				&& Objects.equals(description, function.description)
				&& Objects.equals(policyTypes, function.policyTypes)
				&& Objects.equals(parameters, function.parameters)
				&& Objects.equals(expression, function.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, policyTypes, parameters, expression);
	}

	public Set<BaseStatementFunction> getAllDependencies() {
		if (dependencies == null) {
			return Collections.emptySet();
		}
		Set<BaseStatementFunction> allDependencies = new HashSet<>(dependencies);
		allDependencies.addAll(dependencies.stream().map(BaseStatementFunction::getAllDependencies)
				.flatMap(Set::stream).collect(Collectors.toSet()));
		return allDependencies;
	}
}
