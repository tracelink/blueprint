package com.tracelink.prodsec.blueprint.app.auth.controller;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	@MockBean
	private PasswordEncoder mockPasswordEncoder;

	private UserEntity mockUser;

	@Before
	public void setup() {
		String email = "foo@bar.com";
		String rolesString = "Role1, Role2";

		mockUser = BDDMockito.mock(UserEntity.class);
		BDDMockito.when(mockUser.getUsername()).thenReturn(email);
		BDDMockito.when(mockUser.getRolesString()).thenReturn(rolesString);
	}

	@Test
	@WithMockUser
	public void testProfile() throws Exception {
		String email = "foo@bar.com";
		String rolesString = "Role1, Role2";

		UserEntity mockUser = BDDMockito.mock(UserEntity.class);
		BDDMockito.when(mockUser.getUsername()).thenReturn(email);
		BDDMockito.when(mockUser.getRolesString()).thenReturn(rolesString);

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(mockUser);

		mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
				.andExpect(MockMvcResultMatchers.model().attribute("user", mockUser));
	}

	@Test
	@WithMockUser
	public void testChangePasswordFail() throws Exception {
		String currentPassword = "pass";
		String newPassword = "newpass";

		UserEntity user = BDDMockito.mock(UserEntity.class);
		BDDMockito.when(user.getSsoId()).thenReturn("abcdef1234567890");

		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString()))
				.thenReturn(user);
		BDDMockito.doThrow(new BadCredentialsException("Invalid password")).when(mockAuthService)
				.changePassword(
						BDDMockito.anyString(), BDDMockito.anyString(), BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile/change-password")
						.param("currentPassword", currentPassword)
						.param("newPassword", newPassword)
						.param("confirmPassword", newPassword)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.is("Invalid password")));
	}

	@Test
	@WithMockUser
	public void testChangeProfilePasswordMismatch() throws Exception {
		String current = "pw";
		String newPw = "newpw";

		UserEntity user = BDDMockito.mock(UserEntity.class);
		BDDMockito.when(mockAuthService.findByUsername(BDDMockito.anyString())).thenReturn(user);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile/change-password")
						.param("currentPassword", current)
						.param("newPassword", newPw)
						.param("confirmPassword", "INCORRECT")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
								Matchers.is("Your provided passwords don't match")));
	}


	@Test
	@WithMockUser
	public void testChangeProfileSuccess() throws Exception {
		String current = "pw";
		String newPw = "newpw";

		mockMvc.perform(
				MockMvcRequestBuilders.post("/profile/change-password")
						.param("currentPassword", current)
						.param("newPassword", newPw)
						.param("confirmPassword", newPw)
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
								Matchers.is("Your password has been updated successfully.")));
	}
}
