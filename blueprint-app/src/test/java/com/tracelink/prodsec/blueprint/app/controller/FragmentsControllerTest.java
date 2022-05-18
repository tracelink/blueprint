package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.setup.BaseStatementSetup;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class FragmentsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private BaseStatementService baseStatementService;
	@MockBean
	private PolicyTypeService policyTypeService;
	@MockBean
	private BaseStatementSetup setup;

	@Before
	public void setup() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyType("foo"))
				.thenReturn(new PolicyTypeEntity("foo"));
	}

	@Test
	@WithMockUser
	public void testGetClause() throws Exception {
		BDDMockito.when(baseStatementService
				.getLatestBaseStatementsForPolicyType(BDDMockito.any(PolicyTypeEntity.class)))
				.thenReturn(
						Collections.singletonMap("base statement", new BaseStatementDto()));
		mockMvc.perform(MockMvcRequestBuilders.get("/fragments/clause").param("type", "foo"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.view().name("fragments/clause.html"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.aMapWithSize(1)));
	}

	@Test
	@WithMockUser
	public void testGetStatement() throws Exception {
		BDDMockito.when(baseStatementService
				.getBaseStatement(BDDMockito.anyString())).thenReturn(new BaseStatementEntity());
		mockMvc.perform(
				MockMvcRequestBuilders.get("/fragments/statement")
						.param("clauseIndex", "0")
						.param("statementIndex", "1")
						.param("baseStatement", "base statement"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.view().name("fragments/statement.html"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("clauseIndex", Matchers.is(0)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("statementIndex", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.model().attributeExists("baseStatement"));
	}

	@Test
	@WithMockUser(authorities = CoreRole.BASE_STMT_EDITOR_ROLE)
	public void testGetArguments() throws Exception {
		BDDMockito.when(baseStatementService
				.getArgumentsForBaseStatement(BDDMockito.any(), BDDMockito.anyString()))
				.thenReturn(Collections.singletonList(new BaseStatementArgumentDto()));
		mockMvc.perform(
				MockMvcRequestBuilders.get("/fragments/arguments")
						.param("baseStatementId", "1")
						.param("functionName", "function"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.view().name("fragments/arguments.html"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("arguments", Matchers.iterableWithSize(1)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("editable", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("argumentTypes", Matchers.iterableWithSize(7)));
	}

	@Test
	@WithMockUser
	public void testHandlePolicyElementNotFound() throws Exception {
		BDDMockito.doThrow(new PolicyElementNotFoundException("not found")).when(policyTypeService)
				.getPolicyType("foo");
		mockMvc.perform(MockMvcRequestBuilders.get("/fragments/clause").param("type", "foo"))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.model().attribute("message", "not found"));
	}

}
