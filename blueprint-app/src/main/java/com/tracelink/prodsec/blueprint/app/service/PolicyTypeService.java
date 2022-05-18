package com.tracelink.prodsec.blueprint.app.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.repository.PolicyTypeRepository;

/**
 * Service to create and retrieve policy types.
 *
 * @author mcool
 */
@Service
public class PolicyTypeService {

	private final PolicyTypeRepository policyTypeRepository;

	public PolicyTypeService(@Autowired PolicyTypeRepository policyTypeRepository) {
		this.policyTypeRepository = policyTypeRepository;
	}

	/**
	 * Gets the names of all policy types in the database.
	 *
	 * @return list of policy types
	 */
	public List<String> getPolicyTypes() {
		return policyTypeRepository.findAll().stream()
				.map(PolicyTypeEntity::getName).sorted().collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Gets the policy type with the given name from the database.
	 *
	 * @param name the name of the policy type to retrieve
	 * @return the policy type
	 * @throws PolicyElementNotFoundException if no such policy type exists
	 */
	public PolicyTypeEntity getPolicyType(String name)
			throws PolicyElementNotFoundException {
		PolicyTypeEntity policyType = policyTypeRepository.findByName(name);
		if (policyType == null) {
			throw new PolicyElementNotFoundException(
					MessageFormat.format("There is no policy type with name ''{0}''", name));
		}
		return policyType;
	}

	/**
	 * Gets the policy type with the given name from the database, or creates it if it does not
	 * exist.
	 *
	 * @param name the name of the policy type to get or create
	 * @return the policy type
	 */
	public PolicyTypeEntity getOrCreatePolicyType(String name) {
		PolicyTypeEntity policyType = policyTypeRepository.findByName(name);
		if (policyType == null) {
			return createPolicyType(name);
		}
		return policyType;
	}

	private PolicyTypeEntity createPolicyType(String name) {
		PolicyTypeEntity policyType = new PolicyTypeEntity();
		policyType.setName(name);
		return policyTypeRepository.saveAndFlush(policyType);
	}

}
