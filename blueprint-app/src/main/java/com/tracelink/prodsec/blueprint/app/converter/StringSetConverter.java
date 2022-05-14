package com.tracelink.prodsec.blueprint.app.converter;

import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation of an {@link JacksonAbstractConverter} for a set of strings.
 *
 * @author mcool
 */
public class StringSetConverter extends JacksonAbstractConverter<Set<String>> {

	public StringSetConverter() {
		super(new TypeReference<>() {
		});
	}
}
