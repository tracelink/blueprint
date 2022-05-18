package com.tracelink.prodsec.blueprint.app.service;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementDto;
import com.tracelink.prodsec.blueprint.app.policy.ConfiguredStatementEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyClauseEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyRepository;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;
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

@RunWith(SpringRunner.class)
public class PolicyServiceTest {

	@MockBean
	private BaseStatementService baseStatementService;
	@MockBean
	private PolicyTypeService policyTypeService;
	@MockBean
	private PolicyRepository policyRepository;

	private PolicyService policyService;
	private PolicyTypeEntity policyType;

	@Before
	public void setup() {
		policyService = new PolicyService(baseStatementService, policyTypeService,
				policyRepository);
		policyType = new PolicyTypeEntity();
		policyType.setName("System");
	}

	@Test
	public void testImportPolicyJsonException() throws Exception {
		try {
			policyService.importPolicy("foo");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals(
					"The imported policy is not formatted correctly. Check the log for more details",
					e.getMessage());
		}
	}

	@Test
	public void testImportPolicyInvalid() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyType("Foo"))
				.thenReturn(new PolicyTypeEntity("Foo"));
		try {
			policyService
					.importPolicy("{\"policyType\":\"Foo\", \"clauses\":[{\"statements\":[]}]}");
		} catch (PolicyException e) {
			Assert.assertEquals(
					"Policy validation failed: A policy clause must have at least one configured statement",
					e.getMessage());
		}
	}

	@Test
	public void testImportPolicyValid() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyType("System"))
				.thenReturn(new PolicyTypeEntity("System"));
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());
		PolicyDto policyDto = policyService
				.importPolicy(
						"{\"policyType\":\"System\", \"clauses\":[{\"statements\":[{\"baseStatementName\": \"Base Statement\"}]}]}");
		Assert.assertEquals(1, policyDto.getClauses().size());
		Assert.assertFalse(policyDto.getClauses().get(0).getStatements().isEmpty());
		Assert.assertEquals("System", policyDto.getPolicyType());
	}

	@Test
	public void testImportPolicyBlankPolicyType() throws Exception {
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());
		try {
			policyService
					.importPolicy(
							"{\"policyType\":\"\", \"clauses\":[{\"statements\":[{\"baseStatementName\": \"Base Statement\"}]}]}");
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("Policy validation failed: A policy must have a policy type",
					e.getMessage());
		}
	}

	@Test
	public void testExportPolicyConstraintViolations() throws Exception {
		PolicyDto policyDto = new PolicyDto();
		try {
			policyService.exportPolicy(policyDto);
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertTrue(e.getMessage().contains("Policy validation failed: "));
			Assert.assertTrue(e.getMessage().contains("A policy must have at least one clause"));
			Assert.assertTrue(e.getMessage().contains("A policy must have a policy type"));
		}
	}

	@Test
	public void testExportPolicy() throws Exception {
		PolicyDto policyDto = createValidPolicyDto();
		BaseStatementEntity baseStatement = createValidBaseStatementEntity();

		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);

		String rego = policyService.exportPolicy(policyDto);
		Assert.assertEquals(
				"package blueprint\n\ndefault allow = false\n\nallow {\n\tnot function_name\n}\n\nfunction_name {\n\t1 == 1\n}\n\n",
				rego);

	}

	@Test
	public void testExportPolicyNegationNotAllowed() throws Exception {
		PolicyDto policyDto = createValidPolicyDto();
		BaseStatementEntity baseStatement = createValidBaseStatementEntity();
		baseStatement.setNegationAllowed(false);

		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);

		String rego = policyService.exportPolicy(policyDto);
		Assert.assertEquals(
				"package blueprint\n\ndefault allow = false\n\nallow {\n\tfunction_name\n}\n\nfunction_name {\n\t1 == 1\n}\n\n",
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
	public void testGetPolicyByIdNotFound() throws Exception {
		PolicyEntity policy = new PolicyEntity();
		policy.setName("Foo");
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
		BDDMockito.when(policyRepository.findById(2L)).thenReturn(Optional.empty());
		Assert.assertEquals(policy, policyService.getPolicy(1L));
		try {
			policyService.getPolicy(2L);
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no policy with the id '2'", e.getMessage());
		}
	}

	@Test
	public void testGetPolicyByName() throws Exception {
		PolicyEntity policy = new PolicyEntity();
		policy.setName("Foo");
		BDDMockito.when(policyRepository.findByName("Foo")).thenReturn(policy);
		Assert.assertEquals(policy, policyService.getPolicy("Foo"));
	}

	@Test
	public void testGetPolicyByNameNotFound() {
		try {
			policyService.getPolicy("foo");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no policy with the name 'foo'", e.getMessage());
		}
	}

	@Test
	public void testSavePolicyBlankName() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());

		try {
			PolicyDto policyDto = createValidPolicyDto();
			policyDto.setName(null);
			policyService.savePolicy(policyDto);
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("Policy validation failed: Name cannot be blank", e.getMessage());
		}
	}

	@Test
	public void testSavePolicyWrongAuthor() throws Exception {
		PolicyEntity policy = new PolicyEntity();
		policy.setAuthor("user2");
		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());
		BDDMockito.when(policyRepository.findByName("Name")).thenReturn(policy);
		try {
			policyService.savePolicy(createValidPolicyDto());
			Assert.fail();
		} catch (PolicyException e) {
			Assert.assertEquals("A policy with that name already exists and cannot be overwritten",
					e.getMessage());
		}
	}

	@Test
	public void testSavePolicyNew() throws Exception {
		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(createValidBaseStatementEntity());

		policyService.savePolicy(createValidPolicyDto());
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
		policy.setName("Name");
		policy.setAuthor("user");
		BDDMockito.when(policyTypeService.getPolicyType("System")).thenReturn(policyType);
		BDDMockito.when(baseStatementService.getBaseStatement("Base Statement"))
				.thenReturn(baseStatement);
		BDDMockito.when(policyRepository.findByName("Name")).thenReturn(policy);

		policyService.savePolicy(createValidPolicyDto());
		BDDMockito.verify(policyRepository).saveAndFlush(policy);
		Assert.assertEquals("Name", policy.getName());
		Assert.assertEquals("user", policy.getAuthor());
	}

	@Test
	public void testDeletePolicyInvalidId() throws Exception {
		BDDMockito.when(policyRepository.findById(1L)).thenReturn(Optional.empty());
		try {
			policyService.deletePolicy(1L, "user");
			Assert.fail();
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no policy with the id '1'", e.getMessage());
		}
	}

	@Test
	public void testDeletePolicyNotOwner() throws Exception {
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

	@Test
	public void testGetUpdatedBaseStatements() {
		PolicyEntity policy = new PolicyEntity();
		PolicyClauseEntity clause = new PolicyClauseEntity();
		ConfiguredStatementEntity statement = new ConfiguredStatementEntity();
		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("base statement");
		baseStatement.setVersion(1);
		BaseStatementEntity updated = new BaseStatementEntity();
		updated.setName("base statement");
		updated.setVersion(2);
		statement.setBaseStatement(baseStatement);
		clause.setStatements(Collections.singletonList(statement));
		policy.setClauses(Collections.singletonList(clause));
		BDDMockito.when(baseStatementService.getUpdatedBaseStatement("base statement", 1))
				.thenReturn(Optional.of(updated));
		Assert.assertEquals(1, policyService.getUpdatedBaseStatements(policy).size());
		Assert.assertEquals("base statement:2",
				policyService.getUpdatedBaseStatements(policy).iterator().next());
	}

	public PolicyDto createValidPolicyDto() {
		ConfiguredStatementDto statement = new ConfiguredStatementDto();
		statement.setBaseStatementName("Base Statement");
		statement.setNegated(true);

		PolicyClauseDto clause = new PolicyClauseDto();
		clause.setStatements(Collections.singletonList(statement));

		PolicyDto policy = new PolicyDto();
		policy.setName("Name");
		policy.setAuthor("user");
		policy.setPolicyType("System");
		policy.setClauses(Collections.singletonList(clause));
		return policy;
	}

	private BaseStatementEntity createValidBaseStatementEntity() {
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("System");
		BaseStatementFunctionEntity function = new BaseStatementFunctionEntity();
		function.setName("function_name");
		function.setAuthor("user");
		function.setState(PolicyElementState.RELEASED);
		function.setDescription("A Rego function");
		function.setPolicyTypes(Collections.singleton(policyType));
		function.setExpression("1 == 1");
		function.setParameters(Collections.emptyList());
		function.setDependencies(Collections.emptySet());

		BaseStatementEntity baseStatement = new BaseStatementEntity();
		baseStatement.setName("Base Statement");
		baseStatement.setAuthor("user");
		baseStatement.setVersion(1);
		baseStatement.setState(PolicyElementState.RELEASED);
		baseStatement.setDescription("This is a base statement");
		baseStatement.setPolicyTypes(Collections.singleton(policyType));
		baseStatement.setNegationAllowed(true);
		baseStatement.setFunction(function);
		baseStatement.setArguments(Collections.emptyList());
		return baseStatement;
	}

}
