package com.tracelink.prodsec.blueprint.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tracelink.prodsec.blueprint.app.converter.ArgumentTypeConverter;
import com.tracelink.prodsec.blueprint.app.converter.PolicyElementStateConverter;
import com.tracelink.prodsec.blueprint.core.statement.PolicyElementState;

/**
 * Web configuration class to add custom converters to the formatting registry for {@link
 * PolicyElementState} objects.
 *
 * @author mcool
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new PolicyElementStateConverter());
		registry.addConverter(new ArgumentTypeConverter());
	}
}
