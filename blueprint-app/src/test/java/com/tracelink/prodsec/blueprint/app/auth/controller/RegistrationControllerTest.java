package com.tracelink.prodsec.blueprint.app.auth.controller;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class RegistrationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	///////////////////
	// Get register
	///////////////////
	@Test
	public void testGetRegister() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/register"))
				.andExpect(MockMvcResultMatchers.view().name(Matchers.is("register")));
	}

	///////////////////
	// Post register
	///////////////////

	@Test
	public void testRegisterPasswordMismatch() throws Exception {
		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString())).thenReturn(null);
		String username = "user";
		String password = "pw";

		mockMvc.perform(MockMvcRequestBuilders.post("/register").param("username", username)
				.param("password", password)
				.param("passwordConfirmation", "INCORRECT")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.view().name(Matchers.is("register"))).andExpect(
				MockMvcResultMatchers.content()
						.string(Matchers.containsString("Passwords don&#39;t match")));
	}

	@Test
	public void testRegisterUserExists() throws Exception {
		BDDMockito.doThrow(new UserAccountException("User already exists")).when(mockAuthService)
				.registerNewUser(BDDMockito.anyString(), BDDMockito.anyString());
		String username = "user";
		String password = "pw";

		mockMvc.perform(MockMvcRequestBuilders.post("/register").param("username", username)
				.param("password", password)
				.param("passwordConfirmation", "INCORRECT")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.view().name(Matchers.is("register"))).andExpect(
				MockMvcResultMatchers.content()
						.string(Matchers.containsString("User already exists")));
	}

	@Test
	public void testRegisterSuccess() throws Exception {
		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString())).thenReturn(null);
		String username = "user";
		String password = "pw";

		mockMvc.perform(MockMvcRequestBuilders.post("/register").param("username", username)
				.param("password", password)
				.param("passwordConfirmation", password)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
								Matchers.containsString("User account created successfully")));
	}
}
