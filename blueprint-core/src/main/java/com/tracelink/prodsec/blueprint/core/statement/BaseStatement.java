package com.tracelink.prodsec.blueprint.core.statement;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractRootNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Model Object for a Base Statement
 *
 * @author mcool
 */
public class BaseStatement extends AbstractRootNode {

	private ConfiguredStatement parent;
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
	@NotEmpty(message = "A base statement must be valid for at least one policy type")
	private Set<@NotBlank(message = "Policy types cannot be blank") String> policyTypes;
	@NotNull(message = "Negation cannot be null")
	private boolean negationAllowed;
	@NotNull(message = "Evaluated function cannot be null")
	private BaseStatementFunction function;
	@NotNull(message = "Arguments list cannot be null")
	private List<@NotNull(message = "Arguments cannot be null") @Valid BaseStatementArgument> arguments = new ArrayList<>();

	public void setParent(ConfiguredStatement parent) {
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

	public boolean isNegationAllowed() {
		return negationAllowed;
	}

	public void setNegationAllowed(boolean negationAllowed) {
		this.negationAllowed = negationAllowed;
	}

	public BaseStatementFunction getFunction() {
		return function;
	}

	public void setFunction(BaseStatementFunction function) {
		// Set parent
		function.setParent(this);
		this.function = function;
	}

	public List<BaseStatementArgument> getArguments() {
		return arguments;
	}

	public void setArguments(List<BaseStatementArgument> arguments) {
		// Set parent and index
		arguments.forEach(argument -> argument.setParent(this));
		IntStream.range(0, arguments.size()).forEach(idx -> arguments.get(idx).setIndex(idx));
		this.arguments = arguments;
	}

	public String getVersionedName() {
		return name + ":" + version;
	}

	@Override
	public Iterable<? extends AbstractPolicyNode> children() {
		List<AbstractPolicyNode> children = new ArrayList<>();
		children.add(function);
		if (arguments != null) {
			children.addAll(arguments);
		}
		return children;
	}

	@Override
	public ConfiguredStatement getParent() {
		return parent;
	}

	@Override
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	protected String getLocationIdentifier() {
		return "baseStatement";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseStatement that = (BaseStatement) o;
		return negationAllowed == that.negationAllowed && Objects.equals(name, that.name)
				&& Objects.equals(description, that.description) && Objects
				.equals(policyTypes, that.policyTypes) && Objects
				.equals(function, that.function) && Objects.equals(arguments, that.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, negationAllowed, policyTypes, function, arguments);
	}
}
