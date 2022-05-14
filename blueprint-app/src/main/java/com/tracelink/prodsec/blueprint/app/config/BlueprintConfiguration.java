package com.tracelink.prodsec.blueprint.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring configuration class to set up a password encoder for hashing and storing user credentials
 * in the database.
 *
 * @author csmith
 */
@Configuration
public class BlueprintConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
}
