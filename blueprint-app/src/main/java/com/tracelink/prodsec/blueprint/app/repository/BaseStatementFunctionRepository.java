package com.tracelink.prodsec.blueprint.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Repository to save and retrieve {@link BaseStatementFunctionEntity}s.
 *
 * @author mcool
 */
@Repository(value = "baseStatementFunctionRepository")
public interface BaseStatementFunctionRepository extends
		JpaRepository<BaseStatementFunctionEntity, Long> {

	/**
	 * Gets the function with the given name and version, if it exists
	 *
	 * @param name    the name of the function to get
	 * @param version the version of the function to get
	 * @return the function, or {@link Optional#empty()} if the function does not exist
	 */
	Optional<BaseStatementFunctionEntity> findByNameAndVersion(String name, int version);

	/**
	 * Gets a list of all functions, ordering by name, state (released first, then draft and finally
	 * deprecated), and version (newest versions first).
	 *
	 * @return list of all functions
	 */
	List<BaseStatementFunctionEntity> findAllByOrderByNameAscStateDescVersionDesc();

	/**
	 * Gets a list of all functions in the given state, ordering by name and version (newest
	 * versions first).
	 *
	 * @param state the state of the functions to get
	 * @return list of functions in the given state
	 */
	List<BaseStatementFunctionEntity> findAllByStateOrderByNameAscVersionDesc(
			PolicyElementState state);

	/**
	 * Gets the newest version of the function with the given name, if it exists. Note that the
	 * function may be in any state.
	 *
	 * @param name the name of the function to find
	 * @return the function, or null if no functions with the given name exist
	 */
	BaseStatementFunctionEntity findFirstByNameOrderByVersionDesc(String name);

	/**
	 * Gets the newest version of the function with the given name and state, if it exists.
	 *
	 * @param name  the name of the function to find
	 * @param state the state of the function to find
	 * @return the function, or null if no functions with the given name and state exist
	 */
	BaseStatementFunctionEntity findFirstByNameAndStateOrderByVersionDesc(String name,
			PolicyElementState state);

	/**
	 * Gets the previous version of the function with the given name and version.
	 *
	 * @param name    the name of the function to get the previous version of
	 * @param version the version of the function to get the previous version of
	 * @return the previous version, or {@link Optional#empty()} if no such function exists
	 */
	Optional<BaseStatementFunctionEntity> findFirstByNameAndVersionLessThanOrderByVersionDesc(
			String name, int version);

	/**
	 * Gets the next version of the function with the given name and version.
	 *
	 * @param name    the name of the function to get the next version of
	 * @param version the version of the function to get the next version of
	 * @return the previous version, or {@link Optional#empty()} if no such function exists
	 */
	Optional<BaseStatementFunctionEntity> findFirstByNameAndVersionGreaterThanOrderByVersionAsc(
			String name, int version);

	/**
	 * Gets the newest version of the function with the given name and state whose version is
	 * greater than the given version, if it exists.
	 *
	 * @param name    the name of the function to find
	 * @param version the version minimum (exclusive) of the function to find
	 * @param state   the state of the function to find
	 * @return the function, or {@link Optional#empty()} if no such function exists
	 */
	Optional<BaseStatementFunctionEntity> findFirstByNameAndVersionGreaterThanAndStateOrderByVersionDesc(
			String name, int version, PolicyElementState state);
}
