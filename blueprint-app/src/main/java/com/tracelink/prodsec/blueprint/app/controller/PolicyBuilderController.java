package com.tracelink.prodsec.blueprint.app.controller;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;

/**
 * Controller to get base statements and policy types from the server, as well as to handle policy
 * import and save.
 *
 * @author mcool
 */
@Controller
@RequestMapping("")
public class PolicyBuilderController {

	private static final String BINDING_RESULT_POLICY = "org.springframework.validation.BindingResult.policy";
	private final PolicyService policyService;
	private final BaseStatementService baseStatementService;
	private final PolicyTypeService policyTypeService;

	public PolicyBuilderController(@Autowired PolicyService policyService,
			@Autowired BaseStatementService baseStatementService,
			@Autowired PolicyTypeService policyTypeService) {
		this.policyService = policyService;
		this.baseStatementService = baseStatementService;
		this.policyTypeService = policyTypeService;
	}

	@GetMapping("")
	public ModelAndView getPolicyBuilder(@RequestParam Optional<String> name,
			@RequestParam Optional<String> type, Model model)
			throws PolicyElementNotFoundException {
		BlueprintModelAndView mav = new BlueprintModelAndView("builder");
		mav.addScriptReference("/scripts/builder.js");
		model.addAttribute("policyTypes", policyTypeService.getPolicyTypes());
		model.addAttribute("allBaseStatements", baseStatementService.getAllBaseStatements());
		model.addAttribute("policies", policyService.getPolicies().keySet());
		if (name.isPresent()) {
			// Load policy if a name is provided
			PolicyEntity policy = policyService.getPolicy(name.get());
			model.addAttribute("policyType", policy.getPolicyType().getName());
			model.addAttribute("baseStatements", baseStatementService
					.getLatestBaseStatementsForPolicyType(policy.getPolicyType()));
			model.addAttribute("policy", policy.toDto());
			model.addAttribute("updatedBaseStatements",
					policyService.getUpdatedBaseStatements(policy));
		} else if (type.isPresent()) {
			// Otherwise load base statements if a type is provided
			PolicyTypeEntity policyType = policyTypeService.getPolicyType(type.get());
			model.addAttribute("policyType", policyType.getName());
			model.addAttribute("baseStatements", baseStatementService
					.getLatestBaseStatementsForPolicyType(policyType));
			if (!model.containsAttribute("policy")) {
				model.addAttribute("policy", new PolicyDto());
			}
		}
		return mav;
	}

	@PostMapping("/import")
	public RedirectView importPolicy(@RequestParam String json,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			PolicyDto policy = policyService.importPolicy(json);
			redirectAttributes.addFlashAttribute("policy", policy);
			redirectView = new RedirectView("?type=" + policy.getPolicyType());
		} catch (PolicyException e) {
			redirectAttributes.addFlashAttribute("failure",
					"The imported policy is invalid: " + e.getMessage());
			redirectView = new RedirectView("");
		}
		return redirectView;
	}

	@PostMapping("/save")
	public RedirectView savePolicy(@Valid PolicyDto policyDto,
			BindingResult result, Principal principal, RedirectAttributes redirectAttributes)
			throws PolicyElementNotFoundException {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("failure", "The policy has validation errors");
			redirectAttributes.addFlashAttribute(BINDING_RESULT_POLICY, result);
			redirectAttributes.addFlashAttribute("policy", policyDto);
			return new RedirectView("?type=" + policyDto.getPolicyType());
		}

		RedirectView redirectView;
		try {
			policyDto.setAuthor(principal.getName());
			policyService.savePolicy(policyDto);
			redirectAttributes.addFlashAttribute("success",
					MessageFormat.format("Saved policy ''{0}''", policyDto.getName()));
			redirectView = new RedirectView("?name=" + policyDto.getName());
		} catch (PolicyException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot save policy. " + e.getMessage());
			redirectAttributes.addFlashAttribute("policy", policyDto);
			redirectView = new RedirectView("?type=" + policyDto.getPolicyType());
		}
		return redirectView;
	}

	/**
	 * Exception handler for a {@link PolicyElementNotFoundException}. Returns to the base of this
	 * controller
	 *
	 * @param e                  the exception thrown
	 * @param redirectAttributes the redirect attributes to add the exception message
	 * @return a string redirecting to the main page
	 */
	@ExceptionHandler(PolicyElementNotFoundException.class)
	public RedirectView handlePolicyElementNotFound(Exception e,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("failure", e.getMessage());
		return new RedirectView("");
	}
}
