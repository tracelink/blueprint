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
import com.tracelink.prodsec.blueprint.app.exception.BaseStatementException;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.mvc.BlueprintModelAndView;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionService;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementEntity;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionEntity;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Controller to retrieve base statements from the server, as well as to handle creation, update,
 * deletion, and state changes for base statements.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/base-statements")
@PreAuthorize(
		"hasAnyAuthority('" + CoreRole.ADMIN_ROLE + "', '" + CoreRole.BASE_STMT_EDITOR_ROLE + "')")
public class BaseStatementsController {

	private final PolicyTypeService policyTypeService;
	private final BaseStatementService baseStatementService;
	private final BaseStatementFunctionService functionService;

	public BaseStatementsController(@Autowired PolicyTypeService policyTypeService,
			@Autowired BaseStatementService baseStatementService,
			@Autowired BaseStatementFunctionService functionService) {
		this.policyTypeService = policyTypeService;
		this.baseStatementService = baseStatementService;
		this.functionService = functionService;
	}

	@GetMapping(value = {"", "/{baseStatementId}"})
	public ModelAndView getBaseStatements(@PathVariable Optional<Long> baseStatementId)
			throws PolicyElementNotFoundException {
		BlueprintModelAndView mav = new BlueprintModelAndView("base-statements");
		mav.addScriptReference("/scripts/base-statements.js");
		List<String> policyTypes = policyTypeService.getPolicyTypes();
		mav.addObject("policyTypes", policyTypes);
		List<String> latestFunctions = functionService.getLatestFunctions();
		mav.addObject("functions", latestFunctions);
		mav.addObject("argumentTypes", ArgumentType.getTypes());
		Map<String, Long> baseStatements = baseStatementService.getBaseStatements();
		mav.addObject("baseStatements", baseStatements);
		mav.addObject("editable", false);

		BaseStatementEntity baseStatement = null;
		if (baseStatementId.isPresent()) {
			// Get base statement for the given id
			baseStatement = baseStatementService.getBaseStatement(baseStatementId.get());
		} else if (!baseStatements.isEmpty()) {
			// Get first base statement in map if id is not present
			baseStatement = baseStatementService
					.getBaseStatement(baseStatements.entrySet().iterator().next().getValue());
		}
		// If we have a base statement, add objects to model
		if (baseStatement != null) {
			mav.addObject("baseStatement", baseStatement.toDto());
			mav.addObject("baseStatementId", baseStatement.getId());
			mav.addObject("prevVersionId", baseStatementService.getPreviousVersionId(
					baseStatement.getName(), baseStatement.getVersion()));
			mav.addObject("nextVersionId", baseStatementService
					.getNextVersionId(baseStatement.getName(), baseStatement.getVersion()));
			mav.addObject("updatedFunction",
					functionService.getUpdatedFunction(baseStatement.getFunction().getName(),
							baseStatement.getFunction().getVersion())
							.map(BaseStatementFunctionEntity::getVersionedName).orElse(null));
		}
		return mav;
	}

	@GetMapping(value = {"/create", "/{baseStatementId}/edit"})
	public ModelAndView createEditBaseStatement(@PathVariable Optional<Long> baseStatementId,
			Principal
					principal, Model model, RedirectAttributes redirectAttributes)
			throws PolicyElementNotFoundException {
		// Set up basic view, scripts and objects
		BlueprintModelAndView mav = new BlueprintModelAndView("base-statements");
		mav.addScriptReference("/scripts/base-statements.js");
		List<String> policyTypes = policyTypeService.getPolicyTypes();
		mav.addObject("policyTypes", policyTypes);
		List<String> latestFunctions = functionService.getLatestFunctions();
		mav.addObject("functions", latestFunctions);
		mav.addObject("argumentTypes", ArgumentType.getTypes());
		Map<String, Long> baseStatements = baseStatementService.getBaseStatements();
		mav.addObject("baseStatements", baseStatements);
		mav.addObject("editable", true);

		// If not redirected from another request, add base statement object to create/edit
		if (!model.containsAttribute("baseStatement")) {
			if (baseStatementId.isPresent()) {
				// We are editing an existing base statement
				BaseStatementEntity baseStatement = baseStatementService
						.getBaseStatement(baseStatementId.get());
				// Redirect to base statement view screen if base statement is not a draft
				if (!baseStatement.getState().equals(PolicyElementState.DRAFT)) {
					redirectAttributes.addFlashAttribute("failure",
							MessageFormat
									.format("Cannot edit a base statement that is in the {0} state",
											baseStatement.getState().getName().toLowerCase()));
					return new ModelAndView(
							new RedirectView("/base-statements/" + baseStatementId.get()));
				}
				// Add objects to model
				mav.addObject("baseStatement", baseStatement.toDto());
				mav.addObject("baseStatementId", baseStatementId.get());
				mav.addObject("prevVersionId", baseStatementService.getPreviousVersionId(
						baseStatement.getName(), baseStatement.getVersion()));
				mav.addObject("nextVersionId", baseStatementService.getNextVersionId(
						baseStatement.getName(), baseStatement.getVersion()));
				mav.addObject("updatedFunction",
						functionService.getUpdatedFunction(baseStatement.getFunction().getName(),
								baseStatement.getFunction().getVersion())
								.map(BaseStatementFunctionEntity::getVersionedName).orElse(null));

			} else {
				// We are creating a new base statement
				BaseStatementDto baseStatement = new BaseStatementDto();
				baseStatement.setAuthor(principal.getName());
				baseStatement.setVersion(1);
				baseStatement.setState(PolicyElementState.DRAFT);
				mav.addObject("baseStatement", baseStatement);
			}
		}
		// Prevent optional from showing up in template evaluation
		mav.addObject("baseStatementId", baseStatementId.orElse(null));
		return mav;
	}

	@PostMapping("/{baseStatementId}/create-revision")
	public RedirectView createRevision(@PathVariable Long baseStatementId, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			BaseStatementEntity baseStatement = baseStatementService
					.createRevision(baseStatementId, principal.getName());
			redirectView = new RedirectView(
					MessageFormat.format("/base-statements/{0}/edit", baseStatement.getId()));
		} catch (BaseStatementException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot create new revision. " + e.getMessage());
			redirectView = new RedirectView("/base-statements/" + baseStatementId);
		}
		return redirectView;
	}

	@PostMapping(value = {"/save", "/{baseStatementId}/save"})
	public RedirectView saveBaseStatement(@PathVariable Optional<Long> baseStatementId,
			BaseStatementDto baseStatementDto, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			baseStatementDto.setAuthor(principal.getName());
			BaseStatementEntity baseStatement = baseStatementService
					.saveBaseStatement(baseStatementId, baseStatementDto);
			redirectAttributes.addFlashAttribute("success", "Saved base statement");
			redirectView = new RedirectView("/base-statements/" + baseStatement.getId());
		} catch (BaseStatementException e) {
			redirectAttributes
					.addFlashAttribute("failure", "Cannot save base statement. " + e.getMessage());
			redirectAttributes.addFlashAttribute("baseStatement", baseStatementDto);
			redirectView = new RedirectView(
					baseStatementId.map(id -> MessageFormat.format("/base-statements/{0}/edit", id))
							.orElse("/base-statements/create"));
		}
		return redirectView;
	}

	@PostMapping("/{baseStatementId}/delete")
	public RedirectView deleteBaseStatement(@PathVariable Long baseStatementId, Principal principal,
			RedirectAttributes redirectAttributes) throws PolicyElementNotFoundException {
		RedirectView redirectView;
		try {
			BaseStatementEntity baseStatement = baseStatementService
					.deleteBaseStatement(baseStatementId, principal.getName());
			redirectAttributes.addFlashAttribute("success",
					MessageFormat.format("Deleted version {0} of base statement ''{1}''",
							baseStatement.getVersion(), baseStatement.getName()));
			redirectView = new RedirectView("/base-statements");
		} catch (BaseStatementException e) {
			redirectAttributes.addFlashAttribute("failure",
					"Cannot delete base statement. " + e.getMessage());
			redirectView = new RedirectView("/base-statements/" + baseStatementId);
		}
		return redirectView;
	}

	@PostMapping("/{baseStatementId}/state/{state}")
	public RedirectView updateBaseStatementState(@PathVariable Long baseStatementId,
			@PathVariable String state, RedirectAttributes redirectAttributes)
			throws PolicyElementNotFoundException {
		try {
			baseStatementService.updateBaseStatementState(baseStatementId, state);
			redirectAttributes.addFlashAttribute("success", "Updated base statement state");
		} catch (BaseStatementException e) {
			redirectAttributes.addFlashAttribute("failure",
					"Cannot update base statement state. " + e.getMessage());
		}
		return new RedirectView("/base-statements/" + baseStatementId);
	}

	/**
	 * Exception handler for a {@link PolicyElementNotFoundException}. Returns to the base of this
	 * controller
	 *
	 * @param e                  the exception thrown
	 * @param redirectAttributes the redirect attributes to add the exception message
	 * @return a string redirecting to the main base statements page
	 */
	@ExceptionHandler(PolicyElementNotFoundException.class)
	public RedirectView handlePolicyElementNotFound(Exception e,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("failure", e.getMessage());
		return new RedirectView("/base-statements");
	}
}
