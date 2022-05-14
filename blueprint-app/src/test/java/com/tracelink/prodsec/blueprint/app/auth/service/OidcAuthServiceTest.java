package com.tracelink.prodsec.blueprint.app.auth.service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.blueprint.app.auth.model.OidcUserDetails;
import com.tracelink.prodsec.blueprint.app.auth.model.RoleEntity;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;


@RunWith(SpringRunner.class)
public class OidcAuthServiceTest {

	@MockBean
	private AuthService authService;

	@Mock
	OidcUserRequest oidcUserRequest;

	private OidcAuthService oidcAuthService;
	private final String sub = UUID.randomUUID().toString();
	private final String email = "jdoe@example.com";
	private OidcIdToken idToken;
	private Map<String, Object> claims;

	@Before
	public void setup() {
		oidcAuthService = new OidcAuthService(authService);
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("oidc")
				.clientId("ssoServer")
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.authorizationUri("https://example.com/auth")
				.tokenUri("https://example.com/token")
				.userInfoUri("https://example.com/userinfo")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).build();

		OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, "1234567890ABCDEF",
				Instant.now().minusSeconds(10), Instant.now().plusSeconds(10));

		claims = new HashMap<>();
		claims.put("sub", sub);
		claims.put("email", email);
		idToken = new OidcIdToken("1234567890ABCDEF", Instant.now().minusSeconds(10),
				Instant.now().plusSeconds(10), claims);

		BDDMockito.when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);
		BDDMockito.when(oidcUserRequest.getAccessToken()).thenReturn(accessToken);
		BDDMockito.when(oidcUserRequest.getIdToken()).thenReturn(idToken);
	}

	@Test
	public void testLoadUser() {
		RoleEntity role = new RoleEntity();
		String roleName = "foobar";
		role.setRoleName(roleName);
		BDDMockito.when(authService.findDefaultRole()).thenReturn(role);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		BDDMockito.verify(authService, Mockito.times(1)).saveUser(userCaptor.capture());

		Assert.assertEquals(sub, userCaptor.getValue().getSsoId());
		Assert.assertTrue(StringUtils.isNotBlank(userCaptor.getValue().getSsoId()));
		Assert.assertEquals(email, userCaptor.getValue().getUsername());
		Assert.assertEquals(1, userCaptor.getValue().getRoles().size());
		Assert.assertTrue(userCaptor.getValue().getRoles().contains(role));
		Assert.assertEquals(1, userCaptor.getValue().getEnabled());
		Assert.assertNull(userCaptor.getValue().getPassword());

		Assert.assertTrue(oidcUser instanceof OidcUserDetails);
		Assert.assertEquals(email, oidcUser.getName());
		Assert.assertEquals(1, oidcUser.getAuthorities().size());
		Assert.assertEquals(roleName,
				oidcUser.getAuthorities().iterator().next().getAuthority());
		Assert.assertEquals(claims, oidcUser.getClaims());
		Assert.assertEquals(idToken, oidcUser.getIdToken());
		Assert.assertNull(oidcUser.getUserInfo());
		Assert.assertEquals(claims, oidcUser.getAttributes());
	}

	@Test
	public void testLoadUserExistingLocalUser() {
		UserEntity user = new UserEntity();
		user.setUsername(email);
		user.setSsoId(sub);
		user.setEnabled(1);
		BDDMockito.when(authService.findByUsername(email)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(authService, Mockito.times(0)).saveUser(user);

		Assert.assertTrue(oidcUser instanceof OidcUserDetails);
		Assert.assertEquals(email, oidcUser.getName());
		Assert.assertTrue(oidcUser.getAuthorities().isEmpty());
	}

	@Test
	public void testLoadUserMissingEmail() {
		claims.remove("email");
		claims.put("username", "jdoe");
		idToken = new OidcIdToken("1234567890ABCDEF", Instant.now().minusSeconds(10),
				Instant.now().plusSeconds(10), claims);
		BDDMockito.when(oidcUserRequest.getIdToken()).thenReturn(idToken);

		try {
			oidcAuthService.loadUser(oidcUserRequest);
			Assert.fail("Exception should have been thrown");
		} catch (OAuth2AuthenticationException e) {
			Assert.assertTrue(
					e.getMessage().contains("User info must contain an email attribute to login."));
		}
	}

	@Test
	public void testLoadUserExistingLocalUserCollision() {
		UserEntity user = new UserEntity();
		user.setUsername(email);
		BDDMockito.when(authService.findByUsername(email)).thenReturn(user);
		try {
			oidcAuthService.loadUser(oidcUserRequest);
			Assert.fail("Exception should have been thrown");
		} catch (OAuth2AuthenticationException e) {
			Assert.assertTrue(e.getMessage()
					.contains("A local user with the username \"" + email + "\" already exists."));
		}

		BDDMockito.verify(authService, Mockito.times(0)).saveUser(user);
	}

	@Test
	public void testLoadUserExistingSsoUser() {
		UserEntity user = new UserEntity();
		user.setUsername("oldemail@example.com");
		user.setSsoId(sub);
		user.setEnabled(1);
		BDDMockito.when(authService.findByUsername(email)).thenReturn(null);
		BDDMockito.when(authService.findBySsoId(sub)).thenReturn(user);
		oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(authService, Mockito.times(1)).saveUser(user);
		Assert.assertEquals(email, user.getUsername());
	}

	@Test
	public void testLoadUserExistingDisabledSsoUser() {
		UserEntity user = new UserEntity();
		user.setUsername("oldemail@example.com");
		user.setSsoId(sub);
		user.setEnabled(0);
		BDDMockito.when(authService.findByUsername(email)).thenReturn(null);
		BDDMockito.when(authService.findBySsoId(sub)).thenReturn(user);

		try {
			oidcAuthService.loadUser(oidcUserRequest);
			Assert.fail("Exception should have been thrown");
		} catch (DisabledException e) {
			Assert.assertEquals("Account disabled", e.getMessage());
		}

		BDDMockito.verify(authService, Mockito.times(1)).saveUser(user);
		Assert.assertEquals(email, user.getUsername());
	}

	@Test
	public void testLoadUserExistingSsoUserWithRoles() {
		UserEntity user = new UserEntity();
		user.setUsername("oldemail@example.com");
		user.setSsoId(sub);
		user.setEnabled(1);

		RoleEntity role = new RoleEntity();
		role.setRoleName("SpecialRole");
		user.setRoles(Collections.singleton(role));

		BDDMockito.when(authService.findByUsername(email)).thenReturn(null);
		BDDMockito.when(authService.findBySsoId(sub)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(authService, Mockito.times(1)).saveUser(user);
		Assert.assertEquals(email, user.getUsername());

		Assert.assertEquals(1, oidcUser.getAuthorities().size());
		Assert.assertEquals(role.getRoleName(),
				oidcUser.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	public void testLoadUserExistingSsoUserNewSsoId() {
		UserEntity user = new UserEntity();
		user.setUsername("email@example.com");
		user.setSsoId("1234567890");
		user.setEnabled(1);

		BDDMockito.when(authService.findByUsername(email)).thenReturn(null);
		BDDMockito.when(authService.findBySsoId(sub)).thenReturn(user);
		OidcUser oidcUser = oidcAuthService.loadUser(oidcUserRequest);

		BDDMockito.verify(authService, Mockito.times(2)).saveUser(user);
		Assert.assertEquals(sub, user.getSsoId());
	}
}
