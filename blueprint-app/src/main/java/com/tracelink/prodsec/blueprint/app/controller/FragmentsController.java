package com.tracelink.prodsec.blueprint.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.service.PolicyService;

/**
 * Controller to retrieve HTML fragments to dynamically add new clauses and statements to the UI
 * when building a policy.
 *
 * @author mcool
 */
@Controller
@RequestMapping("/fragments")
public class FragmentsController {

	private final BaseStatementService baseStatementService;

	public FragmentsController(@Autowired BaseStatementService baseStatementService,
			@Autowired PolicyService policyService) {
		this.baseStatementService = baseStatementService;
	}

	@GetMapping("/clause")
	public String getClause(@RequestParam String type, Model model) {
		model.addAttribute("baseStatements",
				baseStatementService.getBaseStatementsForPolicyType(type));
		return "fragments/clause.html";
	}

	@GetMapping("/statement")
	public String getStatement(@RequestParam int clauseIndex, @RequestParam int statementIndex,
			@RequestParam String baseStatement, Model model) {
		model.addAttribute("clauseIndex", clauseIndex);
		model.addAttribute("statementIndex", statementIndex);
		model.addAttribute("baseStatement",
				baseStatementService.getBaseStatement(baseStatement).toDto());
		return "fragments/statement.html";
	}

}
