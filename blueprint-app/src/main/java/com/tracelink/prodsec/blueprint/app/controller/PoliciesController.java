package com.tracelink.prodsec.blueprint.app.controller;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.policy.PolicyEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;

/**
 * Controller to get policies and base statements from the server, as well as to delete policies.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/policies")
public class PoliciesController {

	private final PolicyService policyService;
	private final BaseStatementService baseStatementService;

	public PoliciesController(@Autowired PolicyService policyService,
			@Autowired BaseStatementService baseStatementService) {
		this.policyService = policyService;
		this.baseStatementService = baseStatementService;
	}

	@GetMapping(value = {"", "/{policyId}"})
	public ModelAndView getPolicies(@PathVariable Optional<Long> policyId, Principal principal) {
		BlueprintModelAndView mav = new BlueprintModelAndView("policies");
		mav.addScriptReference("/scripts/policies.js");
		Map<String, Long> policies = policyService.getPolicies();
		mav.addObject("policies", policies);

		PolicyEntity policy = null;
		if (policyId.isPresent()) {
			// Get policy for the given id
			policy = policyService.getPolicy(policyId.get());
			if (policy == null) {
				mav.addErrorMessage("Invalid policy id");
			}
		}

		if (policy == null && !policies.isEmpty()) {
			// Get first policy in map
			policy = policyService.getPolicy(policies.entrySet().iterator().next().getValue());
		}

		if (policy != null) {
			mav.addObject("policy", policy.toDto());
			mav.addObject("author", policy.getAuthor());
			mav.addObject("policyId", policy.getId());
			mav.addObject("delete", policy.getAuthor().equals(principal.getName()));
			mav.addObject("baseStatements", baseStatementService
					.getBaseStatementsForPolicyType(policy.getPolicyType().getName()));
		}
		return mav;
	}

	@PostMapping("/delete/{policyId}")
	public RedirectView deletePolicy(@PathVariable Long policyId, Principal principal,
			RedirectAttributes redirectAttributes) {
		try {
			PolicyEntity policy = policyService.deletePolicy(policyId, principal.getName());
			redirectAttributes
					.addFlashAttribute("success", "Deleted policy \"" + policy.getName() + "\"");
		} catch (PolicyException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot delete policy. " + e.getMessage());
		}
		return new RedirectView("/policies");
	}

}
