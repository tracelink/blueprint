package com.tracelink.prodsec.blueprint.core.policy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PolicyClauseTest {


	@Test
	public void testEquals() {
		PolicyClause clause1 = new PolicyClause();
		Assert.assertEquals(clause1, clause1);

		Assert.assertFalse(clause1.equals(null));

		PolicyClause clause2 = new PolicyClause();
		Assert.assertEquals(clause1, clause2);
		ConfiguredStatement statement = new ConfiguredStatement();
		List<ConfiguredStatement> statementList = Collections.singletonList(statement);
		clause1.setStatements(statementList);
		clause2.setStatements(statementList);
		Assert.assertEquals(clause1, clause2);
	}

	@Test
	public void testHashCode() {
		PolicyClause clause1 = new PolicyClause();
		PolicyClause clause2 = new PolicyClause();
		ConfiguredStatement statement = new ConfiguredStatement();
		clause1.setStatements(Collections.singletonList(statement));
		clause2.setStatements(Collections.singletonList(statement));
		Assert.assertEquals(clause1.hashCode(), clause2.hashCode());
	}

	@Test
	public void testSetStatements() {
		PolicyClause clause = new PolicyClause();
		ConfiguredStatement statement0 = new ConfiguredStatement();
		ConfiguredStatement statement1 = new ConfiguredStatement();
		clause.setStatements(Arrays.asList(statement0, statement1));
		Assert.assertEquals(clause, statement0.getParent());
		Assert.assertEquals(clause, statement1.getParent());
		Assert.assertEquals(0, statement0.getIndex());
		Assert.assertEquals(1, statement1.getIndex());
	}

}
