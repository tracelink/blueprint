package com.tracelink.prodsec.blueprint.app.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracelink.prodsec.blueprint.app.exception.PolicyException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.policy.PolicyDto;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;

/**
 * Controller to export policies to Rego and display the result.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/export")
public class ExportController {

	private static final String BINDING_RESULT_POLICY = "org.springframework.validation.BindingResult.policy";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private final PolicyService policyService;

	public ExportController(@Autowired PolicyService policyService) {
		this.policyService = policyService;
	}

	@GetMapping("")
	public ModelAndView getExport() {
		BlueprintModelAndView mav = new BlueprintModelAndView("export");
		mav.addScriptReference("/scripts/export.js");
		return mav;
	}

	@PostMapping("")
	public RedirectView exportPolicy(@Valid PolicyDto policyDto, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("failure", "The policy has validation errors");
			redirectAttributes.addFlashAttribute(BINDING_RESULT_POLICY, result);
			redirectAttributes.addFlashAttribute("policy", policyDto);
			return new RedirectView("?type=" + policyDto.getPolicyType());
		}

		RedirectView redirectView;
		try {
			String rego = policyService.exportPolicy(policyDto);
			redirectAttributes.addFlashAttribute("rego", rego);
			redirectAttributes.addFlashAttribute("policy",
					OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(policyDto));
			redirectView = new RedirectView("export");
		} catch (IllegalArgumentException | PolicyException | JsonProcessingException e) {
			redirectAttributes.addFlashAttribute("failure", e.getMessage());
			redirectAttributes.addFlashAttribute("policy", policyDto);
			redirectView = new RedirectView("?type=" + policyDto.getPolicyType());
		}
		return redirectView;
	}

}
