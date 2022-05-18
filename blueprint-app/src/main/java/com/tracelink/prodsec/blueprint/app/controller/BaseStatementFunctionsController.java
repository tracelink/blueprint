package com.tracelink.prodsec.blueprint.app.controller;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.exception.BaseStatementFunctionException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Controller to retrieve functions from the server, as well as to handle creation, update,
 * deletion, and state changes for functions.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/functions")
@PreAuthorize(
		"hasAnyAuthority('" + CoreRole.ADMIN_ROLE + "', '" + CoreRole.FUNCTION_EDITOR_ROLE + "')")
public class BaseStatementFunctionsController {

	private final PolicyTypeService policyTypeService;
	private final BaseStatementFunctionService functionService;

	public BaseStatementFunctionsController(@Autowired PolicyTypeService policyTypeService,
			@Autowired BaseStatementFunctionService functionService) {
		this.policyTypeService = policyTypeService;
		this.functionService = functionService;
	}

	@GetMapping(value = {"", "/{functionId}"})
	public ModelAndView getFunctions(@PathVariable Optional<Long> functionId)
			throws PolicyElementNotFoundException {
		BlueprintModelAndView mav = new BlueprintModelAndView("functions");
		mav.addScriptReference("/scripts/functions.js");
		List<String> policyTypes = policyTypeService.getPolicyTypes();
		mav.addObject("policyTypes", policyTypes);
		Map<String, Long> functions = functionService.getFunctions();
		mav.addObject("functions", functions);
		mav.addObject("editable", false);

		BaseStatementFunctionEntity function = null;
		if (functionId.isPresent()) {
			// Get function for the given id
			function = functionService.getFunction(functionId.get());
		} else if (!functions.isEmpty()) {
			// Get first function in map if id is not present
			function = functionService
					.getFunction(functions.entrySet().iterator().next().getValue());
		}
		// If we have a function, add objects to model
		if (function != null) {
			mav.addObject("function", function.toDto());
			mav.addObject("functionId", function.getId());
			mav.addObject("prevVersionId", functionService.getPreviousVersionId(
					function.getName(), function.getVersion()));
			mav.addObject("nextVersionId", functionService.getNextVersionId(
					function.getName(), function.getVersion()));
			mav.addObject("updatedDependencies",
					functionService.getUpdatedDependencies(function));
		}
		return mav;
	}

	@GetMapping(value = {"/create", "/{functionId}/edit"})
	public ModelAndView createEditFunction(@PathVariable Optional<Long> functionId, Principal
			principal, Model model, RedirectAttributes redirectAttributes)
			throws PolicyElementNotFoundException {
		// Set up basic view, scripts and objects
		BlueprintModelAndView mav = new BlueprintModelAndView("functions");
		mav.addScriptReference("/scripts/functions.js");
		List<String> policyTypes = policyTypeService.getPolicyTypes();
		mav.addObject("policyTypes", policyTypes);
		Map<String, Long> functions = functionService.getFunctions();
		mav.addObject("functions", functions);
		mav.addObject("editable", true);

		// If not redirected from another request, add function object to create/edit
		if (!model.containsAttribute("function")) {
			if (functionId.isPresent()) {
				// We are editing an existing function
				BaseStatementFunctionEntity function = functionService
						.getFunction(functionId.get());
				// Redirect to function view screen if function is not a draft
				if (!function.getState().equals(PolicyElementState.DRAFT)) {
					redirectAttributes.addFlashAttribute("failure",
							MessageFormat
									.format("Cannot edit a function that is in the {0} state",
											function.getState().getName().toLowerCase()));
					return new ModelAndView(new RedirectView("/functions/" + functionId.get()));
				}
				// Add objects to model
				mav.addObject("function", function.toDto());
				mav.addObject("functionId", functionId.get());
				mav.addObject("prevVersionId", functionService.getPreviousVersionId(
						function.getName(), function.getVersion()));
				mav.addObject("nextVersionId", functionService.getNextVersionId(
						function.getName(), function.getVersion()));
				mav.addObject("updatedDependencies",
						functionService.getUpdatedDependencies(function));

			} else {
				// We are creating a new function
				BaseStatementFunctionDto function = new BaseStatementFunctionDto();
				function.setAuthor(principal.getName());
				function.setVersion(1);
				function.setState(PolicyElementState.DRAFT);
				mav.addObject("function", function);
			}
		}
		// Prevent optional from showing up in template evaluation
		mav.addObject("functionId", functionId.orElse(null));
		return mav;
	}

	@PostMapping("/{functionId}/create-revision")
	public RedirectView createRevision(@PathVariable Long functionId, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			BaseStatementFunctionEntity function = functionService
					.createRevision(functionId, principal.getName());
			redirectView = new RedirectView(
					MessageFormat.format("/functions/{0}/edit", function.getId()));
		} catch (BaseStatementFunctionException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot create new revision. " + e.getMessage());
			redirectView = new RedirectView("/functions/" + functionId);
		}
		return redirectView;
	}

	@PostMapping(value = {"/save", "/{functionId}/save"})
	public RedirectView saveFunction(@PathVariable Optional<Long> functionId,
			BaseStatementFunctionDto functionDto, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			functionDto.setAuthor(principal.getName());
			BaseStatementFunctionEntity function = functionService
					.saveFunction(functionId, functionDto);
			redirectAttributes.addFlashAttribute("success", "Saved function");
			redirectView = new RedirectView("/functions/" + function.getId());
		} catch (BaseStatementFunctionException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot save function. " + e.getMessage());
			redirectAttributes.addFlashAttribute("function", functionDto);
			redirectView = new RedirectView(
					functionId.map(id -> MessageFormat.format("/functions/{0}/edit", id))
							.orElse("/functions/create"));
		}
		return redirectView;
	}

	@PostMapping("/{functionId}/delete")
	public RedirectView deleteFunction(@PathVariable Long functionId, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			BaseStatementFunctionEntity function = functionService
					.deleteFunction(functionId, principal.getName());
			redirectAttributes.addFlashAttribute("success",
					MessageFormat.format("Deleted version {0} of function ''{1}''",
							function.getVersion(), function.getName()));
			redirectView = new RedirectView("/functions");
		} catch (BaseStatementFunctionException e) {
			redirectAttributes.addFlashAttribute("failure",
					"Cannot delete function. " + e.getMessage());
			redirectView = new RedirectView("/functions/" + functionId);
		}
		return redirectView;
	}

	@PostMapping("/{functionId}/state/{state}")
	public RedirectView updateFunctionState(@PathVariable Long functionId,
			@PathVariable String state, RedirectAttributes redirectAttributes)
			throws PolicyElementNotFoundException {
		try {
			functionService.updateFunctionState(functionId, state);
			redirectAttributes.addFlashAttribute("success", "Updated function state");
		} catch (BaseStatementFunctionException e) {
			redirectAttributes.addFlashAttribute("failure",
					"Cannot update function state. " + e.getMessage());
		}
		return new RedirectView("/functions/" + functionId);
	}

	/**
	 * Exception handler for a {@link PolicyElementNotFoundException}. Returns to the base of this
	 * controller
	 *
	 * @param e                  the exception thrown
	 * @param redirectAttributes the redirect attributes to add the exception message
	 * @return a string redirecting to the main functions page
	 */
	@ExceptionHandler(PolicyElementNotFoundException.class)
	public RedirectView handlePolicyElementNotFound(Exception e,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("failure", e.getMessage());
		return new RedirectView("/functions");
	}
}
