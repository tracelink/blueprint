package com.tracelink.prodsec.blueprint.app.service;

import com.tracelink.prodsec.blueprint.app.exception.BaseStatementFunctionException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.logger.LoggerRule;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.BaseStatementFunctionRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
public class BaseStatementFunctionServiceTest {

	@MockBean
	private BaseStatementFunctionRepository functionRepository;
	@MockBean
	private PolicyTypeService policyTypeService;
	@Rule
	public final LoggerRule loggerRule = LoggerRule.forClass(BaseStatementFunctionService.class);

	private BaseStatementFunctionService functionService;

	@Before
	public void setup() {
		functionService = new BaseStatementFunctionService(functionRepository,
				policyTypeService);
	}

	@Test
	public void testImportBaseStatementFunctionsFunctionInvalid() throws Exception {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setName("Function Name");
		function.setAuthor("user");
		function.setParameters(Collections.singletonList("Bad Param"));

		try {
			functionService
					.importBaseStatementFunctions(Collections.singletonList(function));
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.startsWith("Function validation failed: "));
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString(
							"A function must be valid for at least one policy type"));
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("Expression cannot be blank"));
		}
	}

	@Test
	public void testImportBaseStatementFunctionsFunctionsAreInvalid() {
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setDescription("This is a function");
		function.setName("function_name");
		function.setParameters(Collections.singletonList("param"));
		function.setExpression("1 == 1");
		function.setPolicyTypes(Collections.singleton("User"));
		function.setDependencies(Set.of("foobar"));

		try {
			functionService
					.importBaseStatementFunctions(Arrays.asList(function, function));
			Assert.fail();
		} catch (BaseStatementFunctionException | PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"The base statement function name 'function_name' is duplicated. Function names must be unique",
					e.getMessage());
		}
	}

	@Test
	public void testImportBaseStatementFunctionsValid() throws Exception {
		BaseStatementFunctionDto dependency = new BaseStatementFunctionDto();
		dependency.setDescription("This is a dependency");
		dependency.setName("dependency_name");
		dependency.setAuthor("user");
		dependency.setParameters(Collections.emptyList());
		dependency.setExpression("1 == 1");
		dependency.setPolicyTypes(Collections.singleton("User"));

		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setDescription("This is a function");
		function.setName("function_name");
		function.setAuthor("user");
		function.setParameters(Collections.singletonList("param"));
		function.setExpression("1 == 1");
		function.setPolicyTypes(Collections.singleton("User"));
		function.setDependencies(Collections.singleton("dependency_name"));

		functionService
				.importBaseStatementFunctions(Arrays.asList(function, dependency));

		ArgumentCaptor<Collection<BaseStatementFunctionEntity>> entitiesCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(functionRepository).saveAll(entitiesCaptor.capture());
		BDDMockito.verify(functionRepository).flush();
		MatcherAssert.assertThat(entitiesCaptor.getValue().size(), Matchers.is(2));
		MatcherAssert.assertThat(entitiesCaptor.getValue(),
				Matchers.hasItems(Matchers.allOf(
						Matchers.hasProperty("dependencies", Matchers.iterableWithSize(1)),
						Matchers.hasProperty("name", Matchers.is(function.getName()))),
						Matchers.hasProperty("name", Matchers.is(dependency.getName()))));
	}

	@Test
	public void testImportBaseStatementFunctionsExistingDependency() throws Exception {
		BDDMockito.when(functionRepository.findFirstByNameAndStateOrderByVersionDesc("dependency",
				PolicyElementState.RELEASED)).thenReturn(createValidDependency());
		BaseStatementFunctionDto function = new BaseStatementFunctionDto();
		function.setDescription("This is a function");
		function.setName("function_name");
		function.setAuthor("user");
		function.setParameters(Collections.singletonList("param"));
		function.setExpression("1 == 1");
		function.setPolicyTypes(Collections.singleton("foo"));
		function.setDependencies(Collections.singleton("dependency"));

		functionService
				.importBaseStatementFunctions(Arrays.asList(function));

		ArgumentCaptor<Collection<BaseStatementFunctionEntity>> entitiesCaptor = ArgumentCaptor
				.forClass(Collection.class);
		BDDMockito.verify(functionRepository).saveAll(entitiesCaptor.capture());
		BDDMockito.verify(functionRepository).flush();
		MatcherAssert.assertThat(entitiesCaptor.getValue().size(), Matchers.is(1));
		MatcherAssert.assertThat(entitiesCaptor.getValue(),
				Matchers.hasItems(Matchers.allOf(
						Matchers.hasProperty("dependencies", Matchers.iterableWithSize(1)),
						Matchers.hasProperty("name", Matchers.is(function.getName())))));
	}

	@Test
	public void testGetFunctions() {
		BaseStatementFunctionEntity function1 = new BaseStatementFunctionEntity("function");
		ReflectionTestUtils.setField(function1, "id", 2L);
		BaseStatementFunctionEntity function2 = new BaseStatementFunctionEntity("function");
		BDDMockito.when(functionRepository.findAllByOrderByNameAscStateDescVersionDesc())
				.thenReturn(Arrays.asList(function1, function2));
		Map<String, Long> functions = functionService.getFunctions();
		Assert.assertEquals(1, functions.size());
		Assert.assertEquals(2L, functions.get("function"), 0.001);
	}

	@Test
	public void testGetLatestFunctions() {
		BaseStatementFunctionEntity function1 = new BaseStatementFunctionEntity("function");
		function1.setVersion(2);
		BaseStatementFunctionEntity function2 = new BaseStatementFunctionEntity("function");
		function2.setVersion(1);
		BDDMockito.when(functionRepository.findAllByStateOrderByNameAscVersionDesc(
				PolicyElementState.RELEASED))
				.thenReturn(Arrays.asList(function1, function2));
		List<String> functions = functionService.getLatestFunctions();
		Assert.assertEquals(1, functions.size());
		Assert.assertEquals("function:2", functions.get(0));
	}

	@Test
	public void testGetFunction() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		BDDMockito.when(functionRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(function));
		Assert.assertEquals(function, functionService.getFunction(1L));
	}

	@Test
	public void testGetFunctionNotFound() {
		BDDMockito.when(functionRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.empty());
		try {
			functionService.getFunction(1L);
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no base statement function with the id '1'",
					e.getMessage());
		}
	}

	@Test
	public void testGetFunctionParseNameAndVersion() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		BDDMockito.when(functionRepository
				.findByNameAndVersion(BDDMockito.anyString(), BDDMockito.anyInt()))
				.thenReturn(Optional.of(function));
		Assert.assertEquals(function, functionService.getFunction("function:1"));
	}

	@Test
	public void testGetFunctionParseNameAndVersionNotFound() {
		BDDMockito.when(functionRepository
				.findByNameAndVersion(BDDMockito.anyString(), BDDMockito.anyInt()))
				.thenReturn(Optional.empty());
		try {
			functionService.getFunction("function:1");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no base statement function with the name 'function' and version 1",
					e.getMessage());
		}
	}

	@Test
	public void testGetFunctionParseNameAndVersionInvalidVersion() {
		try {
			functionService.getFunction("function:foo");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no released base statement function with the name 'function:foo'",
					e.getMessage());
		}
	}

	@Test
	public void testGetLatestReleasedFunction() {
		try {
			functionService.getLatestReleasedFunction("function");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals(
					"There is no released base statement function with the name 'function'",
					e.getMessage());
		}
	}

	@Test
	public void testGetPreviousVersionId() {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		ReflectionTestUtils.setField(function, "id", 1L);
		BDDMockito.when(functionRepository
				.findFirstByNameAndVersionLessThanOrderByVersionDesc("function", 2))
				.thenReturn(Optional.of(function));
		Assert.assertEquals(1L, functionService.getPreviousVersionId("function", 2), 0.001);
	}

	@Test
	public void testGetPreviousVersionIdNotFound() {
		BDDMockito.when(functionRepository
				.findFirstByNameAndVersionLessThanOrderByVersionDesc("function", 2))
				.thenReturn(Optional.empty());
		Assert.assertNull(functionService.getPreviousVersionId("function", 2));
	}

	@Test
	public void testGetNextVersionId() {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		ReflectionTestUtils.setField(function, "id", 2L);
		BDDMockito.when(functionRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc("function", 1))
				.thenReturn(Optional.of(function));
		Assert.assertEquals(2L, functionService.getNextVersionId("function", 1), 0.001);
	}

	@Test
	public void testGetNextVersionIdNotFound() {
		BDDMockito.when(functionRepository
				.findFirstByNameAndVersionGreaterThanOrderByVersionAsc("function", 1))
				.thenReturn(Optional.empty());
		Assert.assertNull(functionService.getNextVersionId("function", 1));
	}

	@Test
	public void testGetUpdatedDependencies() {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		BaseStatementFunctionEntity dependency = new BaseStatementFunctionEntity("function");
		dependency.setVersion(1);
		BaseStatementFunctionEntity updated = new BaseStatementFunctionEntity("function");
		updated.setVersion(2);
		function.setDependencies(Collections.singleton(dependency));
		BDDMockito.when(functionRepository
				.findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc("function", 1,
						PolicyElementState.RELEASED)).thenReturn(Optional.of(updated));
		Assert.assertEquals(1, functionService.getUpdatedDependencies(function).size());
		Assert.assertEquals("function:2",
				functionService.getUpdatedDependencies(function).iterator().next());
	}

	@Test
	public void testCreateRevisionLatestReleased() throws Exception {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity("function");
		function.setState(PolicyElementState.RELEASED);
		function.setVersion(1);
		BDDMockito.when(functionRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(function));
		BDDMockito.when(functionRepository.findFirstByNameOrderByVersionDesc("function"))
				.thenReturn(function);
		functionService.createRevision(1L, "user");
		ArgumentCaptor<BaseStatementFunctionEntity> functionCaptor = ArgumentCaptor
				.forClass(BaseStatementFunctionEntity.class);
		BDDMockito.verify(functionRepository).saveAndFlush(functionCaptor.capture());
		BaseStatementFunctionEntity revision = functionCaptor.getValue();
		Assert.assertEquals("function", revision.getName());
		Assert.assertEquals("user", revision.getAuthor());
		Assert.assertEquals(2, revision.getVersion());
		Assert.assertEquals(PolicyElementState.DRAFT, revision.getState());
	}

	@Test
	public void testCreateRevisionLatestDraft() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity("function");
		function.setState(PolicyElementState.DRAFT);
		function.setVersion(1);
		BDDMockito.when(functionRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(function));
		BDDMockito.when(functionRepository.findFirstByNameOrderByVersionDesc("function"))
				.thenReturn(function);
		try {
			functionService.createRevision(1L, "user");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("A draft of this function already exists", e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testCreateRevisionLatestDeprecated() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity("function");
		function.setState(PolicyElementState.DEPRECATED);
		function.setVersion(1);
		BDDMockito.when(functionRepository.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(function));
		BDDMockito.when(functionRepository.findFirstByNameOrderByVersionDesc("function"))
				.thenReturn(function);
		try {
			functionService.createRevision(1L, "user");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("A deprecated function cannot be revised", e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testSaveFunction() throws Exception {
		BaseStatementFunctionEntity function = createValidFunction();
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionRepository.findByNameAndVersion("dependency", 0))
				.thenReturn(Optional.of(createValidDependency()));
		functionService.saveFunction(Optional.of(1L), createValidFunction().toDto());
		BDDMockito.verify(functionRepository).saveAndFlush(function);
	}

	@Test
	public void testSaveFunctionWrongAuthor() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionRepository.findByNameAndVersion("dependency", 0))
				.thenReturn(Optional.of(createValidDependency()));

		BaseStatementFunctionDto dto = createValidFunction().toDto();
		dto.setAuthor("user");
		try {
			functionService.saveFunction(Optional.of(1L), dto);
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("You do not have permission to update this function",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(function);
	}

	@Test
	public void testSaveFunctionNotDraft() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionRepository.findByNameAndVersion("dependency", 0))
				.thenReturn(Optional.of(createValidDependency()));
		try {
			functionService.saveFunction(Optional.of(1L), createValidFunction().toDto());
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("Cannot edit a base statement function in the released state",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(function);
	}

	@Test
	public void testSaveFunctionNewAlreadyExists() throws PolicyElementNotFoundException {
		BDDMockito.when(policyTypeService.getPolicyType(BDDMockito.anyString()))
				.thenReturn(new PolicyTypeEntity("foo"));
		BDDMockito.when(functionRepository.findByNameAndVersion("dependency", 0))
				.thenReturn(Optional.of(createValidDependency()));
		BDDMockito.when(functionRepository.findFirstByNameOrderByVersionDesc("function"))
				.thenReturn(new BaseStatementFunctionEntity());
		try {
			functionService.saveFunction(Optional.empty(), createValidFunction().toDto());
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("A function with the name 'function' already exists",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void testDeleteFunction() throws Exception {
		BaseStatementFunctionEntity function = createValidFunction();
		ReflectionTestUtils.setField(function, "baseStatements", Collections.emptySet());
		ReflectionTestUtils.setField(function, "dependents", Collections.emptySet());
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		functionService.deleteFunction(1L, "author");
		BDDMockito.verify(functionRepository).delete(function);
		BDDMockito.verify(functionRepository).flush();
	}

	@Test
	public void testDeleteFunctionWrongAuthor() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		try {
			functionService.deleteFunction(1L, "user");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("You are not the author of the base statement function",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).delete(function);
		BDDMockito.verify(functionRepository, Mockito.times(0)).flush();
	}

	@Test
	public void testDeleteFunctionReferenced() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		ReflectionTestUtils.setField(function, "baseStatements", Collections.emptySet());
		ReflectionTestUtils.setField(function, "dependents",
				Collections.singleton(new BaseStatementFunctionEntity()));
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		try {
			functionService.deleteFunction(1L, "author");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals(
					"The base statement function 'function' is referenced by at least one base statement or function",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).delete(function);
		BDDMockito.verify(functionRepository, Mockito.times(0)).flush();
	}

	@Test
	public void testUpdateFunctionState() throws Exception {
		BaseStatementFunctionEntity function = createValidFunction();
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		functionService.updateFunctionState(1L, "released");
		BDDMockito.verify(functionRepository).saveAndFlush(function);
		Assert.assertEquals(PolicyElementState.RELEASED, function.getState());
	}

	@Test
	public void testUpdateFunctionStateInvalidState() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		try {
			functionService.updateFunctionState(1L, "foo");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("Invalid state", e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(function);
	}

	@Test
	public void testUpdateFunctionStateInvalidTransition() throws PolicyElementNotFoundException {
		BaseStatementFunctionEntity function = createValidFunction();
		BDDMockito.when(functionRepository.findById(1L))
				.thenReturn(Optional.of(function));
		try {
			functionService.updateFunctionState(1L, "deprecated");
			Assert.fail();
		} catch (BaseStatementFunctionException e) {
			Assert.assertEquals("Function cannot move to deprecated state from draft",
					e.getMessage());
		}
		BDDMockito.verify(functionRepository, Mockito.times(0)).saveAndFlush(function);
	}

	public static BaseStatementFunctionEntity createValidFunction() {
		BaseStatementFunctionEntity functionDto = new BaseStatementFunctionEntity();
		functionDto.setName("function");
		functionDto.setAuthor("author");
		functionDto.setState(PolicyElementState.DRAFT);
		functionDto.setDescription("description");
		functionDto.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		functionDto.setDependencies(Collections.singleton(createValidDependency()));
		functionDto.setParameters(Collections.singletonList("param"));
		functionDto.setExpression("param == 1");
		return functionDto;
	}

	private static BaseStatementFunctionEntity createValidDependency() {
		BaseStatementFunctionEntity dependency = new BaseStatementFunctionEntity("dependency");
		dependency.setAuthor("author");
		dependency.setState(PolicyElementState.RELEASED);
		dependency.setPolicyTypes(Collections.singleton(new PolicyTypeEntity("foo")));
		dependency.setDependencies(Collections.emptySet());
		dependency.setExpression("1 == 1");
		dependency.setParameters(Collections.emptyList());
		return dependency;
	}

}
