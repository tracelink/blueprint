package com.tracelink.prodsec.blueprint.app.converter;

import org.springframework.core.convert.converter.Converter;

import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Converter to allow parsing from the {@link PolicyElementState} display name to its enum value
 * for request parameter binding.
 *
 * @author mcool
 */
public class PolicyElementStateConverter implements Converter<String, PolicyElementState> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PolicyElementState convert(String s) {
		return PolicyElementState.getStateForName(s);
	}
}
