package com.tracelink.prodsec.blueprint.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;

/**
 * Repository to store and retrieve database {@link PolicyEntity} objects.
 *
 * @author csmith
 */
@Repository(value = "policyRepository")
public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {

	PolicyEntity findByName(String name);
}
