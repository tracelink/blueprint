package com.tracelink.prodsec.blueprint.app.converter;

import javax.persistence.AttributeConverter;

import org.springframework.core.convert.converter.Converter;

import com.tracelink.prodsec.blueprint.core.argument.ArgumentType;

/**
 * Implementation of an {@link AttributeConverter} for the {@link ArgumentType} class. Uses {@link
 * ArgumentType#getTypeForName(String)} to convert from a database string.
 *
 * @author mcool
 */
public class ArgumentTypeConverter implements AttributeConverter<ArgumentType, String>,
		Converter<String, ArgumentType> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String convertToDatabaseColumn(ArgumentType attribute) {
		return attribute.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArgumentType convertToEntityAttribute(String dbData) {
		return ArgumentType.getTypeForName(dbData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArgumentType convert(String s) {
		return ArgumentType.getTypeForName(s);
	}
}
