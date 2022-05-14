package com.tracelink.prodsec.blueprint.app.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementImportException;
import com.tracelink.prodsec.blueprint.app.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementFunctionRepository;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;

@RunWith(SpringRunner.class)
public class BaseStatementFunctionServiceTest {

	@MockBean
	private BaseStatementFunctionRepository functionRepository;
	@MockBean
	private PolicyTypeRepository policyTypeRepository;
	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(BaseStatementFunctionService.class);

	private BaseStatementFunctionService baseStatementFunctionService;

	@Before
	public void setup() {
		baseStatementFunctionService = new BaseStatementFunctionService(functionRepository,
				policyTypeRepository);
	}

	@Test
	public void testImportBaseStatementFunctionsFunctionInvalid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("Function Name");
		function.setParameters(Collections.singletonList("Bad Param"));

		try {
			baseStatementFunctionService
					.importBaseStatementFunctions(Collections.singletonList(function));
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals(
					"At least one imported base statement function is invalid. Check the log for more details",
					e.getMessage());
		}

		Assert.assertEquals(4, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement function 'Function Name'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertTrue(loggerRule.getFormattedMessages().get(1).contains(
				"The base statement function 'Function Name' has the following validation errors:"));
		Assert.assertEquals(
				"The base statement function name 'Function Name' is invalid. Function names must only contain lowercase letters, numbers and underscores",
				loggerRule.getFormattedMessages().get(2));
		Assert.assertEquals(
				"The following parameters for the base statement function 'Function Name' are invalid: 'Bad Param' Function parameters must only contain lowercase letters, numbers and underscores",
				loggerRule.getFormattedMessages().get(3));
	}

	@Test
	public void testImportBaseStatementFunctionsFunctionsAreInvalid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setDescription("This is a function");
		function.setName("function_name");
		function.setParameters(Collections.singletonList("param"));
		function.setExpression("1 == 1");
		function.setPolicyTypes(Collections.singleton("User"));
		function.setDependencies(Set.of("foobar", "function_name"));

		try {
			baseStatementFunctionService
					.importBaseStatementFunctions(Arrays.asList(function, function));
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals(
					"The imported base statement functions are invalid. Check the log for more details",
					e.getMessage());
		}

		Assert.assertEquals(5, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement function 'function_name'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating base statement function 'function_name'",
				loggerRule.getFormattedMessages().get(1));
		Assert.assertEquals(
				"The following base statement function names are duplicated 'function_name'. Function names must be unique",
				loggerRule.getFormattedMessages().get(2));
		Assert.assertEquals(
				"The following functions have dependencies that are undefined: 'function_name': 'foobar'",
				loggerRule.getFormattedMessages().get(3));
		Assert.assertEquals("The following functions have cyclic dependencies: 'function_name'",
				loggerRule.getFormattedMessages().get(4));
	}

	@Test
	public void testImportBaseStatementFunctionsValid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setDescription("This is a function");
		function.setName("function_name");
		function.setParameters(Collections.singletonList("param"));
		function.setExpression("1 == 1");
		function.setPolicyTypes(Collections.singleton("User"));
		function.setDependencies(Collections.singleton("dependency_name"));

		BaseStatementFunctionDto dependency = new BaseStatementFunctionDto();
		dependency.setDescription("This is a dependency");
		dependency.setName("dependency_name");
		dependency.setParameters(Collections.emptyList());
		dependency.setExpression("1 == 1");
		dependency.setPolicyTypes(Collections.singleton("User"));

		List<BaseStatementFunctionEntity> entities = Arrays
				.asList(function.toEntity(), dependency.toEntity());
		BDDMockito.when(functionRepository.saveAll(BDDMockito.anyIterable())).thenReturn(entities);

		baseStatementFunctionService
				.importBaseStatementFunctions(Arrays.asList(function, dependency));

		BDDMockito.verify(functionRepository, Mockito.times(2)).saveAll(BDDMockito.anyIterable());
		BDDMockito.verify(functionRepository).flush();
		Assert.assertEquals(entities.get(1), entities.get(0).getDependencies().iterator().next());

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement function 'function_name'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating base statement function 'dependency_name'",
				loggerRule.getFormattedMessages().get(1));
	}

}
