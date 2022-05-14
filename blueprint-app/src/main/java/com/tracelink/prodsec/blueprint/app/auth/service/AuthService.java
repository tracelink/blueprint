package com.tracelink.prodsec.blueprint.app.auth.service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.auth.model.RoleEntity;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.repository.RoleRepository;
import com.tracelink.prodsec.blueprint.app.auth.repository.UserRepository;

/**
 * Service to store and retrieve {@link UserEntity} and {@link RoleEntity} from the database, and
 * to handle user creation and deletion. Also contains logic to change user passwords.
 *
 * @author mcool
 */
@Service
public class AuthService implements UserDetailsService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public AuthService(@Autowired PasswordEncoder passwordEncoder,
			@Autowired UserRepository userRepository,
			@Autowired RoleRepository roleRepository) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	/**
	 * Adds a new user to the database. Hashes the password in the registration
	 * entity, sets the enabled flag, and sets the USER role. This should only be
	 * used for brand-new users
	 *
	 * @param username the user's username
	 * @param password the user's password
	 * @throws UserAccountException if the user already exists
	 */
	public void registerNewUser(String username, String password) throws UserAccountException {
		UserEntity user = createUser(username, password);
		RoleEntity defaultUserRole = roleRepository.findByDefaultRoleTrue();
		if (defaultUserRole != null) {
			user.setRoles(new HashSet<>(Collections.singleton(defaultUserRole)));
		}
		saveUser(user);
	}

	private UserEntity createUser(String username, String password) throws UserAccountException {
		if (findByUsername(username) != null) {
			throw new UserAccountException("User already exists");
		}
		UserEntity user = new UserEntity();
		user.setCreated(new Date());
		user.setUsername(username);
		checkPassword(password);
		user.setPassword(passwordEncoder.encode(password));
		user.setEnabled(1);
		return user;
	}

	private void checkPassword(String password) throws UserAccountException {
		if (password.length() < 20) {
			throw new UserAccountException("Password must be 20 characters or more");
		}
		if (password.toLowerCase().contains("password")) {
			throw new UserAccountException(
					"Don't put the word password in your password... I mean... yikes...");
		}
	}

	/**
	 * Gets the database user whose username matches the given username.
	 *
	 * @param username of user to be found
	 * @return user with the given username, if any
	 */
	public UserEntity findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	/**
	 * Gets the database user whose SSO ID matches the given ID.
	 *
	 * @param ssoId of user to be found
	 * @return user with the given SSO ID, if any
	 */
	public UserEntity findBySsoId(String ssoId) {
		return userRepository.findBySsoId(ssoId);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity user = findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("Unknown username");
		}

		return buildUser(user.getUsername(), user.getPassword(), user);
	}

	private UserDetails buildUser(String username, String password, UserEntity user) {
		return User.builder().username(username).password(password).disabled(user.getEnabled() == 0)
				.accountExpired(false).accountLocked(false).credentialsExpired(false)
				.authorities(user.getRoles()
						.stream().map(RoleEntity::getRoleName).collect(Collectors.toList())
						.toArray(new String[]{}))
				.build();
	}

	@PostConstruct
	public void setupDefaultAuth() {
		setupDefaultRoles();
		setupDefaultUsers();
	}

	private void setupDefaultRoles() {
		for (CoreRole role : CoreRole.values()) {
			RoleEntity roleEntity = roleRepository.findByRoleName(role.getName());
			if (roleEntity == null) {
				roleEntity = new RoleEntity();
				roleEntity.setRoleName(role.getName());
				roleEntity.setDefaultRole(role.isDefaultRole());
			}
			roleEntity.setDescription(role.getDescription());
			roleRepository.save(roleEntity);
		}
		roleRepository.flush();
	}

	private void setupDefaultUsers() {
		// find any user (not the admin user) with an admin role
		boolean existingAdmin = userRepository.findAll().stream()
				.filter(u -> !u.getUsername().equals("admin"))
				.flatMap(u -> u.getRoles().stream())
				.anyMatch(r -> r.getRoleName().equals(CoreRole.ADMIN_ROLE));
		if (existingAdmin) {
			// admin exists, quit
			return;
		}
		// no admin, update/create it
		UserEntity admin = userRepository.findByUsername("admin");
		if (admin == null) {
			admin = new UserEntity();
			admin.setUsername("admin");
			admin.setEnabled(1);
			admin.setCreated(new Date());
		}
		String password = createAdminPassword();
		System.out.println("Password: " + password);
		admin.setPassword(passwordEncoder.encode(password));
		admin.setRoles(new HashSet<>(
				Collections.singleton(roleRepository.findByRoleName(CoreRole.ADMIN_ROLE))));
		saveUser(admin);
	}

	private String createAdminPassword() {
		SecureRandom rand = new SecureRandom();
		byte[] arr = new byte[10];
		rand.nextBytes(arr);
		return new String(Hex.encode(arr));
	}

	/**
	 * Change a user's password given their username and current and new passwords
	 *
	 * @param name            the user's username
	 * @param currentPassword the user's current password
	 * @param newPassword     the user's new password
	 * @throws UserAccountException    if the password cannot be changed
	 * @throws AuthenticationException if the current password does not match
	 */
	public void changePassword(String name, String currentPassword, String newPassword)
			throws UserAccountException, AuthenticationException {
		UserEntity user = findByUsername(name);
		if (user == null) {
			throw new UserAccountException("User does not exist");
		}
		if (user.getSsoId() != null) {
			throw new UserAccountException(
					"You cannot update your password if you authenticate using SSO.");
		}
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new BadCredentialsException("Your current password is invalid");
		}
		checkPassword(newPassword);
		user.setPassword(passwordEncoder.encode(newPassword));
		saveUser(user);
	}

	/**
	 * Override a user's password given their username and new passwords
	 *
	 * @param userId      the user's id
	 * @param newPassword the user's new password
	 * @throws UserAccountException if the password cannot be changed
	 */
	public void changePasswordOverride(long userId, String newPassword)
			throws UserAccountException {
		Optional<UserEntity> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) {
			throw new UserAccountException("Unknown User");
		}
		UserEntity user = userOpt.get();
		checkPassword(newPassword);
		user.setPassword(passwordEncoder.encode(newPassword));
		saveUser(user);
	}

	/**
	 * Updates the given user in the database.
	 *
	 * @param user to be updated
	 */
	public void saveUser(UserEntity user) {
		user.setLastModified(new Date());
		userRepository.saveAndFlush(user);
	}

	/**
	 * Deletes the given user from the database.
	 *
	 * @param user to be deleted
	 */
	public void deleteUser(UserEntity user) {
		userRepository.delete(user);
	}

	/**
	 * Gets a list of all users in the database.
	 *
	 * @return list of users
	 */
	public List<UserEntity> findAllUsers() {
		return userRepository.findAll();
	}

	public void setUserRole(Long userid, Long roleId) throws UserAccountException {
		Optional<UserEntity> userOpt = userRepository.findById(userid);
		Optional<RoleEntity> roleOpt = roleRepository.findById(roleId);
		if (userOpt.isEmpty()) {
			throw new UserAccountException("Unknown user id");
		}
		if (roleOpt.isEmpty()) {
			throw new UserAccountException("Unknown role id");
		}
		UserEntity user = userOpt.get();
		user.setRoles(new HashSet<>(Collections.singleton(roleOpt.get())));
		saveUser(user);
	}

	/**
	 * Updates the last login date of a user upon successful authentication.
	 *
	 * @param event the authentication event
	 */
	@EventListener
	public void onLogin(AuthenticationSuccessEvent event) {
		UserEntity user = findByUsername(event.getAuthentication().getName());
		user.setLastLogin(new Date(event.getTimestamp()));
		userRepository.saveAndFlush(user);
	}

	/**
	 * Gets the user with the given id
	 *
	 * @param userid id of the user
	 * @return the user with the given id
	 * @throws UserAccountException if the user does not exist
	 */
	public UserEntity findById(Long userid) throws UserAccountException {
		Optional<UserEntity> user = userRepository.findById(userid);
		if (user.isEmpty()) {
			throw new UserAccountException("Unknown User");
		}
		return user.get();
	}

	/**
	 * Gets a list of all roles in the database.
	 *
	 * @return list of roles
	 */
	public List<RoleEntity> findAllRoles() {
		return roleRepository.findAll();
	}


	/**
	 * Finds the default role.
	 *
	 * @return the default role or null if it doesn't exist
	 */
	public RoleEntity findDefaultRole() {
		return roleRepository.findByDefaultRoleTrue();
	}
}
