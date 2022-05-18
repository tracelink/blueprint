package com.tracelink.prodsec.blueprint.app.controller;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
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
public class PolicyBuilderControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private PolicyService policyService;
	@MockBean
	private BaseStatementService baseStatementService;
	@MockBean
	private PolicyTypeService policyTypeService;

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
	public void testGetPolicyBuilder() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(policyService.getPolicies()).thenReturn(Collections.emptyMap());

		mockMvc.perform(MockMvcRequestBuilders.get("/"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("builder")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("policies", Matchers.emptyIterable()));
	}

	@Test
	@WithMockUser
	public void testGetPolicyBuilderWithName() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("Foo", 1L));
		BDDMockito.when(policyService.getPolicy("Foo")).thenReturn(policy);
		BDDMockito.when(baseStatementService
				.getLatestBaseStatementsForPolicyType(policy.getPolicyType()))
				.thenReturn(Collections.emptyMap());

		mockMvc.perform(MockMvcRequestBuilders.get("/?name=Foo"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("builder")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("policies", Matchers.contains("Foo")))
				.andExpect(MockMvcResultMatchers.model().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.model().attribute("policyType", "foo"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.anEmptyMap()));

		BDDMockito.verify(baseStatementService)
				.getLatestBaseStatementsForPolicyType(policy.getPolicyType());
	}

	@Test
	@WithMockUser
	public void testGetPolicyBuilderWithInvalidName() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("Foo", 1L));
		BDDMockito.doThrow(new PolicyElementNotFoundException("Invalid policy"))
				.when(policyService).getPolicy("Foo");

		mockMvc.perform(MockMvcRequestBuilders.get("/?name=Foo"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl(""))
				.andExpect(MockMvcResultMatchers.model()
						.attributeDoesNotExist("policy", "policyType", "baseStatements"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "Invalid policy"));

		BDDMockito.verify(baseStatementService, Mockito.times(0))
				.getLatestBaseStatementsForPolicyType(BDDMockito.any(PolicyTypeEntity.class));
	}

	@Test
	@WithMockUser
	public void testGetPolicyBuilderWithType() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("Foo", 1L));
		BDDMockito.when(policyTypeService.getPolicyType("foo")).thenReturn(policyType);
		BDDMockito.when(baseStatementService
				.getLatestBaseStatementsForPolicyType(policyType))
				.thenReturn(Collections.emptyMap());

		mockMvc.perform(MockMvcRequestBuilders.get("/?type=foo"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.model()
						.attribute("contentViewName", Matchers.is("builder")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("policyTypes", Matchers.contains("foo")))
				.andExpect(
						MockMvcResultMatchers.model()
								.attribute("policies", Matchers.contains("Foo")))
				.andExpect(MockMvcResultMatchers.model().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.model().attribute("policyType", "foo"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("baseStatements", Matchers.anEmptyMap()));

		BDDMockito.verify(baseStatementService)
				.getLatestBaseStatementsForPolicyType(policyType);
	}

	@Test
	@WithMockUser
	public void testGetPolicyBuilderWithInvalidType() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyTypes())
				.thenReturn(Collections.singletonList("foo"));
		BDDMockito.when(policyService.getPolicies())
				.thenReturn(Collections.singletonMap("Foo", 1L));
		BDDMockito.doThrow(new PolicyElementNotFoundException("Invalid policy type"))
				.when(policyTypeService).getPolicyType("bar");

		mockMvc.perform(MockMvcRequestBuilders.get("/?type=bar"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl(""))
				.andExpect(MockMvcResultMatchers.model()
						.attributeDoesNotExist("policy", "policyType", "baseStatements"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "Invalid policy type"));

		BDDMockito.verify(baseStatementService, Mockito.times(0))
				.getLatestBaseStatementsForPolicyType(BDDMockito.any(PolicyTypeEntity.class));
	}

	@Test
	@WithMockUser
	public void testImportPolicy() throws Exception {
		BDDMockito.when(policyService.importPolicy(BDDMockito.anyString()))
				.thenReturn(policy.toDto());

		mockMvc.perform(MockMvcRequestBuilders.post("/import")
				.param("json", "{}")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?type=foo"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("policy"));

		BDDMockito.verify(policyService).importPolicy("{}");
	}

	@Test
	@WithMockUser
	public void testImportPolicyInvalid() throws Exception {
		BDDMockito.doThrow(new PolicyException("Invalid")).when(policyService)
				.importPolicy(BDDMockito.anyString());

		mockMvc.perform(MockMvcRequestBuilders.post("/import")
				.param("json", "{}")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl(""))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "The imported policy is invalid: Invalid"));

		BDDMockito.verify(policyService).importPolicy("{}");
	}

	@Test
	@WithMockUser
	public void testSavePolicy() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/save")
				.param("name", "My Policy")
				.param("policyType", "foo")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?name=My Policy"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("success", "Saved policy 'My Policy'"));

		BDDMockito.verify(policyService)
				.savePolicy(BDDMockito.any(PolicyDto.class));
	}

	@Test
	@WithMockUser
	public void testSavePolicyValidationErrors() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/save")
				.param("name", "My Policy")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?type=null"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "The policy has validation errors"))
				.andExpect(MockMvcResultMatchers.flash()
						.attributeExists("org.springframework.validation.BindingResult.policy"));

		BDDMockito.verify(policyService, Mockito.times(0))
				.savePolicy(BDDMockito.any(PolicyDto.class));
	}

	@Test
	@WithMockUser
	public void testSavePolicyException() throws Exception {
		BDDMockito.doThrow(new PolicyException("Error")).when(policyService)
				.savePolicy(BDDMockito.any(PolicyDto.class));

		mockMvc.perform(MockMvcRequestBuilders.post("/save")
				.param("name", "My Policy")
				.param("policyType", "foo")
				.param("clauses[0].statements[0].baseStatementName", "bar")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("?type=foo"))
				.andExpect(MockMvcResultMatchers.flash().attributeExists("policy"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute("failure", "Cannot save policy. Error"));

		BDDMockito.verify(policyService)
				.savePolicy(BDDMockito.any(PolicyDto.class));
	}

}
