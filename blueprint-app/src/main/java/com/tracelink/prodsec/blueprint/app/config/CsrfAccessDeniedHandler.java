package com.tracelink.prodsec.blueprint.app.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

/**
 * Class to handle denying user access if a CSRF token is not provided on appropriate requests.
 *
 * @author csmith
 */
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		if (accessDeniedException instanceof CsrfException) {
			response.getWriter().write(accessDeniedException.getMessage());
		}

	}

}
