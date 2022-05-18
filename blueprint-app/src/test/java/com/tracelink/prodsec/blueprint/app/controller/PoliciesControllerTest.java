package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
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
public class PoliciesControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private PolicyService policyService;
	@MockBean
	private BaseStatementService baseStatementService;

	private PolicyEntity policy;
	private PolicyTypeEntity policyType;

	@Before
	public void setup() {
		policyType = new PolicyTypeEntity();
		policyType.setName("foo");
		policy = new PolicyEntity();
		policy.setPolicyType(policyType);
		policy.setClauses(Collections.emptyList());
		policy.setName("Foo");
		policy.setAuthor("user");
	}

	@Test
	@WithMockUser
	public void testGetPoliciesNone() throws Exception {
		BDDMockito.when(policyService.getPolicies()).thenReturn(Collections.emptyMap());
		BDDMockito.when(baseStatementService
				.getLatestBaseStatementsForPolicyType(BDDMockito.any(PolicyTypeEntity.class)))
				.thenReturn(Collections.emptyMap());

		mockMvc.perform(MockMvcRequestBuilders.get("/policies"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("policies")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policies", Matchers.anEmptyMap()));

		BDDMockito.verify(policyService, Mockito.times(0)).getPolicy(BDDMockito.anyLong());
	}

	@Test
	@WithMockUser
	public void testGetPolicies() throws Exception {
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("foo", 1L));
		BDDMockito.when(policyService.getPolicy(BDDMockito.anyLong())).thenReturn(policy);
		BDDMockito.when(baseStatementService.getAllBaseStatements())
				.thenReturn(Collections.singletonMap("bar", new BaseStatementDto()));

		mockMvc.perform(MockMvcRequestBuilders.get("/policies"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("policies")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policies", Matchers.hasEntry("foo", 1L)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.hasKey("bar")))
				.andExpect(MockMvcResultMatchers.model().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.model().attribute("author", "user"))
				.andExpect(MockMvcResultMatchers.model().attribute("policyId", 0L))
				.andExpect(MockMvcResultMatchers.model().attribute("delete", true));

		BDDMockito.verify(policyService).getPolicy(BDDMockito.anyLong());
	}

	@Test
	@WithMockUser
	public void testGetPoliciesWithId() throws Exception {
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("foo", 1L));
		BDDMockito.when(baseStatementService.getAllBaseStatements())
				.thenReturn(Collections.singletonMap("bar", new BaseStatementDto()));
		BDDMockito.when(policyService.getPolicy(1L)).thenReturn(policy);

		mockMvc.perform(MockMvcRequestBuilders.get("/policies/1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("policies")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policies", Matchers.hasEntry("foo", 1L)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.hasKey("bar")))
				.andExpect(MockMvcResultMatchers.model().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.model().attribute("author", "user"))
				.andExpect(MockMvcResultMatchers.model().attribute("policyId", 0L))
				.andExpect(MockMvcResultMatchers.model().attribute("delete", true));

		BDDMockito.verify(policyService).getPolicy(1L);
	}

	@Test
	@WithMockUser(username = "user2")
	public void testGetPoliciesWithInvalidId() throws Exception {
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("foo", 1L));
		BDDMockito.when(baseStatementService.getAllBaseStatements())
				.thenReturn(Collections.singletonMap("bar", new BaseStatementDto()));
		BDDMockito.when(policyService.getPolicy(1L)).thenReturn(policy);

		mockMvc.perform(MockMvcRequestBuilders.get("/policies/2"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("policies")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policies", Matchers.hasEntry("foo", 1L)))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.hasKey("bar")))
				.andExpect(MockMvcResultMatchers.model().attribute("failure", "Invalid policy id"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.model().attribute("author", "user"))
				.andExpect(MockMvcResultMatchers.model().attribute("policyId", 0L))
				.andExpect(MockMvcResultMatchers.model().attribute("delete", false));

		BDDMockito.verify(policyService).getPolicy(1L);
	}

	@Test
	@WithMockUser
	public void testDeletePolicy() throws Exception {
		BDDMockito.when(policyService.deletePolicy(BDDMockito.anyLong(), BDDMockito.anyString()))
				.thenReturn(policy);

		mockMvc.perform(MockMvcRequestBuilders.post("/policies/delete/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/policies"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("success", "Deleted policy \"Foo\""));

		BDDMockito.verify(policyService).deletePolicy(1L, "user");
	}

	@Test
	@WithMockUser
	public void testDeletePolicyInvalid() throws Exception {
		BDDMockito.doThrow(new PolicyException("Invalid id")).when(policyService)
				.deletePolicy(BDDMockito.anyLong(), BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/policies/delete/2")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/policies"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "Cannot delete policy. Invalid id"));

		BDDMockito.verify(policyService).deletePolicy(2L, "user");
	}

	@Test
	@WithMockUser
	public void testHandlePolicyElementNotFound() throws Exception {
		BDDMockito.doThrow(new PolicyElementNotFoundException("not found")).when(
				policyService)
				.deletePolicy(1L, "user");
		mockMvc.perform(MockMvcRequestBuilders.post("/policies/delete/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/policies"))
				.andExpect(MockMvcResultMatchers.flash().attribute("failure",
						"not found"));
	}

}
