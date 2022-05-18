package com.tracelink.prodsec.blueprint.app.auth.controller;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.auth.model.RoleEntity;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UserManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService mockAuthService;

	private static final String mockUserName = "user@foo.com";

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE)
	public void testUserMgmt() throws Exception {
		List<UserEntity> users = new ArrayList<>();
		BDDMockito.when(mockAuthService.findAllUsers()).thenReturn(users);
		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt"))
				.andExpect(MockMvcResultMatchers.model().attribute("users", users));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = CoreRole.ADMIN_ROLE)
	public void testGetUserView() throws Exception {
		String email = "foo@bar.com";

		UserEntity user = new UserEntity();
		user.setUsername(email);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt/1"))
				.andExpect(MockMvcResultMatchers.model().attribute("user", user))
				.andExpect(MockMvcResultMatchers.model().attributeExists("roles", "showActions"));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = CoreRole.ADMIN_ROLE)
	public void testGetUserViewFailUnknown() throws Exception {
		BDDMockito.doThrow(new UserAccountException("Unknown User")).when(mockAuthService)
				.findById(BDDMockito.anyLong());

		mockMvc.perform(MockMvcRequestBuilders.get("/usermgmt/1"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
								Matchers.containsString("Unknown User")));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = CoreRole.ADMIN_ROLE)
	public void testChangePassword() throws Exception {
		UserEntity user = new UserEntity();
		user.setUsername(mockUserName);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/usermgmt/1/changepw").param("newPassword", "secret")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
										Matchers.containsString(
												"Successfully set the user's password")));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = CoreRole.ADMIN_ROLE)
	public void testChangePasswordFailException() throws Exception {
		UserEntity user = new UserEntity();
		user.setUsername(mockUserName);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);
		BDDMockito.doThrow(new UserAccountException("Unknown User")).when(mockAuthService)
				.changePasswordOverride(BDDMockito.anyLong(), BDDMockito.anyString());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/usermgmt/1/changepw").param("newPassword", "secret")
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.containsString("Unknown User")));
	}

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE)
	public void testSetRoleSuccess() throws Exception {
		String userName = "foo@bar.com";
		String roleName = "myRole";

		UserEntity user = new UserEntity();
		user.setUsername(userName);

		RoleEntity role = new RoleEntity();
		role.setRoleName(roleName);

		// add role
		String[] truths = new String[]{"on", "true", "yes", "1"};
		for (String truth : truths) {
			user.setRoles(new HashSet<>());
			testSetRole(user, role, truth)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/usermgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
									Matchers.containsString("Successfully set role")));
		}

		// remove role
		String[] falses = new String[]{"off", "0"};
		for (String falsehood : falses) {
			user.setRoles(new HashSet<>(Arrays.asList(role)));
			testSetRole(user, role, falsehood)
					.andExpect(MockMvcResultMatchers.redirectedUrl("/usermgmt"))
					.andExpect(MockMvcResultMatchers.flash()
							.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
									"Successfully set role"));
		}

	}

	private ResultActions testSetRole(UserEntity user, RoleEntity role, String paramTruth)
			throws Exception {
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		return mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/setrole")
				.param("roleIds", String.valueOf(role.getId()))
				.with(SecurityMockMvcRequestPostProcessors.csrf()));
	}

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE)
	public void testSetRoleFailNull() throws Exception {
		BDDMockito.doThrow(new UserAccountException("Unknown User")).when(mockAuthService)
				.findById(BDDMockito.anyLong());

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/setrole")
				.param("roleIds", "1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.containsString("Unknown User")));
	}

	@Test
	@WithMockUser(username = mockUserName, authorities = CoreRole.ADMIN_ROLE)
	public void testSetRoleFailCurrentUser() throws Exception {
		UserEntity user = new UserEntity();
		user.setUsername(mockUserName);

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);
		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/setrole")
				.param("roleIds", "1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.containsString("Cannot edit own information")));
	}

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE)
	public void testDeleteUserSuccess() throws Exception {
		String email = "foo@bar.com";

		UserEntity user = new UserEntity();
		user.setUsername(email);
		user.setRoles(Collections.singleton(new RoleEntity()));

		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong())).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
										Matchers.containsString("Deleted foo@bar.com")));

		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		BDDMockito.verify(mockAuthService).deleteUser(userCaptor.capture());
		Assert.assertEquals(user, userCaptor.getValue());
	}

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE)
	public void testDeleteUserFailUnknown() throws Exception {
		BDDMockito.doThrow(new UserAccountException("Unknown User")).when(mockAuthService)
				.findById(BDDMockito.anyLong());

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.containsString("Unknown User")));
	}

	@Test
	@WithMockUser(authorities = CoreRole.ADMIN_ROLE, username = mockUserName)
	public void testDeleteUserCurrentUser() throws Exception {
		UserEntity user = new UserEntity();
		user.setUsername(mockUserName);
		BDDMockito.when(mockAuthService.findById(BDDMockito.anyLong()))
				.thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/usermgmt/1/delete")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(
						MockMvcResultMatchers.flash()
								.attribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
										Matchers.containsString("Can't delete yourself")));
	}
}
