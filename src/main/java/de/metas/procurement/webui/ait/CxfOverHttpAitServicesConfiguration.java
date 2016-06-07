package de.metas.procurement.webui.ait;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.gwt.thirdparty.guava.common.base.Strings;

/*
 * #%L
 * de.metas.procurement.webui
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 *
 * @author metas-dev <dev@metasfresh.com>
 * @task https://metasfresh.atlassian.net/browse/FRESH-243
 */
@Configuration()
@Profile("ait")    // see http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html
public class CxfOverHttpAitServicesConfiguration
{

	private static final transient Logger logger = LoggerFactory.getLogger(CxfOverHttpAitServicesConfiguration.class);

	private static final String DEFAULT_UrlMapping = "/rest/*";

	private String urlMapping = DEFAULT_UrlMapping;

	@Autowired
	private ApplicationContext context;

	@Bean
	public ServletRegistrationBean cxfServletRegistrationBean()
	{
		if (Strings.isNullOrEmpty(urlMapping))
		{
			logger.info("No urlMapping defined. Using default.");
			urlMapping = DEFAULT_UrlMapping;
		}

		logger.info("cxf.urlMapping = {}", urlMapping);

		final ServletRegistrationBean registrationBean = new ServletRegistrationBean(new CXFServlet(), urlMapping);
		registrationBean.setAsyncSupported(true);
		registrationBean.setLoadOnStartup(1);
		registrationBean.setName("RestfullAITEndpointsConfiguration");
		return registrationBean;
	}

	/**
	 * Start the JAX-RS server
	 *
	 * @return
	 */
	@Bean
	public Server jaxRsServer(
			final SpringBus bus,
			final JacksonJaxbJsonProvider jacksonJaxbJsonProvider,
			final LoggingFeature loggingFeature)
	{

		final SpringResourceFactory factory = new SpringResourceFactory(AITService.class.getSimpleName());
		factory.setApplicationContext(context);

		final JAXRSServerFactoryBean svrFactory = new JAXRSServerFactoryBean();
		svrFactory.setProvider(jacksonJaxbJsonProvider);
		svrFactory.setBus(bus);
		svrFactory.setResourceProvider(factory);
		svrFactory.getFeatures().add(loggingFeature);
		return svrFactory.create();
	}
}
