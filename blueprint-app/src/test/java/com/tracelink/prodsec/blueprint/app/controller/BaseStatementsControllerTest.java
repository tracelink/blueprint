package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.exception.BaseStatementException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionService;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionServiceTest;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementServiceTest;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.setup.BaseStatementSetup;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
import java.util.Collections;
import java.util.Optional;
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
public class BaseStatementsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private BaseStatementService baseStatementService;
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
		BDDMockito.when(baseStatementService.getBaseStatements())
				.thenReturn(Collections.singletonMap("baseStatement", 1L));
		BDDMockito.when(functionService.getLatestFunctions())
				.thenReturn(Collections.singletonList("function"));
		BDDMockito.when(functionService
				.getUpdatedFunction(BDDMockito.anyString(), BDDMockito.anyInt())).thenReturn(
				Optional.of(BaseStatementFunctionServiceTest.createValidFunction()));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testGetBaseStatementsNoId() throws Exception {
		BDDMockito.when(baseStatementService.getBaseStatement(1L)).thenReturn(
				BaseStatementServiceTest.createValidBaseStatement());
		mockMvc.perform(MockMvcRequestBuilders.get("/base-statements"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("base-statements")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.iterableWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("argumentTypes", Matchers.iterableWithSize(7)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(false)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("baseStatement", "baseStatementId", "prevVersionId",
								"nextVersionId", "updatedFunction"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testGetBaseStatementsWithId() throws Exception {
		BDDMockito.when(baseStatementService.getBaseStatement(1L)).thenReturn(
				BaseStatementServiceTest.createValidBaseStatement());
		mockMvc.perform(MockMvcRequestBuilders.get("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("base-statements")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.iterableWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("argumentTypes", Matchers.iterableWithSize(7)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(false)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("baseStatement", "baseStatementId", "prevVersionId",
								"nextVersionId", "updatedFunction"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testCreateBaseStatement() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/base-statements/create"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("base-statements")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.iterableWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("argumentTypes", Matchers.iterableWithSize(7)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(true)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("baseStatement"))
				.andExpect(MockMvcResultMatchers.model()
						.attributeDoesNotExist("baseStatementId", "prevVersionId", "nextVersionId",
								"updatedFunction"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testEditBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = BaseStatementServiceTest.createValidBaseStatement();
		BDDMockito.when(baseStatementService.getBaseStatement(1L)).thenReturn(baseStatement);
		mockMvc.perform(MockMvcRequestBuilders.get("/base-statements/1/edit"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("base-statements")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("functions", Matchers.iterableWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("argumentTypes", Matchers.iterableWithSize(7)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.aMapWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.equalTo(true)))
				.andExpect(MockMvcResultMatchers.model()
						.attributeExists("baseStatement", "baseStatementId", "prevVersionId",
								"nextVersionId",
								"updatedFunction"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testEditBaseStatementNotDraft() throws Exception {
		BaseStatementEntity baseStatement = BaseStatementServiceTest.createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementService.getBaseStatement(1L)).thenReturn(baseStatement);
		mockMvc.perform(MockMvcRequestBuilders.get("/base-statements/1/edit"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot edit a base statement that is in the released state"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testCreateRevision() throws Exception {
		BaseStatementEntity baseStatement = BaseStatementServiceTest.createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementService.createRevision(1L, "user")).thenReturn(baseStatement);
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/create-revision")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/0/edit"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testCreateRevisionFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementException("failure")).when(baseStatementService)
				.createRevision(1L, "user");
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/create-revision")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot create new revision. failure"));
	}


	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testSaveBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = BaseStatementServiceTest.createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementService
				.saveBaseStatement(BDDMockito.any(), BDDMockito.any(BaseStatementDto.class)))
				.thenReturn(baseStatement);
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/save")
				.param("name", "baseStatement")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/0"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testSaveBaseStatementFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementException("failure")).when(baseStatementService)
				.saveBaseStatement(BDDMockito.any(), BDDMockito.any(BaseStatementDto.class));
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/save")
				.param("name", "baseStatement")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1/edit"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot save base statement. failure"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("baseStatement"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testSaveBaseStatementFailureNoId() throws Exception {
		BDDMockito.doThrow(new BaseStatementException("failure")).when(baseStatementService)
				.saveBaseStatement(BDDMockito.any(), BDDMockito.any(BaseStatementDto.class));
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/save")
				.param("name", "baseStatement")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/create"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot save base statement. failure"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("baseStatement"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testDeleteBaseStatement() throws Exception {
		BaseStatementEntity baseStatement = BaseStatementServiceTest.createValidBaseStatement();
		baseStatement.setState(PolicyElementState.RELEASED);
		BDDMockito.when(baseStatementService.deleteBaseStatement(1L, "user"))
				.thenReturn(baseStatement);
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements"))
				.andExpect(MockMvcResultMatchers.flash().attribute("success",
						"Deleted version 0 of base statement 'Base Statement'"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testDeleteBaseStatementFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementException("failure")).when(baseStatementService)
				.deleteBaseStatement(1L, "user");
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot delete base statement. failure"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testUpdateBaseStatementState() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("success",
						"Updated base statement state"));
	}

	@Test
	@WithMockUser(username = "user", authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testUpdateBaseStatementStateFailure() throws Exception {
		BDDMockito.doThrow(new BaseStatementException("failure")).when(baseStatementService)
				.updateBaseStatementState(1L, "release");
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements/1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"Cannot update base statement state. failure"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testHandlePolicyElementNotFound() throws Exception {
		BDDMockito.doThrow(new PolicyElementNotFoundException("not found")).when(
				baseStatementService)
				.updateBaseStatementState(1L, "release");
		mockMvc.perform(MockMvcRequestBuilders.post("/base-statements/1/state/release")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/base-statements"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"not found"));
	}

}
