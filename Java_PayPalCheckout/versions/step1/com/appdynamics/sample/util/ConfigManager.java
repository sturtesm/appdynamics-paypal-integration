package com.appdynamics.sample.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class ConfigManager {
	private static final String DEFAULT_CONFIGURATION_FILE = "payment-services.properties";
	private static final Logger log = Logger.getLogger(ConfigManager.class);

	Properties properties = null;
	
	private boolean propertyLoaded = false;

	public ConfigManager() {
		loadProperties();
	}

	private void loadProperties() {
		
		if (!isPropertyLoaded() ) {
			/*
			 * Load configuration for default 'sdk_config.properties'
			 */
			ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_CONFIGURATION_FILE);

			properties = new Properties();
			try {
				InputStream inputStream = resourceLoader.getInputStream();
				properties.load(inputStream);
			} catch (IOException e) {
				// We tried reading the config, but it seems like you dont have it. Skipping...
				log.debug(DEFAULT_CONFIGURATION_FILE + " not present. Skipping...");
			} finally {
				setPropertyLoaded(true);
			}
		}
	}

	public boolean isPropertyLoaded() {
		return propertyLoaded;
	}

	public void setPropertyLoaded(boolean propertyLoaded) {
		this.propertyLoaded = propertyLoaded;
	}
}
