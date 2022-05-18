package com.tracelink.prodsec.blueprint.app.auth.service;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.auth.model.RoleEntity;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.repository.RoleRepository;
import com.tracelink.prodsec.blueprint.app.auth.repository.UserRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AuthServiceTest {

	private static final String ADMIN_USERNAME = "admin";

	@MockBean
	private UserRepository userRepo;
	@MockBean
	private RoleRepository roleRepo;

	private AuthService authService;

	@Before
	public void setup() {
		BDDMockito.when(roleRepo.findByRoleName(CoreRole.ADMIN_ROLE))
				.thenReturn(new RoleEntity());
		BDDMockito.when(userRepo.findByUsername(ADMIN_USERNAME))
				.thenReturn(new UserEntity());

		this.authService = new AuthService(passwordEncoder, userRepo, roleRepo);

		// reset counters on invocations for easier test counting
		BDDMockito.clearInvocations(roleRepo);
		BDDMockito.clearInvocations(userRepo);
	}

	// garbage no-op password encoder for testing
	private final PasswordEncoder passwordEncoder = new PassthroughPasswordEncoder();

	@Test
	public void findAllUsersTest() {
		authService.findAllUsers();
		BDDMockito.verify(userRepo).findAll();
	}

	@Test
	public void findByUsernameTest() {
		authService.findByUsername("");
		BDDMockito.verify(userRepo).findByUsername(BDDMockito.anyString());
	}

	@Test
	public void loadUserByUsernameSuccess() {
		String email = "my.email@example.com";
		String password = "myAwfulPw";
		String roleName = "myRole";

		RoleEntity role = new RoleEntity();
		role.setRoleName(roleName);

		UserEntity user = new UserEntity();
		user.setUsername(email);
		user.setPassword(password);
		user.setRoles(Collections.singleton(role));

		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);
		UserDetails userDetails = authService.loadUserByUsername("");

		BDDMockito.verify(userRepo).findByUsername(BDDMockito.anyString());
		Assert.assertEquals(email, userDetails.getUsername());
		Assert.assertEquals(password, userDetails.getPassword());
		Assert.assertTrue(userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals(roleName)));
	}

	@Test(expected = UsernameNotFoundException.class)
	public void findByUsernameFailUser() {
		authService.loadUserByUsername("");
	}

	@Test
	public void findByIdTest() throws Exception {
		BDDMockito.when(userRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(new UserEntity()));
		authService.findById(1L);
		BDDMockito.verify(userRepo).findById(BDDMockito.anyLong());
	}

	@Test
	public void saveUserTest() {
		authService.saveUser(new UserEntity());
		BDDMockito.verify(userRepo).saveAndFlush(BDDMockito.any());
	}

	@Test
	public void deleteUserTest() {
		authService.deleteUser(null);
		BDDMockito.verify(userRepo).delete(BDDMockito.any());
	}

	@Test
	public void findAllRolesTest() {
		authService.findAllRoles();
		BDDMockito.verify(roleRepo).findAll();
	}

	@Test
	public void testFindBySsoId() throws Exception {
		authService.findBySsoId("123");
		BDDMockito.verify(userRepo).findBySsoId(BDDMockito.anyString());
	}

	@Test
	public void testFindByIdUnknown() {
		try {
			authService.findById(123L);
			Assert.fail();
		} catch (UserAccountException e) {
			Assert.assertEquals("Unknown User", e.getMessage());
		}
		BDDMockito.verify(userRepo).findById(BDDMockito.anyLong());
	}

	@Test
	public void testFindByIdSuccess() throws Exception {
		BDDMockito.when(userRepo.findById(BDDMockito.anyLong()))
				.thenReturn(Optional.of(new UserEntity()));
		authService.findById(123L);
		BDDMockito.verify(userRepo).findById(BDDMockito.anyLong());
	}

	@Test
	public void testFindAllUsers() {
		authService.findAllUsers();
		BDDMockito.verify(userRepo).findAll();
	}

	@Test
	public void testFindByUsername() {
		authService.findByUsername("");
		BDDMockito.verify(userRepo).findByUsername(BDDMockito.anyString());
	}

	@Test
	public void testRegisterNewUser() throws Exception {
		String pass = "abcdefghijklmnopqrstuvwxyz";
		RoleEntity role = new RoleEntity();
		BDDMockito.when(roleRepo.findByDefaultRoleTrue()).thenReturn(role);
		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		authService.registerNewUser("user", pass);
		BDDMockito.verify(userRepo).saveAndFlush(userCaptor.capture());
		Assert.assertTrue(passwordEncoder.matches(pass, userCaptor.getValue().getPassword()));
		Assert.assertTrue(userCaptor.getValue().getRoles().contains(role));
	}

	@Test
	public void testRegisterNewUserAlreadyExists() {
		String pass = "foobar";
		BDDMockito.when(userRepo.findByUsername("user")).thenReturn(new UserEntity());
		try {
			authService.registerNewUser("user", pass);
			Assert.fail();
		} catch (UserAccountException e) {
			Assert.assertEquals("User already exists", e.getMessage());
		}
		BDDMockito.verify(userRepo, Mockito.times(0)).save(BDDMockito.any(UserEntity.class));
	}

	@Test
	public void testRegisterNewUserPasswordTooShort() {
		String pass = "foobar";
		RoleEntity role = new RoleEntity();
		BDDMockito.when(roleRepo.findByDefaultRoleTrue()).thenReturn(role);
		try {
			authService.registerNewUser("user", pass);
			Assert.fail();
		} catch (UserAccountException e) {
			Assert.assertEquals("Password must be 20 characters or more", e.getMessage());
		}
		BDDMockito.verify(userRepo, Mockito.times(0)).save(BDDMockito.any(UserEntity.class));
	}

	@Test
	public void testRegisterNewUserBadPassword() {
		String pass = "PASSWORDPASSWORDPASSWORD";
		RoleEntity role = new RoleEntity();
		BDDMockito.when(roleRepo.findByDefaultRoleTrue()).thenReturn(role);
		try {
			authService.registerNewUser("user", pass);
			Assert.fail();
		} catch (UserAccountException e) {
			Assert.assertEquals(
					"Don't put the word password in your password... I mean... yikes...",
					e.getMessage());
		}
		BDDMockito.verify(userRepo, Mockito.times(0)).save(BDDMockito.any(UserEntity.class));
	}


	@Test
	public void testSaveUser() {
		UserEntity user = new UserEntity();
		authService.saveUser(user);
		BDDMockito.verify(userRepo).saveAndFlush(user);
	}

	@Test
	public void testDeleteUser() {
		authService.deleteUser(new UserEntity());
		BDDMockito.verify(userRepo).delete(BDDMockito.any(UserEntity.class));
	}

	@Test
	public void testLoadUserByUsernameSuccess() {
		String username = "user";
		String password = "myPass";
		String roleName1 = "role1";
		String roleName2 = "role2";

		RoleEntity role1 = new RoleEntity().setRoleName(roleName1);
		RoleEntity role2 = new RoleEntity().setRoleName(roleName2);

		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPassword(password);
		user.setRoles(new HashSet<>(Arrays.asList(role1, role2)));

		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);

		UserDetails springUser = authService.loadUserByUsername(username);

		Assert.assertEquals(username, springUser.getUsername());
		Assert.assertEquals(password, springUser.getPassword());
		MatcherAssert.assertThat(
				springUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.collect(Collectors.toList()),
				Matchers.contains(roleName1, roleName2));
	}

	@Test
	public void testLoadUserByUsernameNoUser() {
		Assert.assertThrows(UsernameNotFoundException.class,
				() -> {
					BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString()))
							.thenReturn(null);
					authService.loadUserByUsername("");
				});
	}

	@Test
	public void testChangePasswordSuccess() throws Exception {
		UserEntity user = new UserEntity();
		String oldpass = "foo";
		String newpass = "abcdefghijklmnopqrstuvwxyz";
		user.setPassword(passwordEncoder.encode(oldpass));
		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);

		authService.changePassword("user", oldpass, newpass);

		Assert.assertTrue(passwordEncoder.matches(newpass, user.getPassword()));
	}

	@Test
	public void testChangePasswordDoesNotExist() throws Exception {
		UserEntity user = new UserEntity();
		String oldpass = "foo";
		String newpass = "bar";
		user.setSsoId("ssoid");
		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(null);
		try {
			authService.changePassword("user", oldpass, newpass);
			Assert.fail("Should throw exception");
		} catch (UserAccountException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("User does not exist"));
		}
	}

	@Test
	public void testChangePasswordFailSSO() throws Exception {
		UserEntity user = new UserEntity();
		String oldpass = "foo";
		String newpass = "bar";
		user.setSsoId("ssoid");
		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);
		try {
			authService.changePassword("user", oldpass, newpass);
			Assert.fail("Should throw exception");
		} catch (UserAccountException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("authenticate using SSO"));
		}
	}

	@Test
	public void testChangePasswordFailBadPw() throws Exception {
		UserEntity user = new UserEntity();
		String oldpass = "foo";
		String newpass = "bar";
		user.setPassword(passwordEncoder.encode(oldpass));
		BDDMockito.when(userRepo.findByUsername(BDDMockito.anyString())).thenReturn(user);
		try {
			authService.changePassword("user", "wrong", newpass);
			Assert.fail("Should throw exception");
		} catch (BadCredentialsException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("password is invalid"));
		}
	}

	@Test
	public void testSetUserRole() throws Exception {
		UserEntity user = new UserEntity();
		BDDMockito.when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		RoleEntity role = new RoleEntity();
		BDDMockito.when(roleRepo.findById(2L)).thenReturn(Optional.of(role));
		authService.setUserRoles(1L, Collections.singletonList(2L));
		BDDMockito.verify(userRepo).saveAndFlush(user);
		Assert.assertTrue(user.getRoles().contains(role));
	}

	@Test
	public void testSetUserRoleUnknownUser() {
		UserEntity user = new UserEntity();
		BDDMockito.when(userRepo.findById(1L)).thenReturn(Optional.empty());
		try {
			authService.setUserRoles(1L, Collections.singletonList(2L));
			Assert.fail();
		} catch (UserAccountException e) {
			Assert.assertEquals("Unknown user id", e.getMessage());
		}
		BDDMockito.verify(userRepo, Mockito.times(0)).saveAndFlush(user);
		Assert.assertTrue(user.getRoles().isEmpty());
	}

	@Test
	public void testSetUserRoleUnknownRole() {
		UserEntity user = new UserEntity();
		BDDMockito.when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		BDDMockito.when(roleRepo.findById(2L)).thenReturn(Optional.empty());
		try {
			authService.setUserRoles(1L, Collections.singletonList(2L));
		} catch (UserAccountException e) {
			Assert.assertEquals("Unknown role id", e.getMessage());
		}
		BDDMockito.verify(userRepo, Mockito.times(0)).saveAndFlush(user);
		Assert.assertTrue(user.getRoles().isEmpty());
	}

	@Test
	public void testFindDefaultRole() {
		authService.findDefaultRole();
		BDDMockito.verify(roleRepo).findByDefaultRoleTrue();
	}

	@Test
	public void testFindAllRoles() {
		authService.findAllRoles();
		BDDMockito.verify(roleRepo).findAll();
	}

	@Test
	public void testSetupDefaultAuthExistingAdmin() {
		UserEntity user = new UserEntity();
		user.setUsername("foo");
		RoleEntity role = new RoleEntity();
		role.setRoleName(CoreRole.ADMIN_ROLE);
		user.setRoles(Collections.singleton(role));
		BDDMockito.when(userRepo.findAll()).thenReturn(Collections.singletonList(user));
		authService.setupDefaultAuth();
		BDDMockito.verify(userRepo, Mockito.times(0))
				.saveAndFlush(BDDMockito.any(UserEntity.class));
	}

	@Test
	public void testChangePasswordOverrideUnknownUser() {
		BDDMockito.when(userRepo.findById(BDDMockito.anyLong())).thenReturn(Optional.empty());
		try {
			authService.changePasswordOverride(1L, "foo");
			Assert.fail("Should throw exception");
		} catch (UserAccountException e) {
			MatcherAssert.assertThat(e.getMessage(),
					Matchers.containsString("Unknown User"));
		}
	}

	@Test
	public void testChangePasswordOverrideSuccess() throws Exception {
		UserEntity user = new UserEntity();
		String newpass = "abcdefghijklmnopqrstuvwxyz";
		BDDMockito.when(userRepo.findById(BDDMockito.anyLong())).thenReturn(Optional.of(user));

		authService.changePasswordOverride(1L, newpass);

		Assert.assertTrue(passwordEncoder.matches(newpass, user.getPassword()));
		BDDMockito.verify(userRepo).saveAndFlush(user);
	}

	@Test
	public void testOnLogin() {
		Authentication auth = new Authentication() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return null;
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean b) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "foo";
			}
		};
		AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);
		BDDMockito.when(userRepo.findByUsername("foo")).thenReturn(new UserEntity());
		authService.onLogin(event);
		BDDMockito.verify(userRepo).saveAndFlush(BDDMockito.any(UserEntity.class));
	}
}
