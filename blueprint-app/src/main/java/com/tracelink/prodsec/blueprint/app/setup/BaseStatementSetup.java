package com.tracelink.prodsec.blueprint.app.setup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	public BaseStatementSetup(@Autowired BaseStatementService baseStatementService) {
		this.baseStatementService = baseStatementService;
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
			baseStatementService.importBaseStatements(importedBaseStatements.getBaseStatements(),
					importedBaseStatements.getBaseStatementFunctions());
		}
	}

	/**
	 * DTO class to read the values from the JSON resource file.
	 */
	private static class BaseStatementImportDto {

		private List<BaseStatementDto> baseStatements = new ArrayList<>();
		private List<BaseStatementFunctionDto> baseStatementFunctions = new ArrayList<>();

		public List<BaseStatementDto> getBaseStatements() {
			return baseStatements;
		}

		public void setBaseStatements(
				List<BaseStatementDto> baseStatements) {
			this.baseStatements = baseStatements;
		}

		public List<BaseStatementFunctionDto> getBaseStatementFunctions() {
			return baseStatementFunctions;
		}

		public void setBaseStatementFunctions(
				List<BaseStatementFunctionDto> baseStatementFunctions) {
			this.baseStatementFunctions = baseStatementFunctions;
		}
	}

}
