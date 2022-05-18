package com.tracelink.prodsec.blueprint.app.auth.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;

/**
 * Controller to handle administrative user management including updating user roles, setting user
 * passwords and deleting users.
 *
 * @author csmith
 */
@Controller
@RequestMapping("/usermgmt")
@PreAuthorize("hasAuthority('" + CoreRole.ADMIN_ROLE + "')")
public class UserManagementController {

	private final AuthService authService;

	public UserManagementController(@Autowired AuthService authService) {
		this.authService = authService;
	}

	@GetMapping()
	public BlueprintModelAndView getUserMgmt() {
		BlueprintModelAndView mav = new BlueprintModelAndView("usermgmt");
		mav.addObject("users", authService.findAllUsers());
		return mav;
	}

	@GetMapping("/{userid}")
	public BlueprintModelAndView getUserView(@PathVariable Long userid) {
		BlueprintModelAndView mav = new BlueprintModelAndView("user");
		try {
			mav.addObject("user", authService.findById(userid));
			mav.addObject("roles", authService.findAllRoles());
			mav.addObject("showActions", true);
			mav.addScriptReference("/scripts/user.js");
		} catch (UserAccountException e) {
			mav.setViewName("redirect:/usermgmt");
			mav.addErrorMessage(e.getMessage());
		}
		return mav;
	}

	@PostMapping("/{userid}/changepw")
	public String changeUserPassword(@PathVariable Long userid, @RequestParam String newPassword,
			RedirectAttributes redirectAttributes) {
		try {
			authService.changePasswordOverride(userid, newPassword);
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
					"Successfully set the user's password");
		} catch (UserAccountException e) {
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
					e.getMessage());
		}
		return "redirect:/usermgmt";
	}

	@PostMapping("/{userid}/setrole")
	public String setRole(@PathVariable Long userid, @RequestParam List<Long> roleIds,
			RedirectAttributes redirectAttributes, Principal principal) {
		try {
			UserEntity user = authService.findById(userid);
			if (user.getUsername().equals(principal.getName())) {
				redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
						"Cannot edit own information");
			} else {
				authService.setUserRoles(userid, roleIds);
				redirectAttributes.addFlashAttribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
						"Successfully set role");
			}
		} catch (UserAccountException e) {
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
					e.getMessage());
		}
		return "redirect:/usermgmt";
	}

	@PostMapping("/{userid}/delete")
	public String deleteUser(@PathVariable Long userid, RedirectAttributes redirectAttributes,
			Principal principal) {
		try {
			UserEntity user = authService.findById(userid);
			if (user.getUsername().equals(principal.getName())) {
				redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
						"Can't delete yourself");
			} else {
				authService.deleteUser(user);
				redirectAttributes.addFlashAttribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
						"Deleted " + user.getUsername());
			}
		} catch (UserAccountException e) {
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
					e.getMessage());
		}
		return "redirect:/usermgmt";
	}
}
