package com.tracelink.prodsec.blueprint.app.auth.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tracelink.prodsec.blueprint.app.auth.UserAccountException;
import com.tracelink.prodsec.blueprint.app.auth.model.UserRegistrationForm;
import com.tracelink.prodsec.blueprint.app.auth.service.AuthService;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;

/**
 * Controller for the registration flow.
 *
 * @author csmith
 */
@Controller
public class RegistrationController {

	private final AuthService authService;

	public RegistrationController(@Autowired AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/register")
	public String registerForm(UserRegistrationForm form) {
		return "register";
	}

	@PostMapping("/register")
	public ModelAndView registerUser(@Valid @ModelAttribute UserRegistrationForm form,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		ModelAndView modelAndView = new ModelAndView();

		if (!form.getPassword().equals(form.getPasswordConfirmation())) {
			bindingResult
					.rejectValue("passwordConfirmation", "error.user", "Passwords don't match");
		}

		try {
			authService.registerNewUser(form.getUsername(), form.getPassword());
		} catch (UserAccountException e) {
			bindingResult.rejectValue("password", "error.user", e.getMessage());
		}

		if (bindingResult.hasErrors()) {
			modelAndView.setViewName("register");
			return modelAndView;
		}

		redirectAttributes.addFlashAttribute(BlueprintModelAndView.SUCCESS_NOTIFICATION,
				"User account created successfully. Please sign in.");

		modelAndView.setViewName("redirect:/login");

		return modelAndView;
	}
}
