package com.tracelink.prodsec.blueprint.app.auth.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.UserEntity;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;

/**
 * Controller to display user information in the UI and to allow users to change their passwords.
 *
 * @author csmith, mcool
 */
@Controller
@RequestMapping(value = "/profile")
public class ProfileController {

	private final AuthService authService;

	public ProfileController(@Autowired AuthService authService) {
		this.authService = authService;
	}

	@GetMapping
	public BlueprintModelAndView profile(Principal principal) {
		BlueprintModelAndView mav = new BlueprintModelAndView("user");
		UserEntity user = authService.findByUsername(principal.getName());
		mav.addObject("user", authService.findByUsername(user.getUsername()));
		mav.addObject("localUser", user.getSsoId() == null);
		return mav;
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String currentPassword,
			@RequestParam String newPassword, @RequestParam String confirmPassword,
			RedirectAttributes redirectAttributes, Principal authenticatedUser) {
		if (!newPassword.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
					"Your provided passwords don't match");
			return "redirect:/profile";
		}

		try {
			authService.changePassword(authenticatedUser.getName(), currentPassword, newPassword);
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
					"Your password has been updated successfully.");
		} catch (UserAccountException | AuthenticationException e) {
			redirectAttributes.addFlashAttribute(BlueprintModelAndView.FAILURE_NOTIFICATION,
					e.getMessage());
		}
		return "redirect:/profile";
	}
}
