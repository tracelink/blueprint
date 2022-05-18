package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;
import org.hamcrest.Matchers;
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
public class ExportControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private PolicyService policyService;

	@Test
	@WithMockUser
	public void testGetExport() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/export"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("export")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("scripts", Matchers.contains("/scripts/export.js")));
	}

	@Test
	@WithMockUser
	public void testExportPolicy() throws Exception {
		BDDMockito.when(policyService.exportPolicy(BDDMockito.any(PolicyDto.class)))
				.thenReturn("default allow = false");

		mockMvc.perform(MockMvcRequestBuilders.post("/export")
				.param("policyType", "foo")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("export"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("rego", "default allow = false"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("policy"));

		BDDMockito.verify(policyService).exportPolicy(BDDMockito.any(PolicyDto.class));
	}

	@Test
	@WithMockUser
	public void testExportPolicyValidationErrors() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/export")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?type=null"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "The policy has validation errors"))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists("org.springframework.validation.BindingResult.policy"));

		BDDMockito.verify(policyService, Mockito.times(0))
				.exportPolicy(BDDMockito.any(PolicyDto.class));
	}

	@Test
	@WithMockUser
	public void testSavePolicyException() throws Exception {
		BDDMockito.doThrow(new PolicyException("Error")).when(policyService)
				.exportPolicy(BDDMockito.any(PolicyDto.class));

		mockMvc.perform(MockMvcRequestBuilders.post("/export")
				.param("name", "My Policy")
				.param("policyType", "foo")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?type=foo"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "Error"));

		BDDMockito.verify(policyService).exportPolicy(BDDMockito.any(PolicyDto.class));
	}

}
