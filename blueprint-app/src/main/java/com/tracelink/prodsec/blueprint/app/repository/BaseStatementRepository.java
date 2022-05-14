package com.tracelink.prodsec.blueprint.app.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;

/**
 * Repository to save and retrieve {@link BaseStatementEntity}s.
 *
 * @author mcool
 */
@Repository(value = "baseStatementRepository")
public interface BaseStatementRepository extends JpaRepository<BaseStatementEntity, Long> {

	BaseStatementEntity findByName(String name);

	List<BaseStatementEntity> findByPolicyTypesInOrderByNameAsc(Set<PolicyTypeEntity> policyTypes);
}
