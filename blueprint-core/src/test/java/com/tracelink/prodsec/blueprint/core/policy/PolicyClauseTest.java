package com.tracelink.prodsec.blueprint.core.policy;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.blueprint.core.policy.ConfiguredStatement;
import com.tracelink.prodsec.blueprint.core.policy.PolicyClause;

public class PolicyClauseTest {


	@Test
	public void testEquals() {
		PolicyClause clause1 = new PolicyClause(null);
		Assert.assertEquals(clause1, clause1);

		Assert.assertFalse(clause1.equals(null));

		PolicyClause clause2 = new PolicyClause(null);
		Assert.assertEquals(clause1, clause2);
		ConfiguredStatement statement = new ConfiguredStatement(null);
		List<ConfiguredStatement> statementList = Collections.singletonList(statement);
		clause1.setStatements(statementList);
		clause2.setStatements(statementList);
		Assert.assertEquals(clause1, clause2);
	}

	@Test
	public void testHashCode() {
		PolicyClause clause1 = new PolicyClause(null);
		PolicyClause clause2 = new PolicyClause(null);
		ConfiguredStatement statement = new ConfiguredStatement(null);
		clause1.setStatements(Collections.singletonList(statement));
		clause2.setStatements(Collections.singletonList(statement));
		Assert.assertEquals(clause1.hashCode(), clause2.hashCode());
	}

}
