package com.tracelink.prodsec.blueprint.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;

/**
 * Repository to save and retrieve {@link BaseStatementFunctionEntity}s.
 *
 * @author mcool
 */
@Repository(value = "baseStatementFunctionRepository")
public interface BaseStatementFunctionRepository extends
		JpaRepository<BaseStatementFunctionEntity, Long> {

}
