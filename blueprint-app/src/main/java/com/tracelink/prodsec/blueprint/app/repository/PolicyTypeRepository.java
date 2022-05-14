package com.tracelink.prodsec.blueprint.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;

/**
 * Repository to store and retrieve database {@link PolicyTypeEntity} objects.
 *
 * @author mcool
 */
@Repository(value = "policyTypeRepository")
public interface PolicyTypeRepository extends JpaRepository<PolicyTypeEntity, Long> {

	PolicyTypeEntity findByName(String name);
}
