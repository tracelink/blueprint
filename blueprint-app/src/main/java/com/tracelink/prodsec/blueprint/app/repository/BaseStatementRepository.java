package com.tracelink.prodsec.blueprint.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Repository to save and retrieve {@link BaseStatementEntity}s.
 *
 * @author mcool
 */
@Repository(value = "baseStatementRepository")
public interface BaseStatementRepository extends JpaRepository<BaseStatementEntity, Long> {

	/**
	 * Gets the base statement with the given name and version, if it exists
	 *
	 * @param name    the name of the base statement to get
	 * @param version the version of the base statement to get
	 * @return the function, or {@link Optional#empty()} if the function does not exist
	 */
	Optional<BaseStatementEntity> findByNameAndVersion(String name, int version);

	/**
	 * Gets a list of all base statements, ordering by name, state (released first, then draft and
	 * finally deprecated), and version (newest versions first).
	 *
	 * @return list of all base statements
	 */
	List<BaseStatementEntity> findAllByOrderByNameAscStateDescVersionDesc();

	/**
	 * Gets a list of all base statements in the given state that are valid for one of the given
	 * policy types, ordering by name and version (newest versions first).
	 *
	 * @param state       the state of the base statements to get
	 * @param policyTypes the policy types that are valid for the base statements to get
	 * @return list of base statements in the given state and one of the given policy types
	 */
	List<BaseStatementEntity> findAllByStateAndPolicyTypesInOrderByNameAscVersionDesc(
			PolicyElementState state, Set<PolicyTypeEntity> policyTypes);

	/**
	 * Gets the newest version of the base statement with the given name, if it exists. Note that
	 * the base statement may be in any state.
	 *
	 * @param name the name of the base statement to find
	 * @return the function, or null if no base statements with the given name exist
	 */
	BaseStatementEntity findFirstByNameOrderByVersionDesc(String name);

	/**
	 * Gets the newest version of the base statement with the given name and state, if it exists.
	 *
	 * @param name  the name of the base statement to find
	 * @param state the state of the base statement to find
	 * @return the function, or null if no base statements with the given name and state exist
	 */
	BaseStatementEntity findFirstByNameAndStateOrderByVersionDesc(String name,
			PolicyElementState state);

	/**
	 * Gets the previous version of the base statement with the given name and version.
	 *
	 * @param name    the name of the base statement to get the previous version of
	 * @param version the version of the base statement to get the previous version of
	 * @return the previous version, or {@link Optional#empty()} if no such base statement exists
	 */
	Optional<BaseStatementEntity> findFirstByNameAndVersionLessThanOrderByVersionDesc(
			String name, int version);

	/**
	 * Gets the next version of the base statement with the given name and version.
	 *
	 * @param name    the name of the base statement to get the next version of
	 * @param version the version of the base statement to get the next version of
	 * @return the previous version, or {@link Optional#empty()} if no such base statement exists
	 */
	Optional<BaseStatementEntity> findFirstByNameAndVersionGreaterThanOrderByVersionAsc(
			String name, int version);

	/**
	 * Gets the newest version of the base statement with the given name and state whose version is
	 * greater than the given version, if it exists.
	 *
	 * @param name    the name of the base statement to find
	 * @param version the version minimum (exclusive) of the base statement to find
	 * @param state   the state of the base statement to find
	 * @return the base statement, or {@link Optional#empty()} if no such base statement exists
	 */
	Optional<BaseStatementEntity> findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc(
			String name, int version, PolicyElementState state);
}
