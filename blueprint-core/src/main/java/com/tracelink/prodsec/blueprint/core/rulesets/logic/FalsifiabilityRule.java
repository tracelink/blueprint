package com.tracelink.prodsec.blueprint.core.rulesets.logic;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.Policy;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;
import com.tracelink.prodsec.blueprint.core.rules.AbstractPolicyRule;
import com.tracelink.prodsec.blueprint.core.rules.PolicyReport;
import com.tracelink.prodsec.blueprint.core.rules.RuleSeverity;

/**
 * Detects policies that are configured so that any boolean input to its statements always results
 * in
 * an evaluation of true.
 * <p>
 * This protects policies against being mistakenly created to allow all.
 * <p>
 * Formally, for a Policy P, containing Clauses C1, C2,...Cn, where each clause contains Statements
 * S1, S2,...Sn, this rule evaluates all boolean inputs to Statements such that to policy overall
 * evaluates to true. This logic is based on the fact that a policy can be written as
 * <pre>P := C1 | C2 | ... | Cn</pre> where <pre>C := S1 &amp; S2 &amp; ... &amp; Sn</pre> or <pre>P := (S1 &amp;
 * S2) | (S3 &amp; S4) | ...</pre>
 * <p>
 * In this case, Clauses C1-n and Statements 1-n can be exact copies of their respective values and
 * treated as the same value, and also Statements may be negated by their definition.
 * <p>
 * For a simple case, a Policy is made up of Clauses C1 and C2; C1 contains Statement S1; C2
 * contains the negated S1. The resulting boolean logic is P:=(S1) OR (&#172; S2) For every boolean
 * input to S1, the Policy will return true because:
 * <pre>
 *    S1   |   C1   |   C2
 * --------------------------
 *   true  |  true  |  false
 *   false |  false |  true
 * </pre>
 *
 * @author csmith
 */
public class FalsifiabilityRule extends AbstractPolicyRule {

	/*
	 * An assignment maps the integer hash code of a configured statement to its truth-table equivalent
	 */
	private LinkedHashMap<Integer, Boolean> assignments = new LinkedHashMap<>();

	public FalsifiabilityRule() {
		super("Falsifiability Rule");
	}

	@Override
	public RuleSeverity getSeverity() {
		return RuleSeverity.HIGH;
	}

	@Override
	public PolicyReport visit(Policy node, PolicyReport report) {
		super.visit(node, report);
		if (isFalsifiable(node)) {
			report.addViolation(this, node,
				"This policy is unsatisfiable because at least one clause can be true for every given input");
		}
		return report;
	}

	@Override
	public PolicyReport visit(PolicyClause node, PolicyReport report) {
		for (ConfiguredStatement stmt : node.getStatements()) {
			int code = stmt.generateHashCodeWithoutNegation();
			assignments.putIfAbsent(code, false);
		}
		return super.visit(node, report);
	}

	/*
	 * true if at least 1 clause is true in a policy for every combination of boolean inputs
	 */
	private boolean isFalsifiable(Policy policy) {
		do {
			boolean atLeast1TrueClause = clausesHaveTrueCase(policy);

			// If for any assignment 0 clauses are true, then we can stop solving entirely, since all
			// assignments must be true
			if (!atLeast1TrueClause) {
				return false;
			}

		} while (nextAssignment());

		return true;
	}

	/*
	 * true if any clause evaluates to true, false if all clauses evaluate to false
	 */
	private boolean clausesHaveTrueCase(Policy policy) {
		for (PolicyClause clause : policy.getClauses()) {
			boolean foundTrueClauseCase = allStatementsAreTrue(clause);

			// If any clause is true, we can skip all others for this assignment since clauses
			// are OR'd together
			if (foundTrueClauseCase) {
				return true;
			}
		}
		return false;
	}

	/*
	 * false if any statement evaluates to false, true if all statements evaluate to true
	 */
	private boolean allStatementsAreTrue(PolicyClause clause) {
		for (ConfiguredStatement statement : clause.getStatements()) {
			int code = statement.generateHashCodeWithoutNegation();
			boolean assignment = assignments.get(code);

			//if the statement is negated, invert the assignment
			boolean statementCase = statement.isNegated() != assignment;

			// If any statement is false, then this clause can be skipped because
			// all statements are AND'd together
			if (!statementCase) {
				return false;
			}
		}
		return true;
	}

	/*
	 * NOTE: This has side-effects. The method sets the next boolean configuration on the
	 * assignments map and also returns true if the assignments continue and false if the search-
	 * space is finished
	 */
	private boolean nextAssignment() {
		//From back to front, search for the next false bit
		for (Entry<Integer, Boolean> entry : assignments.entrySet()) {
			//Found a false, flip it to true and return since the assignments are now correct
			if (!entry.getValue()) {
				assignments.put(entry.getKey(), true);
				return true;
			}
			//Otherwise flip this back to false
			else {
				assignments.put(entry.getKey(), false);
			}
		}

		return false;
	}
}
