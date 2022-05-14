package com.tracelink.prodsec.blueprint.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.auth.service.OidcAuthService;

/**
 * Spring configuration for security features, including login and logout as well as CSRF
 * protections.
 *
 * @author csmith, mcool
 */
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final AuthService authService;
	private final OidcAuthService oidcAuthService;
	private final ClientRegistrationRepository clientRegistrationRepository;

	public SecurityConfig(@Autowired AuthService authService,
			@Autowired OidcAuthService oidcAuthService,
			@Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
		this.authService = authService;
		this.oidcAuthService = oidcAuthService;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	/**
	 * Enforce authenticated access to special endpoints. Set up login and logout
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().ignoringAntMatchers("/console/**");
		http.exceptionHandling().accessDeniedHandler(new CsrfAccessDeniedHandler());
		http.headers().httpStrictTransportSecurity().disable();
		http.headers().frameOptions().disable();
		http.authorizeRequests()
				.antMatchers("/error").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/register").permitAll()
				.antMatchers("/console/**").hasAuthority(CoreRole.ADMIN.getName())
				.anyRequest().authenticated()
				.and()
				.formLogin()
				.loginPage("/login")
				.failureHandler(getAuthFailureHandler())
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/", true)
				.and()
				.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/login?logout=true")
				.deleteCookies("JSESSIONID")
				.invalidateHttpSession(true);

		if (clientRegistrationRepository != null
				&& clientRegistrationRepository.findByRegistrationId("oidc") != null) {
			http.oauth2Login()
					.loginPage("/login")
					.failureHandler(getAuthFailureHandler())
					.defaultSuccessUrl("/", true)
					.userInfoEndpoint()
					.oidcUserService(oidcAuthService);
		}
	}

	/**
	 * Configure the correct authentication service for these paths
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(authService);
	}

	/**
	 * Allow all static urls
	 *
	 * @param web the web security configuration object
	 * @throws Exception if web is null
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/styles/**", "/icons/**", "/images/**", "/scripts/**",
				"/webjars/**");
	}

	public AuthenticationFailureHandler getAuthFailureHandler() {
		return new AuthFailureHandler();
	}
}
