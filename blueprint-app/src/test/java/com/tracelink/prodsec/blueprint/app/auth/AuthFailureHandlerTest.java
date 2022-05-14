package com.tracelink.prodsec.blueprint.app.auth;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.RedirectStrategy;

import com.tracelink.prodsec.blueprint.app.config.AuthFailureHandler;

public class AuthFailureHandlerTest {

	private AuthFailureHandler authFailureHandler;
	private RedirectStrategy redirectStrategy;

	@Before
	public void setup() {
		authFailureHandler = new AuthFailureHandler();
		redirectStrategy = BDDMockito.mock(RedirectStrategy.class);
		authFailureHandler.setRedirectStrategy(redirectStrategy);
	}

	@Test
	public void testOnAuthenticationFailureDisabled() throws Exception {
		authFailureHandler.onAuthenticationFailure(null, null, new DisabledException("Error"));
		BDDMockito.verify(redirectStrategy)
				.sendRedirect(null, null, "/login?error=Account Disabled");
	}

	@Test
	public void testOnAuthenticationFailureOAuth2() throws Exception {
		authFailureHandler
				.onAuthenticationFailure(null, null,
						new OAuth2AuthenticationException(new OAuth2Error("foo"), "Error"));
		BDDMockito.verify(redirectStrategy)
				.sendRedirect(null, null, "/login?error=Error");
	}

	@Test
	public void testOnAuthenticationFailureGeneric() throws Exception {
		authFailureHandler
				.onAuthenticationFailure(null, null, new UsernameNotFoundException("Error"));
		BDDMockito.verify(redirectStrategy)
				.sendRedirect(null, null, "/login?error=Invalid Login");
	}

}
