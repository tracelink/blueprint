package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.exception.BaseStatementFunctionException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionService;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionServiceTest;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.setup.BaseStatementSetup;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class BaseStatementFunctionsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private BaseStatementFunctionService functionService;
	@MockBean
	private PolicyTypeService policyTypeService;
	@MockBean
	private BaseStatementSetup setup;

	@Before
	public void setup() {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(functionService.getFunctions())
				.thenReturn(Collections.singletonMap("function", 1L));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testGetFunctionsNoId() throws Exception {
		BDDMockito.when(functionService.getFunction(1L)).thenReturn(
				BaseStatementFunctionServiceTest.createValidFunction());
		mockMvc.perform(MockMvcRequestBuilders.get("/functions"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("functions")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(false)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("function", "functionId", "prevVersionId", "nextVersionId",
								"updatedDependencies"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testGetFunctionsWithId() throws Exception {
		BDDMockito.when(functionService.getFunction(1L)).thenReturn(
				BaseStatementFunctionServiceTest.createValidFunction());
		mockMvc.perform(MockMvcRequestBuilders.get("/functions/1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("functions")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(false)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("function", "functionId", "prevVersionId", "nextVersionId",
								"updatedDependencies"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testCreateFunction() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/functions/create"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("functions")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(true)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("function"))
				.andExpect(MockMvcResultMatchers.model()
						.attributeDoesNotExist("functionId", "prevVersionId", "nextVersionId",
								"updatedDependencies"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testEditFunction() throws Exception {
		BaseStatementFunctionEntity function = BaseStatementFunctionServiceTest
				.createValidFunction();
		BDDMockito.when(functionService.getFunction(1L)).thenReturn(function);
		mockMvc.perform(MockMvcRequestBuilders.get("/functions/1/edit"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("functions")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(true)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("function", "functionId", "prevVersionId", "nextVersionId",
								"updatedDependencies"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testEditFunctionNotDraft() throws Exception {
		BaseStatementFunctionEntity function = BaseStatementFunctionServiceTest
				.createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionService.getFunction(1L)).thenReturn(function);
		mockMvc.perform(MockMvcRequestBuilders.get("/functions/1/edit"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot edit a function that is in the released state"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testCreateRevision() throws Exception {
		BaseStatementFunctionEntity function = BaseStatementFunctionServiceTest
				.createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionService.createRevision(1L, "user")).thenReturn(function);
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/create-revision")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/0/edit"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testCreateRevisionFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementFunctionException("failure")).when(functionService)
				.createRevision(1L, "user");
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/create-revision")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot create new revision. failure"));
	}


	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testSaveFunction() throws Exception {
		BaseStatementFunctionEntity function = BaseStatementFunctionServiceTest
				.createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionService
				.saveFunction(BDDMockito.any(), BDDMockito.any(BaseStatementFunctionDto.class)))
				.thenReturn(function);
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/save")
				.param("name", "function")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/0"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testSaveFunctionFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementFunctionException("failure")).when(functionService)
				.saveFunction(BDDMockito.any(), BDDMockito.any(BaseStatementFunctionDto.class));
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/save")
				.param("name", "function")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1/edit"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot save function. failure"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("function"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testSaveFunctionFailureNoId() throws Exception {
		BDDMockito.doThrow(new BaseStatementFunctionException("failure")).when(functionService)
				.saveFunction(BDDMockito.any(), BDDMockito.any(BaseStatementFunctionDto.class));
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/save")
				.param("name", "function")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/create"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot save function. failure"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("function"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testDeleteFunction() throws Exception {
		BaseStatementFunctionEntity function = BaseStatementFunctionServiceTest
				.createValidFunction();
		function.setState(PolicyElementState.RELEASED);
		BDDMockito.when(functionService.deleteFunction(1L, "user")).thenReturn(function);
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions"))
				.andExpect(MockMvcResultMatchers.flash().attribute("success",
						"Deleted version 0 of function 'function'"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testDeleteFunctionFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementFunctionException("failure")).when(functionService)
				.deleteFunction(1L, "user");
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot delete function. failure"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testUpdateFunctionState() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("success",
						"Updated function state"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testUpdateFunctionStateFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementFunctionException("failure")).when(functionService)
				.updateFunctionState(1L, "release");
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot update function state. failure"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.FUNCTION_EDITOR_ROLE)
	public void testHandlePolicyElementNotFound() throws Exception {
		BDDMockito.doThrow(new PolicyElementNotFoundException("not found")).when(functionService)
				.updateFunctionState(1L, "release");
		mockMvc.perform(MockMvcRequestBuilders.post("/functions/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/functions"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"not found"));
	}

}
