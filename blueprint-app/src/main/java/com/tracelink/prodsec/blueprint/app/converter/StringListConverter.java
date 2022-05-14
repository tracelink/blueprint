package com.tracelink.prodsec.blueprint.app.converter;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation of an {@link JacksonAbstractConverter} for a list of strings.
 *
 * @author mcool
 */
public class StringListConverter extends JacksonAbstractConverter<List<String>> {

	public StringListConverter() {
		super(new TypeReference<>() {
		});
	}
}
