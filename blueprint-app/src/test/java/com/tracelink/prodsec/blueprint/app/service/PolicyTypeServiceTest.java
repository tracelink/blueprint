package com.tracelink.prodsec.blueprint.app.service;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PolicyTypeServiceTest {

	@MockBean
	private PolicyTypeRepository policyTypeRepository;

	private PolicyTypeService policyTypeService;

	@Before
	public void setup() {
		policyTypeService = new PolicyTypeService(policyTypeRepository);
	}

	@Test
	public void testGetPolicyTypes() {
		PolicyTypeEntity policyType1 = new PolicyTypeEntity();
		policyType1.setName("Foo");
		PolicyTypeEntity policyType2 = new PolicyTypeEntity();
		policyType2.setName("Bar");
		BDDMockito.when(policyTypeRepository.findAll())
				.thenReturn(Arrays.asList(policyType1, policyType2));
		Assert.assertEquals(Arrays.asList("Bar", "Foo"), policyTypeService.getPolicyTypes());
	}

	@Test
	public void testGetPolicyType() throws Exception {
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName("Foo");
		BDDMockito.when(policyTypeRepository.findByName(BDDMockito.anyString()))
				.thenReturn(policyType);
		Assert.assertEquals(policyType, policyTypeService.getPolicyType("Foo"));
	}

	@Test
	public void testGetPolicyTypeNotFound() {
		try {
			policyTypeService.getPolicyType("Foo");
		} catch (PolicyElementNotFoundException e) {
			Assert.assertEquals("There is no policy type with name 'Foo'", e.getMessage());
		}
	}

}
