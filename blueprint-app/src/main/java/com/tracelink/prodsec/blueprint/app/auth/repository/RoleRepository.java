package com.tracelink.prodsec.blueprint.app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.auth.model.RoleEntity;

/**
 * Repository to store and retrieve database {@link RoleEntity} objects.
 *
 * @author csmith
 */
@Repository(value = "roleRepository")
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

	RoleEntity findByRoleName(String roleName);

	RoleEntity findByDefaultRoleTrue();
}
