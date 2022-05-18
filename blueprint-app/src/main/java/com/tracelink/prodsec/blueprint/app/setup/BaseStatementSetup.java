package com.tracelink.prodsec.blueprint.app.setup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementFunctionService;
import com.tracelink.prodsec.blueprint.app.service.BaseStatementService;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementDto;
import com.tracelink.prodsec.blueprint.app.statement.BaseStatementFunctionDto;

/**
 * Setup component to read base statements and base statement functions from the JSON resource file
 * and import them using the {@link BaseStatementService}.
 *
 * @author mcool
 */
@Component
public class BaseStatementSetup {

	private final BaseStatementService baseStatementService;
	private final BaseStatementFunctionService functionService;

	public BaseStatementSetup(@Autowired BaseStatementService baseStatementService,
			@Autowired BaseStatementFunctionService functionService) {
		this.baseStatementService = baseStatementService;
		this.functionService = functionService;
	}

	/**
	 * Imports the default set of base statements into the database after application startup.
	 *
	 * @param event the event triggered after application startup
	 * @throws Exception if an error occurs
	 */
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) throws Exception {
		try (InputStream is = getClass().getClassLoader()
				.getResourceAsStream("static/baseStatements.json")) {
			ObjectMapper objectMapper = new ObjectMapper();
			BaseStatementImportDto importedBaseStatements = objectMapper
					.readValue(is, BaseStatementImportDto.class);
			// Set author on functions and base statements
			importedBaseStatements.getFunctions()
					.forEach(function -> function.setAuthor("admin"));
			importedBaseStatements.getBaseStatements().forEach(baseStatement -> baseStatement
					.setAuthor("admin"));
			// Validate and save functions to the database
			functionService.importBaseStatementFunctions(importedBaseStatements.getFunctions());
			// Validate and save base statements to the database
			baseStatementService.importBaseStatements(importedBaseStatements.getBaseStatements());
		}
	}

	/**
	 * DTO class to read the values from the JSON resource file.
	 */
	private static class BaseStatementImportDto {

		private List<BaseStatementDto> baseStatements = new ArrayList<>();
		private List<BaseStatementFunctionDto> functions = new ArrayList<>();

		public List<BaseStatementDto> getBaseStatements() {
			return baseStatements;
		}

		public void setBaseStatements(List<BaseStatementDto> baseStatements) {
			this.baseStatements = baseStatements;
		}

		public List<BaseStatementFunctionDto> getFunctions() {
			return functions;
		}

		public void setFunctions(
				List<BaseStatementFunctionDto> functions) {
			this.functions = functions;
		}
	}

}
