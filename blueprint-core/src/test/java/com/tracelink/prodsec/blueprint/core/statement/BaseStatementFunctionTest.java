package com.tracelink.prodsec.blueprint.core.statement;

import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;

public class BaseStatementFunctionTest {

	@Test
	public void testEquals() {
		BaseStatementFunction function = new BaseStatementFunction();
		Assert.assertEquals(function, function);
		Assert.assertFalse(function.equals(new BaseStatementArgument()));
	}

	@Test
	public void testEqualsDifferentName() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n1");
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n2");
		Assert.assertNotEquals(function1, function2);
	}

	@Test
	public void testEqualsDifferentDescription() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n");
		function1.setDescription("d1");
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n");
		function2.setDescription("d2");
		Assert.assertNotEquals(function1, function2);
	}

	@Test
	public void testEqualsDifferentPolicyTypes() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n");
		function1.setDescription("d");
		function1.setPolicyTypes(Collections.singleton("Enterprise"));
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n");
		function2.setDescription("d");
		function2.setPolicyTypes(Collections.singleton("Multienterprise"));
		Assert.assertNotEquals(function1, function2);
	}

	@Test
	public void testEqualsDifferentParameters() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n");
		function1.setDescription("d");
		function1.setPolicyTypes(Collections.singleton("Enterprise"));
		function1.setParameters(Collections.singletonList("p1"));
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n");
		function2.setDescription("d");
		function2.setPolicyTypes(Collections.singleton("Enterprise"));
		function2.setParameters(Collections.singletonList("p2"));
		Assert.assertNotEquals(function1, function2);
	}

	@Test
	public void testEqualsDifferentExpression() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n");
		function1.setDescription("d");
		function1.setPolicyTypes(Collections.singleton("Enterprise"));
		function1.setParameters(Collections.singletonList("p"));
		function1.setExpression("e1");
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n");
		function2.setDescription("d");
		function2.setPolicyTypes(Collections.singleton("Enterprise"));
		function2.setParameters(Collections.singletonList("p"));
		function2.setExpression("e2");
		Assert.assertNotEquals(function1, function2);
	}

	@Test
	public void testEqualsAllSame() {
		BaseStatementFunction function1 = new BaseStatementFunction();
		function1.setName("n");
		function1.setDescription("d");
		function1.setPolicyTypes(Collections.singleton("Enterprise"));
		function1.setParameters(Collections.singletonList("p"));
		function1.setExpression("e");
		function1.setDependencies(Collections.emptySet());
		BaseStatementFunction function2 = new BaseStatementFunction();
		function2.setName("n");
		function2.setDescription("d");
		function2.setPolicyTypes(Collections.singleton("Enterprise"));
		function2.setParameters(Collections.singletonList("p"));
		function2.setExpression("e");
		function2.setDependencies(Collections.emptySet());
		Assert.assertEquals(function1, function2);
	}

	@Test
	public void testGetAllDependencies() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setName("f1");
		BaseStatementFunction dependency = BDDMockito.mock(BaseStatementFunction.class);
		BDDMockito.when(dependency.getAllDependencies()).thenReturn(Collections.emptySet());
		function.setDependencies(Collections.singleton(dependency));

		Set<BaseStatementFunction> dependencies = function.getAllDependencies();
		Assert.assertEquals(1, dependencies.size());
		Assert.assertTrue(dependencies.contains(dependency));
	}

	@Test
	public void testGetters() {
		BaseStatementFunction function = new BaseStatementFunction();
		function.setAuthor("author");
		function.setVersion(1);
		function.setDescription("description");
		function.setPolicyTypes(Collections.singleton("Enterprise"));
		function.setParameters(Collections.singletonList("foo"));
		function.setExpression("1 == 1");
		function.setDependencies(Collections.emptySet());

		Assert.assertEquals("author", function.getAuthor());
		Assert.assertEquals(1, function.getVersion());
		Assert.assertEquals("description", function.getDescription());
		Assert.assertEquals(1, function.getPolicyTypes().size());
		Assert.assertTrue(function.getPolicyTypes().contains("Enterprise"));
		Assert.assertEquals(1, function.getParameters().size());
		Assert.assertTrue(function.getParameters().contains("foo"));
		Assert.assertEquals("1 == 1", function.getExpression());
		Assert.assertTrue(function.getDependencies().isEmpty());
	}

	@Test
	public void testGetAllDependenciesNull() {
		Assert.assertEquals(Collections.emptySet(),
				new BaseStatementFunction().getAllDependencies());
	}

}
