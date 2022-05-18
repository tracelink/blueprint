package com.tracelink.prodsec.blueprint.app.service;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
public class BaseStatementServiceTest {

	@MockBean
	private BaseStatementRepository baseStatementRepository;
	@MockBean
	private PolicyTypeService policyTypeService;
	@MockBean
	private BaseStatementFunctionService functionService;
	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(BaseStatementService.class);

	private BaseStatementService baseStatementService;

	@Before
	public void setup() {
		baseStatementService = new BaseStatementService(baseStatementRepository,
				policyTypeService, functionService);
	}

	@Test
	public void testGetBaseStatementNoVersion() throws Exception {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndStateOrderByVersionDesc(BDDMockito.anyString(), BDDMockito.any(
						PolicyElementState.class))).thenReturn(baseStatement);

		Assert.assertEquals(baseStatement, baseStatementService.getBaseStatement("foo"));
	}

	@Test
	public void testGetBaseStatementsForPolicyTypeUnknownPolicyType() {
		Map<String, BaseStatementDto> baseStatements = baseStatementService
				.getLatestBaseStatementsForPolicyType(new PolicyTypeEntity("System"));
		Assert.assertEquals(0, baseStatements.size());
	}

	@Test
	public void testGetBaseStatementsForPolicyType() throws Exception {
		BaseStatementEntity baseStatement = BDDMockito.mock(BaseStatementEntity.class);
		BDDMockito.when(baseStatement.getName()).thenReturn("Base Statement");
		BaseStatementDto dto = new BaseStatementDto();
		dto.setName("Base Statement");
		BDDMockito.when(baseStatement.toDto()).thenReturn(dto);
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity());
		BDDMockito
				.when(baseStatementRepository
						.findAllByStateAndPolicyTypesInOrderByNameAscVersionDesc(
								BDDMockito.any(PolicyElementState.class), BDDMockito.anySet()))
				.thenReturn(Collections.singletonList(baseStatement));

		Map<String, BaseStatementDto> baseStatements = baseStatementService
				.getLatestBaseStatementsForPolicyType(new PolicyTypeEntity("System"));
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertTrue(baseStatements.containsKey("Base Statement"));
		Assert.assertEquals(dto, baseStatements.get("Base Statement"));
	}

	@Test
	public void testImportBaseStatementsBaseStatementInvalid() throws Exception {
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base_Statement");

		BDDMockito.when(policyTypeService.getPolicyType("System"))
				.thenReturn(new PolicyTypeEntity("System"));

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement));
			Assert.fail();
		} catch (BaseStatementException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.startsWith("Base statement validation failed: "));
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("Evaluated function cannot be null"));
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString(
							"A base statement must be valid for at least one policy type"));
		}
	}

	@Test
	public void testImportBaseStatementsBaseStatementsAreInvalid() throws Exception {
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");

		BDDMockito.when(policyTypeService.getPolicyType("System"))
				.thenReturn(new PolicyTypeEntity("System"));

		try {
			baseStatementService.importBaseStatements(Arrays.asList(baseStatement, baseStatement));
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals(
					"The base statement name 'Base Statement' is duplicated. Base statement names must be unique",
					e.getMessage());
		}
	}

	@Test
	public void testImportBaseStatementsBaseStatementsArgumentsInvalid() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		function.setName("function_name");
		function.setAuthor("author");
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("description");
		function.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		function.setExpression("1 == 1");
		function.setParameters(Collections.emptyList());
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setParameter("param");
		argument.setDescription("description");
		argument.setType(ArgumentType.getTypeForName("string"));
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("user");
		baseStatement.setPolicyTypes(Collections.singleton("foo"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		BDDMockito.when(policyTypeService.getPolicyType("foo"))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function_name")).thenReturn(function);

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement));
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals(
					"Base statement validation failed: The number of arguments defined does not match the number of parameters for the function 'function_name'",
					e.getMessage());
		}
	}

	@Test
	public void testImportBaseStatementsArgumentEnumValuesInvalid() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		function.setName("function_name");
		function.setAuthor("author");
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("description");
		function.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		function.setExpression("1 == 1");
		function.setParameters(Collections.singletonList("param"));
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setParameter("param");
		argument.setDescription("This is an argument");
		argument.setEnumValues(Set.of("foo", "2"));
		argument.setType(ArgumentType.getTypeForName("integerArray"));
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("user");
		baseStatement.setPolicyTypes(Collections.singleton("foo"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		BDDMockito.when(policyTypeService.getPolicyType("foo"))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function_name")).thenReturn(function);

		try {
			baseStatementService.importBaseStatements(Collections.singletonList(baseStatement));
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals(
					"Base statement validation failed: The enum values do not match the type 'integer'",
					e.getMessage());
		}
	}

	@Test
	public void testImportBaseStatementsValid() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		function.setName("function_name");
		function.setAuthor("author");
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("description");
		function.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("System")));
		function.setExpression("1 == 1");
		function.setParameters(Collections.singletonList("param"));
		BaseStatementArgumentDto argument = new BaseStatementArgumentDto();
		argument.setParameter("param");
		argument.setDescription("This is an argument");
		argument.setType(ArgumentType.getTypeForName("integerArray"));
		BaseStatementDto baseStatement = new BaseStatementDto();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("user");
		baseStatement.setState(PolicyElementState.RELEASED);
		baseStatement.setDescription("description");
		baseStatement.setPolicyTypes(Collections.singleton("System"));
		baseStatement.setFunction("function_name");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setArguments(Collections.singletonList(argument));

		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("System");

		BDDMockito.when(policyTypeService.getPolicyType("System"))
				.thenReturn(new PolicyTypeEntity("System"));
		BDDMockito.when(functionService.getFunction("function_name")).thenReturn(function);

		baseStatementService.importBaseStatements(Collections.singletonList(baseStatement));

		ArgumentCaptor<Collection<BaseStatementEntity>> entityCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(baseStatementRepository).saveAll(entityCaptor.capture());
		Set<BaseStatementEntity> baseStatements = new HashSet<>(entityCaptor.getValue());
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertEquals(function, baseStatements.iterator().next().getFunction());
	}

	@Test
	public void testGetAllBaseStatements() {
		BaseStatementEntity baseStatement1 = new BaseStatementEntity();
		baseStatement1.setName("base statement");
		baseStatement1.setVersion(1);
		BaseStatementEntity baseStatement2 = new BaseStatementEntity();
		baseStatement2.setName("base statement");
		baseStatement2.setVersion(2);
		BDDMockito.when(baseStatementRepository.findAllByOrderByNameAscStateDescVersionDesc())
				.thenReturn(Arrays.asList(baseStatement1, baseStatement2));
		Map<String, BaseStatementDto> baseStatements = baseStatementService.getAllBaseStatements();
		Assert.assertEquals(2, baseStatements.size());
		Assert.assertTrue(baseStatements.containsKey("base statement:1"));
		Assert.assertTrue(baseStatements.containsKey("base statement:2"));
	}

	@Test
	public void testGetBaseStatements() {
		BaseStatementEntity baseStatement1 = new BaseStatementEntity();
		baseStatement1.setName("base statement");
		ReflectionTestUtils.setField(baseStatement1, "id", 2L);
		BaseStatementEntity baseStatement2 = new BaseStatementEntity();
		baseStatement2.setName("base statement");
		BDDMockito.when(baseStatementRepository.findAllByOrderByNameAscStateDescVersionDesc())
				.thenReturn(Arrays.asList(baseStatement1, baseStatement2));
		Map<String, Long> baseStatements = baseStatementService.getBaseStatements();
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertEquals(2L, baseStatements.get("base statement"), 0.001);
	}

	@Test
	public void testGetLatestBaseStatementsForPolicyType() {
		PolicyTypeEntity policyType = new PolicyTypeEntity("foo");
		BaseStatementEntity baseStatement1 = new BaseStatementEntity();
		baseStatement1.setName("base statement");
		baseStatement1.setVersion(2);
		BaseStatementEntity baseStatement2 = new BaseStatementEntity();
		baseStatement2.setName("base statement");
		baseStatement2.setVersion(1);
		BDDMockito.when(baseStatementRepository
				.findAllByStateAndPolicyTypesInOrderByNameAscVersionDesc(
						PolicyElementState.RELEASED, Collections.singleton(policyType)))
				.thenReturn(Arrays.asList(baseStatement1, baseStatement2));
		Map<String, BaseStatementDto> baseStatements = baseStatementService
				.getLatestBaseStatementsForPolicyType(policyType);
		Assert.assertEquals(1, baseStatements.size());
		Assert.assertEquals(2, baseStatements.get("base statement").getVersion());
	}

	@Test
	public void testGetBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		Assert.assertEquals(baseStatement, baseStatementService.getBaseStatement(1L));
	}

	@Test
	public void testGetBaseStatementNotFound() {
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.empty());
		try {
			baseStatementService.getBaseStatement(1L);
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no base statement with the id '1'",
					e.getMessage());
		}
	}

	@Test
	public void testGetBaseStatementParseNameAndVersion() throws Exception {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		BDDMockito.when(baseStatementRepository
				.findByNameAndVersion(BDDMockito.anyString(), BDDMockito.anyInt()))
				.thenReturn(Optional.of(baseStatement));
		Assert.assertEquals(baseStatement,
				baseStatementService.getBaseStatement("base statement:1"));
	}

	@Test
	public void testGetBaseStatementParseNameAndVersionNotFound() {
		BDDMockito.when(baseStatementRepository
				.findByNameAndVersion(BDDMockito.anyString(), BDDMockito.anyInt()))
				.thenReturn(Optional.empty());
		try {
			baseStatementService.getBaseStatement("base statement:1");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no base statement with the name 'base statement' and version 1",
					e.getMessage());
		}
	}

	@Test
	public void testGetBaseStatementParseNameAndVersionInvalidVersion() {
		try {
			baseStatementService.getBaseStatement("base statement:foo");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no released base statement with the name 'base statement:foo'",
					e.getMessage());
		}
	}

	@Test
	public void testGetLatestReleasedBaseStatement() {
		try {
			baseStatementService.getLatestReleasedBaseStatement("base statement");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no released base statement with the name 'base statement'",
					e.getMessage());
		}
	}

	@Test
	public void testGetPreviousVersionId() {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		ReflectionTestUtils.setField(baseStatement, "id", 1L);
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndVersionLessThanOrderByVersionDesc("base statement", 2))
				.thenReturn(Optional.of(baseStatement));
		Assert.assertEquals(1L, baseStatementService.getPreviousVersionId("base statement", 2),
				0.001);
	}

	@Test
	public void testGetPreviousVersionIdNotFound() {
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndVersionLessThanOrderByVersionDesc("function", 2))
				.thenReturn(Optional.empty());
		Assert.assertNull(baseStatementService.getPreviousVersionId("function", 2));
	}

	@Test
	public void testGetNextVersionId() {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		ReflectionTestUtils.setField(baseStatement, "id", 2L);
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc("base statement", 1))
				.thenReturn(Optional.of(baseStatement));
		Assert.assertEquals(2L, baseStatementService.getNextVersionId("base statement", 1), 0.001);
	}

	@Test
	public void testGetNextVersionIdNotFound() {
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc("base statement", 1))
				.thenReturn(Optional.empty());
		Assert.assertNull(baseStatementService.getNextVersionId("base statement", 1));
	}

	@Test
	public void testGetUpdatedBaseStatement() {
		BaseStatementEntity updated = new BaseStatementEntity();
		updated.setName("base statement");
		updated.setVersion(2);
		BDDMockito.when(baseStatementRepository
				.findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc("function", 1,
						PolicyElementState.RELEASED)).thenReturn(Optional.of(updated));
		Assert.assertTrue(baseStatementService.getUpdatedBaseStatement("function", 1).isPresent());
		Assert.assertEquals("base statement:2",
				baseStatementService.getUpdatedBaseStatement("function", 1).get()
						.getVersionedName());
	}

	@Test
	public void testCreateRevisionLatestReleased() throws Exception {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("base statement");
		baseStatement.setState(PolicyElementState.RELEASED);
		baseStatement.setVersion(1);
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(baseStatementRepository.findFirstByNameOrderByVersionDesc("base statement"))
				.thenReturn(baseStatement);
		baseStatementService.createRevision(1L, "user");
		ArgumentCaptor<BaseStatementEntity> baseStatementCaptor = ArgumentCaptor
				.forClass(BaseStatementEntity.class);
		BDDMockito.verify(baseStatementRepository).saveAndFlush(baseStatementCaptor.capture());
		BaseStatementEntity revision = baseStatementCaptor.getValue();
		Assert.assertEquals("base statement", revision.getName());
		Assert.assertEquals("user", revision.getAuthor());
		Assert.assertEquals(2, revision.getVersion());
		Assert.assertEquals(PolicyElementState.DRAFT, revision.getState());
	}

	@Test
	public void testCreateRevisionLatestDraft() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("base statement");
		baseStatement.setState(PolicyElementState.DRAFT);
		baseStatement.setVersion(1);
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(baseStatementRepository.findFirstByNameOrderByVersionDesc("base statement"))
				.thenReturn(baseStatement);
		try {
			baseStatementService.createRevision(1L, "user");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("A draft of this base statement already exists", e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testCreateRevisionLatestDeprecated() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("base statement");
		baseStatement.setState(PolicyElementState.DEPRECATED);
		baseStatement.setVersion(1);
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(baseStatementRepository.findFirstByNameOrderByVersionDesc("base statement"))
				.thenReturn(baseStatement);
		try {
			baseStatementService.createRevision(1L, "user");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("A deprecated base statement cannot be revised", e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testSaveBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function:0"))
				.thenReturn(createValidFunction());
		baseStatementService.saveBaseStatement(Optional.of(1L), createValidBaseStatement().toDto());
		BDDMockito.verify(baseStatementRepository).saveAndFlush(baseStatement);
	}

	@Test
	public void testSaveBaseStatementWrongAuthor() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function")).thenReturn(createValidFunction());

		BaseStatementDto dto = createValidBaseStatement().toDto();
		dto.setAuthor("user");
		try {
			baseStatementService.saveBaseStatement(Optional.of(1L), dto);
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("You do not have permission to update this base statement",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(baseStatement);
	}

	@Test
	public void testSaveBaseStatementNotDraft() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function")).thenReturn(createValidFunction());
		try {
			baseStatementService
					.saveBaseStatement(Optional.of(1L), createValidBaseStatement().toDto());
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("Cannot edit a base statement in the released state",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(baseStatement);
	}

	@Test
	public void testSaveBaseStatementNewAlreadyExists() throws PolicyElementNotFoundException {
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionService.getFunction("function")).thenReturn(createValidFunction());
		BDDMockito.when(baseStatementRepository.findFirstByNameOrderByVersionDesc("Base Statement"))
				.thenReturn(new BaseStatementEntity());
		try {
			baseStatementService
					.saveBaseStatement(Optional.empty(), createValidBaseStatement().toDto());
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("A base statement with the name 'Base Statement' already exists",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		ReflectionTestUtils
				.setField(baseStatement, "configuredStatements", Collections.emptyList());
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		baseStatementService.deleteBaseStatement(1L, "author");
		BDDMockito.verify(baseStatementRepository).delete(baseStatement);
		BDDMockito.verify(baseStatementRepository).flush();
	}

	@Test
	public void testDeleteBaseStatementWrongAuthor() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		try {
			baseStatementService.deleteBaseStatement(1L, "user");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("You are not the author of the base statement",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).delete(baseStatement);
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).flush();
	}

	@Test
	public void testDeleteBaseStatementReferenced() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		ReflectionTestUtils.setField(baseStatement, "configuredStatements",
				Collections.singletonList(new ConfiguredStatementEntity()));
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		try {
			baseStatementService.deleteBaseStatement(1L, "author");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals(
					"The base statement 'Base Statement' is referenced by at least one policy",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).delete(baseStatement);
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).flush();
	}

	@Test
	public void testUpdateBaseStatementState() throws Exception {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		baseStatementService.updateBaseStatementState(1L, "released");
		BDDMockito.verify(baseStatementRepository).saveAndFlush(baseStatement);
		Assert.assertEquals(PolicyElementState.RELEASED, baseStatement.getState());
	}

	@Test
	public void testUpdateBaseStatementStateInvalidState() throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		try {
			baseStatementService.updateBaseStatementState(1L, "foo");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("Invalid state", e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(baseStatement);
	}

	@Test
	public void testUpdateBaseStatementStateInvalidTransition()
			throws PolicyElementNotFoundException {
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(1L))
				.thenReturn(Optional.of(baseStatement));
		try {
			baseStatementService.updateBaseStatementState(1L, "deprecated");
			Assert.fail();
		} catch (BaseStatementException e) {
			Assert.assertEquals("Base statement cannot move to deprecated state from draft",
					e.getMessage());
		}
		BDDMockito.verify(baseStatementRepository, Mockito.times(0)).saveAndFlush(baseStatement);
	}

	@Test
	public void testGetArgumentsForBaseStatement() throws Exception {
		BDDMockito.when(functionService.getFunction("function")).thenReturn(createValidFunction());
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		List<BaseStatementArgumentDto> arguments = baseStatementService
				.getArgumentsForBaseStatement(Optional.of(1L), "function");
		Assert.assertEquals(1, arguments.size());
		Assert.assertEquals(baseStatement.getArguments().get(0).getParameter(),
				arguments.get(0).getParameter());
		Assert.assertEquals(baseStatement.getArguments().get(0).getType(),
				arguments.get(0).getType());
		Assert.assertEquals(baseStatement.getArguments().get(0).getDescription(),
				arguments.get(0).getDescription());
		Assert.assertEquals(baseStatement.getArguments().get(0).getEnumValues(),
				arguments.get(0).getEnumValues());
	}

	@Test
	public void testGetArgumentsForBaseStatementNewParam() throws Exception {
		BaseStatementFunctionEntity function = createValidFunction();
		function.setParameters(Arrays.asList("new", "param"));
		BDDMockito.when(functionService.getFunction("function")).thenReturn(function);
		BaseStatementEntity baseStatement = createValidBaseStatement();
		BDDMockito.when(baseStatementRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(baseStatement));
		List<BaseStatementArgumentDto> arguments = baseStatementService
				.getArgumentsForBaseStatement(Optional.of(1L), "function");
		Assert.assertEquals(2, arguments.size());
		Assert.assertEquals("new", arguments.get(0).getParameter());
		Assert.assertEquals(baseStatement.getArguments().get(0).getParameter(),
				arguments.get(1).getParameter());
		Assert.assertEquals(baseStatement.getArguments().get(0).getType(),
				arguments.get(1).getType());
		Assert.assertEquals(baseStatement.getArguments().get(0).getDescription(),
				arguments.get(1).getDescription());
		Assert.assertEquals(baseStatement.getArguments().get(0).getEnumValues(),
				arguments.get(1).getEnumValues());
	}

	@Test
	public void testGetArgumentsForBaseStatementNoId() throws Exception {
		BDDMockito.when(functionService.getFunction("function")).thenReturn(createValidFunction());
		List<BaseStatementArgumentDto> arguments = baseStatementService
				.getArgumentsForBaseStatement(Optional.empty(), "function");
		Assert.assertEquals(1, arguments.size());
		Assert.assertEquals("param", arguments.get(0).getParameter());
		Assert.assertNull(arguments.get(0).getDescription());
	}

	public static BaseStatementEntity createValidBaseStatement() {
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("author");
		baseStatement.setState(PolicyElementState.DRAFT);
		baseStatement.setDescription("description");
		baseStatement.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		baseStatement.setFunction(createValidFunction());
		baseStatement.setArguments(Collections.singletonList(createValidArgument()));
		return baseStatement;
	}

	private static BaseStatementArgumentEntity createValidArgument() {
		BaseStatementArgumentEntity argument = new BaseStatementArgumentEntity();
		argument.setParameter("param");
		argument.setType(ArgumentType.getTypeForName("string"));
		argument.setDescription("description");
		argument.setEnumValues(new HashSet<>(Arrays.asList("foo", "bar")));
		return argument;
	}

	private static BaseStatementFunctionEntity createValidFunction() {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity("function");
		function.setAuthor("author");
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("description");
		function.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		function.setDependencies(Collections.emptySet());
		function.setExpression("1 == 1");
		function.setParameters(Collections.singletonList("param"));
		return function;
	}

}
