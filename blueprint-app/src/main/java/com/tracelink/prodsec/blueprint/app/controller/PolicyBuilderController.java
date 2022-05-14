package com.tracelink.prodsec.blueprint.app.controller;

import java.security.Principal;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyImportException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;

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

	public PolicyBuilderController(@Autowired PolicyService policyService,
			@Autowired BaseStatementService baseStatementService) {
		this.policyService = policyService;
		this.baseStatementService = baseStatementService;
	}

	@GetMapping("")
	public ModelAndView getPolicyBuilder(@RequestParam Optional<String> name,
			@RequestParam Optional<String> type, Model model) {
		BlueprintModelAndView mav = new BlueprintModelAndView("builder");
		mav.addScriptReference("/scripts/builder.js");
		model.addAttribute("policyTypes", baseStatementService.getPolicyTypes());
		model.addAttribute("policies", policyService.getPolicies().keySet());
		if (name.isPresent()) {
			// Try to load policy if a name is provided
			PolicyEntity policy = policyService.getPolicy(name.get());
			if (policy != null) {
				model.addAttribute("policyType", policy.getPolicyType().getName());
				model.addAttribute("baseStatements", baseStatementService
						.getBaseStatementsForPolicyType(policy.getPolicyType().getName()));
				model.addAttribute("policy", policy.toDto());
			} else {
				model.addAttribute("failure", "No policy with name \"" + name.get() + "\"");
			}
		} else if (type.isPresent()) {
			// Otherwise try to load base statements if a type is provided
			PolicyTypeEntity policyType = baseStatementService.getPolicyType(type.get());
			if (policyType != null) {
				model.addAttribute("policyType", policyType.getName());
				model.addAttribute("baseStatements", baseStatementService
						.getBaseStatementsForPolicyType(policyType.getName()));
				if (!model.containsAttribute("policy")) {
					model.addAttribute("policy", new PolicyDto());
				}
			} else {
				model.addAttribute("failure", "No policy type with name \"" + type.get() + "\"");
			}
		}
		return mav;
	}

	@PostMapping("/import")
	public RedirectView importPolicy(@RequestParam String json,
			RedirectAttributes redirectAttributes) {
		RedirectView redirectView;
		try {
			PolicyDto policy = policyService.importPolicy(json);
			redirectAttributes.addFlashAttribute("policy", policy);
			redirectView = new RedirectView("?type=" + policy.getPolicyType());
		} catch (PolicyImportException e) {
			redirectAttributes.addFlashAttribute("failure",
					"The imported policy is invalid: " + e.getMessage());
			redirectView = new RedirectView("");
		}
		return redirectView;
	}

	@PostMapping("/save")
	public RedirectView savePolicy(@RequestParam String name, @Valid PolicyDto policyDto,
			BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("failure", "The policy has validation errors");
			redirectAttributes.addFlashAttribute(BINDING_RESULT_POLICY, result);
			redirectAttributes.addFlashAttribute("policy", policyDto);
			return new RedirectView("?type=" + policyDto.getPolicyType());
		}

		RedirectView redirectView;
		try {
			policyService.savePolicy(policyDto, name, principal.getName());
			redirectAttributes.addFlashAttribute("success", "Saved policy \"" + name + "\"");
			redirectView = new RedirectView("?name=" + name);
		} catch (PolicyException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot save policy. " + e.getMessage());
			redirectAttributes.addFlashAttribute("policy", policyDto);
			redirectView = new RedirectView("?type=" + policyDto.getPolicyType());
		}
		return redirectView;
	}
}
