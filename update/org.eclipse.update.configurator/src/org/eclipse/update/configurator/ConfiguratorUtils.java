/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.configurator;

import java.io.*;
import java.net.*;

import org.eclipse.update.internal.configurator.ConfigurationActivator;
import org.osgi.framework.*;

/**
 * Helper class to get platform configuration data without having to
 * use BootLoader methods from the compatibility layer.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public class ConfiguratorUtils {
	/**
	 * Returns the current platform configuration. This method replaces the one we used
	 * to call on BootLoader.
	 * 
	 * @return platform configuration used in current instance of platform
	 * @since 3.0
	 */
	public static IPlatformConfiguration getCurrentPlatformConfiguration() {
		// acquire factory service first
		BundleContext context = ConfigurationActivator.getBundleContext();
		ServiceReference configFactorySR = context.getServiceReference(IPlatformConfigurationFactory.class.getName());
		if (configFactorySR == null)
			throw new IllegalStateException();
		IPlatformConfigurationFactory configFactory = (IPlatformConfigurationFactory) context.getService(configFactorySR);
		if (configFactory == null)
			throw new IllegalStateException();
		// get the configuration using the factory
		IPlatformConfiguration currentConfig = configFactory.getCurrentPlatformConfiguration();
		context.ungetService(configFactorySR);
		return currentConfig;
	}
	
	/**
	 * Returns a platform configuration object, optionally initialized with previously saved
	 * configuration information. We will use this method instead of the old one in BootLoader.
	 * 
	 * @param url location of previously save configuration information. If <code>null</code>
	 * is specified, an empty configuration object is returned
	 * @return platform configuration used in current instance of platform
	 */
	public static IPlatformConfiguration getPlatformConfiguration(URL url) throws IOException {
		// acquire factory service first
		BundleContext context = ConfigurationActivator.getBundleContext();
		ServiceReference configFactorySR = context.getServiceReference(IPlatformConfigurationFactory.class.getName());
		if (configFactorySR == null)
			throw new IllegalStateException();
		IPlatformConfigurationFactory configFactory = (IPlatformConfigurationFactory) context.getService(configFactorySR);
		if (configFactory == null)
			throw new IllegalStateException();
		// get the configuration using the factory
		IPlatformConfiguration config = configFactory.getPlatformConfiguration(url);
		context.ungetService(configFactorySR);
		return config;
	}
	
	/**
	 * @return the URL of this eclispe installation
	 */
	public static URL getInstallURL() {
		return ConfigurationActivator.getInstallURL();
	}
}
