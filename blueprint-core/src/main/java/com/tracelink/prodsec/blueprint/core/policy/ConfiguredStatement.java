package com.tracelink.prodsec.blueprint.core.policy;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.report.PolicyBuilderReport;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatement;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementArgument;
import com.tracelink.prodsec.blueprint.core.statement.BaseStatementFunction;
import com.tracelink.prodsec.blueprint.core.visitor.AbstractPolicyNode;
import com.tracelink.prodsec.blueprint.core.visitor.PolicyVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model for a single configured statement in a policy clause.
 *
 * @author mcool
 */
public class ConfiguredStatement extends AbstractPolicyNode {

	private PolicyClause parent;
	private int index;
	@NotNull(message = "Base statement cannot be null")
	private BaseStatement baseStatement;
	private boolean negated;
	@NotNull(message = "Argument values list cannot be null")
	private List<@NotBlank(message = "Argument values cannot be blank") String> argumentValues = new ArrayList<>();

	public void setParent(PolicyClause parent) {
		this.parent = parent;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public BaseStatement getBaseStatement() {
		return baseStatement;
	}

	public void setBaseStatement(BaseStatement baseStatement) {
		// Set parent
		baseStatement.setParent(this);
		this.baseStatement = baseStatement;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConfiguredStatement other = (ConfiguredStatement) o;
		return negated == other.negated && Objects.equals(baseStatement, other.baseStatement)
				&& areArgumentsEqual(other);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (negated ? 1 : 0);
		hash = 31 * hash + (baseStatement == null ? 0 : baseStatement.hashCode());
		hash = 31 * hash + (argumentValues == null ? 0 : generateArgumentsHashCode());
		return hash;
	}

	/**
	 * Generate the same hash code as {@link #hashCode()} but does not compute anything for the
	 * {@link #negated} flag
	 *
	 * @return the hash code for this statement without factoring in negated
	 */
	public int generateHashCodeWithoutNegation() {
		int hash = 7;
		hash = 31 * hash + (baseStatement == null ? 0 : baseStatement.hashCode());
		hash = 31 * hash + (argumentValues == null ? 0 : generateArgumentsHashCode());
		return hash;
	}

	Set<BaseStatementFunction> getAllDependentFunctions() {
		Set<BaseStatementFunction> dependentFunctions = new HashSet<>();
		dependentFunctions.add(baseStatement.getFunction());
		dependentFunctions.addAll(baseStatement.getFunction().getAllDependencies());
		return dependentFunctions;
	}

	/**
	 * Tests whether this statement has equal arguments to another based on number of arguments, and
	 * individual argument values matching in content
	 *
	 * @param other the other statement to compare to
	 * @return true if this statement contains the same arguments as the supplied statement
	 */
	public boolean areArgumentsEqual(ConfiguredStatement other) {
		// If there is no info about the arguments, do a simple equals on argument values
		if (baseStatement == null || baseStatement.getArguments() == null) {
			return Objects.equals(argumentValues, other.getArgumentValues());
		}
		// Both should have the same number of arguments
		if (argumentValues.size() != other.getArgumentValues().size()) {
			return false;
		}
		// Matching arguments should be equal
		return IntStream
				.range(0, Math.min(argumentValues.size(), baseStatement.getArguments().size()))
				.allMatch(i -> isArgumentEqual(baseStatement.getArguments().get(i),
						getArgumentValues().get(i),
						other.getArgumentValues().get(i)));
	}

	private boolean isArgumentEqual(BaseStatementArgument argument, String value1, String value2) {
		if (argument.getType().isArrayType() && argument.isArrayUnordered()) {
			// If the arg is an array type and does not have ordered items, compare items
			return areArrayItemsEqual(argument.getType(), value1, value2);
		} else {
			// A direct equals call will work
			return Objects.equals(value1, value2);
		}
	}

	private boolean areArrayItemsEqual(ArgumentType argumentType, String value1, String value2) {
		try {
			// Get the list of items for each argument
			List<?> items1 = argumentType.getArrayItems(value1);
			List<?> items2 = argumentType.getArrayItems(value2);
			// Map the counts of each unique item
			Map<?, Long> itemCounts1 = items1.stream()
					.collect(Collectors.groupingBy(e -> e, Collectors.counting()));
			Map<?, Long> itemCounts2 = items2.stream()
					.collect(Collectors.groupingBy(e -> e, Collectors.counting()));
			// Determine whether the counts maps are equivalent
			return Objects.equals(itemCounts1, itemCounts2);
		} catch (NumberFormatException e) {
			// If we cannot read argument items then return direct equals
			return Objects.equals(value1, value2);
		}
	}

	private int generateArgumentsHashCode() {
		// If there is no info about the arguments, do a simple hash on argument values
		if (baseStatement == null || baseStatement.getArguments() == null) {
			return Objects.hash(argumentValues);
		}
		int hash = 7;
		hash = 31 * hash + argumentValues.size();
		for (int i = 0; i < Math.min(argumentValues.size(), baseStatement.getArguments().size());
				i++) {
			hash = 31 * hash + argumentHashCode(baseStatement.getArguments().get(i),
					argumentValues.get(i));
		}
		return hash;
	}

	private int argumentHashCode(BaseStatementArgument requiredArg, String arg) {
		if (requiredArg.getType().isArrayType() && requiredArg.isArrayUnordered()) {
			// If the arg is an array type and does not have ordered items, compare items
			return argumentItemsHashCode(requiredArg.getType(), arg);
		} else {
			// A direct equals call will work
			return arg == null ? 0 : arg.hashCode();
		}
	}

	private int argumentItemsHashCode(ArgumentType argumentType, String arg) {
		try {
			// Get the list of items for each argument
			List<?> items = argumentType.getArrayItems(arg);
			// Map the counts of each unique item
			Map<?, Long> itemCounts = items.stream()
					.collect(Collectors.groupingBy(e -> e, Collectors.counting()));
			// Return hash code of counts map
			return itemCounts.hashCode();
		} catch (NumberFormatException e) {
			// If we cannot read argument items then return direct hash
			return Objects.hash(arg);
		}
	}

	@Override
	public Iterable<? extends AbstractPolicyNode> children() {
		return Collections.singletonList(baseStatement);
	}

	@Override
	public PolicyBuilderReport accept(PolicyVisitor visitor, PolicyBuilderReport report) {
		return visitor.visit(this, report);
	}

	@Override
	public PolicyClause getParent() {
		return parent;
	}

	@Override
	protected String getLocationIdentifier() {
		return "statements[" + index + "]";
	}

}
