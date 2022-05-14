package com.tracelink.prodsec.blueprint.app.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementImportException;
import com.tracelink.prodsec.blueprint.app.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementRepository;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

@RunWith(SpringRunner.class)
public class BaseStatementServiceTest {

	@MockBean
	private BaseStatementRepository baseStatementRepository;
	@MockBean
	private PolicyTypeRepository policyTypeRepository;
	@MockBean
	private BaseStatementFunctionService functionService;
	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(BaseStatementService.class);

	private BaseStatementService baseStatementService;

	@Before
	public void setup() {
		baseStatementService = new BaseStatementService(baseStatementRepository,
				policyTypeRepository, functionService);
	}

	@Test
	public void testGetBaseStatement() {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		BDDMockito.when(baseStatementRepository.findByName(BDDMockito.anyString()))
				.thenReturn(baseStatement);

		Assert.assertEquals(baseStatement, baseStatementService.getBaseStatement("foo"));
	}

	@Test
	public void testGetBaseStatementsForPolicyTypeUnknownPolicyType() {
		Map<String, BaseStatementDto> baseStatements = baseStatementService
				.getBaseStatementsForPolicyType(
						"System");
		Assert.assertEquals(0, baseStatements.size());
	}

	@Test
	public void testGetBaseStatementsForPolicyType() {
		BaseStatementEntity baseStatement = BDDMockito.mock(BaseStatementEntity.class);
		BaseStatementDto dto = new BaseStatementDto();
		dto.setName("Base Statement");
		BDDMockito.when(baseStatement.toDto()).thenReturn(dto);
		BDDMockito.when(policyTypeRepository.findByName(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity());
		BDDMockito.when(baseStatementRepository
				.findByPolicyTypesInOrderByNameAsc(BDDMockito.anySet()))
				.thenReturn(Collections.singletonList(baseStatement));

		Map<String, BaseStatementDto> baseStatements = baseStatementService
				.getBaseStatementsForPolicyType(
						"System");
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertTrue(baseStatements.containsKey("Base Statement"));
		Assert.assertEquals(dto, baseStatements.get("Base Statement"));
	}

	@Test
	public void testImportBaseStatementsBaseStatementInvalid() {
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base_Statement");
		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement),
					Collections.emptyList());
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals("At least one imported base statement is invalid", e.getMessage());
		}

		Assert.assertEquals(3, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base_Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertTrue(loggerRule.getFormattedMessages().get(1).contains(
				"The base statement 'Base_Statement' has the following validation errors:"));
		Assert.assertEquals(
				"The base statement name 'Base_Statement' is invalid. Names must only contain letters and spaces, with no whitespace at the beginning or end",
				loggerRule.getFormattedMessages().get(2));
	}

	@Test
	public void testImportBaseStatementsBaseStatementsAreInvalid() {
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");

		try {
			baseStatementService.importBaseStatements(Arrays.asList(baseStatement, baseStatement),
					Collections.emptyList());
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals("The imported base statements are invalid", e.getMessage());
		}

		Assert.assertEquals(4, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(1));
		Assert.assertEquals(
				"The following base statement names are duplicated 'Base Statement'. Names must be unique",
				loggerRule.getFormattedMessages().get(2));
		Assert.assertEquals(
				"The following base statements have evaluated functions that are undefined: 'Base Statement': 'function_name'",
				loggerRule.getFormattedMessages().get(3));
	}

	@Test
	public void testImportBaseStatementsBaseStatementsArgumentsInvalid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("function_name");
		function.setParameters(Collections.emptyList());
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setConstant(true);
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement),
					Collections.singletonList(function));
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals("The imported base statement arguments are invalid",
					e.getMessage());
		}

		Assert.assertEquals(5, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating arguments for base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(1));
		Assert.assertTrue(loggerRule.getFormattedMessages().get(2).contains(
				"The base statement argument at index 0 has the following validation errors:"));
		Assert.assertEquals(
				"At least one argument for the base statement 'Base Statement' is invalid",
				loggerRule.getFormattedMessages().get(3));
		Assert.assertEquals(
				"The number of arguments defined for the base statement 'Base Statement' does not match the number of parameters for its evaluated function 'function_name'",
				loggerRule.getFormattedMessages().get(4));
	}

	@Test
	public void testImportBaseStatementsUnknownArgumentType() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("function_name");
		function.setParameters(Collections.singletonList("arg"));
		ArgumentType argumentType = BDDMockito.mock(ArgumentType.class);
		BDDMockito.when(argumentType.getName()).thenReturn("fooArray");
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setDescription("This is an argument");
		argument.setEnumValues(Set.of("foo", "2"));
		argument.setType(argumentType);
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement),
					Collections.singletonList(function));
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals("The imported base statement arguments are invalid",
					e.getMessage());
		}

		Assert.assertEquals(4, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating arguments for base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(1));
		Assert.assertEquals(
				"The base statement argument at index 0 has an unknown base type 'foo'",
				loggerRule.getFormattedMessages().get(2));
		Assert.assertEquals(
				"At least one argument for the base statement 'Base Statement' is invalid",
				loggerRule.getFormattedMessages().get(3));
	}

	@Test
	public void testImportBaseStatementsArgumentEnumValuesInvalid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("function_name");
		function.setParameters(Collections.singletonList("arg"));
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setDescription("This is an argument");
		argument.setEnumValues(Set.of("foo", "2"));
		argument.setType(ArgumentType.getTypeForName("integerArray"));
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement),
					Collections.singletonList(function));
			Assert.fail();
		} catch (BaseStatementImportException e) {
			Assert.assertEquals("The imported base statement arguments are invalid",
					e.getMessage());
		}

		Assert.assertEquals(4, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating arguments for base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(1));
		Assert.assertEquals(
				"The base statement argument at index 0 has enum values that do not match the type 'integer'",
				loggerRule.getFormattedMessages().get(2));
		Assert.assertEquals(
				"At least one argument for the base statement 'Base Statement' is invalid",
				loggerRule.getFormattedMessages().get(3));
	}

	@Test
	public void testImportBaseStatementsValid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("function_name");
		function.setPolicyTypes(Collections.singleton("System"));
		function.setParameters(Collections.singletonList("arg"));
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setDescription("This is an argument");
		argument.setType(ArgumentType.getTypeForName("integerArray"));
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		BaseStatementFunctionEntity functionEntity = new BaseStatementFunctionEntity();
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("System");
		functionEntity.setPolicyTypes(Collections.singleton(policyType));
		BDDMockito.when(functionService.importBaseStatementFunctions(BDDMockito.anyList()))
				.thenReturn(Collections.singletonMap("function_name", functionEntity));

		baseStatementService.importBaseStatements(Collections.singletonList(baseStatement),
				Collections.singletonList(function));

		ArgumentCaptor<Set> entityCaptor = ArgumentCaptor.forClass(Set.class);
		BDDMockito.verify(baseStatementRepository).saveAll(entityCaptor.capture());
		Set<BaseStatementEntity> baseStatements = (Set<BaseStatementEntity>) entityCaptor
				.getValue();
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertEquals(functionEntity, baseStatements.iterator().next().getFunction());

		Assert.assertEquals(2, loggerRule.getMessages().size());
		Assert.assertEquals("Validating base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(0));
		Assert.assertEquals("Validating arguments for base statement 'Base Statement'",
				loggerRule.getFormattedMessages().get(1));
	}

	@Test
	public void testGetPolicyTypes() {
		PolicyTypeEntity policyType1 = new PolicyTypeEntity();
		policyType1.setName("Foo");
		PolicyTypeEntity policyType2 = new PolicyTypeEntity();
		policyType2.setName("Bar");
		BDDMockito.when(policyTypeRepository.findAll())
				.thenReturn(Arrays.asList(policyType1, policyType2));
		Assert.assertEquals(Arrays.asList("Bar", "Foo"), baseStatementService.getPolicyTypes());
	}

	@Test
	public void testGetPolicyType() {
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("Foo");
		BDDMockito.when(policyTypeRepository.findByName(BDDMockito.anyString()))
				.thenReturn(policyType);
		Assert.assertEquals(policyType, baseStatementService.getPolicyType("Foo"));
	}

}
