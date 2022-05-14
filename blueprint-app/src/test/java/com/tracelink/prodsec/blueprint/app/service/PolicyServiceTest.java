package com.tracelink.prodsec.blueprint.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyImportException;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;

@RunWith(SpringRunner.class)
public class PolicyServiceTest {

	@MockBean
	private BaseStatementService baseStatementService;
	@MockBean
	private PolicyRepository policyRepository;

	private PolicyService policyService;
	private PolicyTypeEntity policyType;

	@Before
	public void setup() {
		policyService = new PolicyService(baseStatementService, policyRepository);
		policyType = new PolicyTypeEntity();
		policyType.setName("System");
	}

	@Test
	public void testImportPolicyJsonException() {
		try {
			policyService.importPolicy("foo");
			Assert.fail();
		} catch (PolicyImportException e) {
			Assert.assertTrue(e.getMessage().startsWith(
					"The imported policy is not formatted correctly: Unrecognized token 'foo'"));
		}
	}

	@Test
	public void testImportPolicy() throws Exception {
		PolicyDto policyDto = policyService
				.importPolicy("{\"policyType\":\"System\", \"clauses\":[{\"statements\":[]}]}");
		Assert.assertEquals(1, policyDto.getClauses().size());
		Assert.assertTrue(policyDto.getClauses().get(0).getStatements().isEmpty());
		Assert.assertEquals("System", policyDto.getPolicyType());
	}

	@Test
	public void testImportPolicyBlankPolicyType() throws Exception {
		try {
			policyService
					.importPolicy("{\"policyType\":\"\", \"clauses\":[{\"statements\":[]}]}");
			Assert.fail();
		} catch (PolicyImportException e) {
			Assert.assertEquals("The imported policy must have a policy type", e.getMessage());
		}
	}

	@Test
	public void testExportPolicyConstraintViolations() {
		PolicyDto policyDto = new PolicyDto();
		try {
			policyService.exportPolicy(policyDto);
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertTrue(e.getMessage().contains("Policy validation failed: "));
			Assert.assertTrue(e.getMessage().contains("A policy must have at least one clause"));
			Assert.assertTrue(e.getMessage().contains("A policy must have a type"));
		}
	}

	@Test
	public void testExportPolicy() throws Exception {
		PolicyDto policyDto = createValidPolicyDto();
		BaseStatementEntity baseStatement = createValidBaseStatementEntity();

		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);

		String rego = policyService.exportPolicy(policyDto);
		Assert.assertEquals(
				"default allow = false\n\nallow {\n\tnot function_name\n}\n\nfunction_name {\n\t1 == 1\n}\n\n",
				rego);

	}

	@Test
	public void testExportPolicyNegationNotAllowed() throws Exception {
		PolicyDto policyDto = createValidPolicyDto();
		BaseStatementEntity baseStatement = createValidBaseStatementEntity();
		baseStatement.setNegationAllowed(false);

		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);

		String rego = policyService.exportPolicy(policyDto);
		Assert.assertEquals(
				"default allow = false\n\nallow {\n\tfunction_name\n}\n\nfunction_name {\n\t1 == 1\n}\n\n",
				rego);

	}

	@Test
	public void testGetPolicies() {
		PolicyEntity policy1 = new PolicyEntity();
		policy1.setName("Foo");
		PolicyEntity policy2 = new PolicyEntity();
		policy2.setName("Bar");
		List<PolicyEntity> policies = new ArrayList<>();
		policies.add(policy1);
		policies.add(policy2);
		BDDMockito.when(policyRepository.findAll()).thenReturn(policies);
		Map<String, Long> result = policyService.getPolicies();
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("Bar", result.entrySet().iterator().next().getKey());
		Assert.assertTrue(result.containsKey("Foo"));
	}

	@Test
	public void testGetPolicyById() {
		PolicyEntity policy = new PolicyEntity();
		policy.setName("Foo");
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
		BDDMockito.when(policyRepository.findById(2L)).thenReturn(Optional.empty());
		Assert.assertEquals(policy, policyService.getPolicy(1L));
		Assert.assertNull(policyService.getPolicy(2L));
	}

	@Test
	public void testGetPolicyByName() {
		PolicyEntity policy = new PolicyEntity();
		policy.setName("Foo");
		BDDMockito.when(policyRepository.findByName("Foo")).thenReturn(policy);
		Assert.assertEquals(policy, policyService.getPolicy("Foo"));
	}

	@Test
	public void testSavePolicyBlankName() {
		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());

		try {
			policyService.savePolicy(createValidPolicyDto(), "", "user");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("A policy name cannot be blank", e.getMessage());
		}
	}

	@Test
	public void testSavePolicyWrongAuthor() {
		PolicyEntity policy = new PolicyEntity();
		policy.setAuthor("user2");
		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());
		BDDMockito.when(policyRepository.findByName("Name")).thenReturn(policy);
		try {
			policyService.savePolicy(createValidPolicyDto(), "Name", "user");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("A policy with that name already exists and cannot be overwritten",
					e.getMessage());
		}
	}

	@Test
	public void testSavePolicyNew() throws Exception {
		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());

		policyService.savePolicy(createValidPolicyDto(), "Name", "user");
		ArgumentCaptor<PolicyEntity> policyCaptor = ArgumentCaptor.forClass(PolicyEntity.class);
		BDDMockito.verify(policyRepository).saveAndFlush(policyCaptor.capture());
		Assert.assertEquals("Name", policyCaptor.getValue().getName());
		Assert.assertEquals("user", policyCaptor.getValue().getAuthor());
	}

	@Test
	public void testSavePolicyExisting() throws Exception {
		BaseStatementEntity baseStatement = createValidBaseStatementEntity();
		baseStatement.setNegationAllowed(false);
		PolicyEntity policy = new PolicyEntity();
		policy.setAuthor("user");
		BDDMockito.when(baseStatementService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);
		BDDMockito.when(policyRepository.findByName("Name")).thenReturn(policy);

		policyService.savePolicy(createValidPolicyDto(), "Name", "user");
		BDDMockito.verify(policyRepository).saveAndFlush(policy);
		Assert.assertEquals("Name", policy.getName());
		Assert.assertEquals("user", policy.getAuthor());
	}

	@Test
	public void testDeletePolicyInvalidId() {
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.empty());
		try {
			policyService.deletePolicy(1L, "user");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("Invalid policy id", e.getMessage());
		}
	}

	@Test
	public void testDeletePolicyNotOwner() {
		PolicyEntity policy = new PolicyEntity();
		policy.setAuthor("user2");
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
		try {
			policyService.deletePolicy(1L, "user");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("You are not the author of the policy", e.getMessage());
		}
	}

	@Test
	public void testDeletePolicy() throws Exception {
		PolicyEntity policy = new PolicyEntity();
		policy.setAuthor("user");
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

		Assert.assertEquals(policy, policyService.deletePolicy(1L, "user"));
		BDDMockito.verify(policyRepository).delete(policy);
		BDDMockito.verify(policyRepository).flush();
	}

	private PolicyDto createValidPolicyDto() {
		ConfiguredStatementDto statement = new ConfiguredStatementDto();
		statement.setBaseStatementName("Base Statement");
		statement.setNegated(true);

		PolicyClauseDto clause = new PolicyClauseDto();
		clause.setStatements(Collections.singletonList(statement));

		PolicyDto policy = new PolicyDto();
		policy.setPolicyType("System");
		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

	private BaseStatementEntity createValidBaseStatementEntity() {
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("System");
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		function.setName("function_name");
		function.setDescription("A Rego function");
		function.setPolicyTypes(Collections.singleton(policyType));
		function.setExpression("1 == 1");
		function.setParameters(Collections.emptyList());
		function.setDependencies(Collections.emptySet());

		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("Base Statement");
		baseStatement.setDescription("This is a base statement");
		baseStatement.setPolicyTypes(Collections.singleton(policyType));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function);
		baseStatement.setArguments(Collections.emptyList());
		return baseStatement;
	}

}
