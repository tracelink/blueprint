package com.tracelink.prodsec.blueprint.app.converter;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An abstract implementation of an {@link AttributeConverter} that uses a Jackson {@link
 * ObjectMapper} to convert between the parameter type {@code <T>} and a string. Contains a type
 * reference for the deserialization.
 *
 * @param <T> the entity attribute type for conversion
 */
public abstract class JacksonAbstractConverter<T> implements AttributeConverter<T, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private final TypeReference<T> typeReference;

	public JacksonAbstractConverter(TypeReference<T> typeReference) {
		this.typeReference = typeReference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String convertToDatabaseColumn(T attribute) {
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T convertToEntityAttribute(String dbData) {
		try {
			return OBJECT_MAPPER.readValue(dbData, typeReference);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
