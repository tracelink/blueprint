package com.tracelink.prodsec.blueprint.app.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.tracelink.prodsec.blueprint.app.auth.model.CoreRole;
import com.tracelink.prodsec.blueprint.app.exception.PolicyElementNotFoundException;
import com.tracelink.prodsec.blueprint.app.policy.PolicyTypeEntity;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyTypeService;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementArgumentDto;
import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

/**
 * Controller to retrieve HTML fragments to dynamically add new elements to the UI when building a
 * policy or base statement.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/fragments")
public class FragmentsController {

	private final BaseStatementService baseStatementService;
	private final PolicyTypeService policyTypeService;

	public FragmentsController(@Autowired BaseStatementService baseStatementService,
			@Autowired PolicyTypeService policyTypeService) {
		this.baseStatementService = baseStatementService;
		this.policyTypeService = policyTypeService;
	}

	@GetMapping("/clause")
	public String getClause(@RequestParam String type, Model model)
			throws PolicyElementNotFoundException {
		PolicyTypeEntity policyType = policyTypeService.getPolicyType(type);
		model.addAttribute("baseStatements",
				baseStatementService.getLatestBaseStatementsForPolicyType(policyType));
		return "fragments/clause.html";
	}

	@GetMapping("/statement")
	public String getStatement(@RequestParam int clauseIndex, @RequestParam int statementIndex,
			@RequestParam String baseStatement, Model model) throws PolicyElementNotFoundException {
		model.addAttribute("clauseIndex", clauseIndex);
		model.addAttribute("statementIndex", statementIndex);
		model.addAttribute("baseStatement",
				baseStatementService.getBaseStatement(baseStatement).toDto());
		return "fragments/statement.html";
	}

	@GetMapping("/arguments")
	@PreAuthorize(
			"hasAnyAuthority('" + CoreRole.ADMIN_ROLE + "', '" + CoreRole.BASE_STMT_EDITOR_ROLE
					+ "')")
	public String getArguments(@RequestParam Optional<Long> baseStatementId,
			@RequestParam String functionName, Model model)
			throws PolicyElementNotFoundException {
		List<BaseStatementArgumentDto> arguments = baseStatementService
				.getArgumentsForBaseStatement(baseStatementId, functionName);
		model.addAttribute("arguments", arguments);
		model.addAttribute("editable", true);
		model.addAttribute("argumentTypes", ArgumentType.getTypes());
		return "fragments/arguments.html";
	}

	/**
	 * Exception handler for a {@link PolicyElementNotFoundException}. Returns a 404 error page.
	 *
	 * @param e the exception thrown
	 * @return a {@link ModelAndView} for the 404 error page
	 */
	@ExceptionHandler(PolicyElementNotFoundException.class)
	public ModelAndView handlePolicyElementNotFound(Exception e) {
		ModelAndView mav = new ModelAndView("error");
		mav.setStatus(HttpStatus.NOT_FOUND);
		mav.addObject("message", e.getMessage());
		return mav;
	}

}
