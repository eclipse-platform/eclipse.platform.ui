/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;

/**
 * Configuation info class;
 * <p>
 * The information within subclasses of this object is obtained from a configuration
 * "ini" file". This file resides within an install configurations directory and must
 * be a standard java property file. A properties file may also be used to NL values
 * in the ini file. 
 * </p>
 */

public abstract class ConfigurationInfo {

	private IPluginDescriptor desc;
	private URL baseURL;
	private String iniFilename;
	private String propertiesFilename;

	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$
	
	protected ConfigurationInfo(String ini, String properties) {
		iniFilename = ini;
		propertiesFilename = properties;
	}

	/**
	 * R1.0 platform.ini handling using "main" plugin and fragments for NL
	 */
	public void readINIFile() throws CoreException {
		// determine the identifier of the "dominant" application 
		IPlatformConfiguration conf = BootLoader.getCurrentPlatformConfiguration();
		String configName = conf.getPrimaryFeatureIdentifier();
		if (configName == null) {
			reportINIFailure(null, "Unknown configuration identifier"); //$NON-NLS-1$
			return;
		}

		// attempt to locate its corresponding "main" plugin
		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null) {
			reportINIFailure(null, "Plugin registry is null"); //$NON-NLS-1$
			return;
		}
		int index = configName.lastIndexOf("_"); //$NON-NLS-1$
		if (index == -1)
			this.desc = reg.getPluginDescriptor(configName);
		else {
			String mainPluginName = configName.substring(0, index);
			PluginVersionIdentifier mainPluginVersion = null;
			try {
				mainPluginVersion =
					new PluginVersionIdentifier(configName.substring(index + 1));
			} catch (Exception e) {
				reportINIFailure(e, "Unknown plugin version " + configName); //$NON-NLS-1$
				return;
			}
			this.desc = reg.getPluginDescriptor(mainPluginName, mainPluginVersion);
		}
		if (this.desc == null) {
			reportINIFailure(null, "Missing plugin descriptor for " + configName); //$NON-NLS-1$
			return;
		}
		this.baseURL = desc.getInstallURL();

		// load the platform.ini and platform.properties file	
		URL iniURL = desc.find(new Path(iniFilename));
		if (iniURL == null) {
			reportINIFailure(null, "Unable to load plugin file: " + iniFilename); //$NON-NLS-1$
			return;
		}
		
		URL propertiesURL = desc.find(new Path(propertiesFilename));
		// OK to pass null properties file
		readINIFile(iniURL, propertiesURL);
	}
		
	/**
	 * Gets the descriptor
	 * @return Returns a IPluginDescriptor
	 */
	protected IPluginDescriptor getDescriptor() {
		return desc;
	}
	/**
	 * Gets the baseURL
	 * @return Returns a URL
	 */
	protected URL getBaseURL() {
		return baseURL;
	}
	/**
	 * Returns a resource string corresponding to the given argument 
	 * value and bundle.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the given resource bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed against the
	 * specified resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string begining with the "%" character.
	 * Note that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * For example, assume resource bundle plugin.properties contains
	 * name = Project Name
	 * <pre>
	 *     <li>getResourceString("Hello World") returns "Hello World"</li>
	 *     <li>getResourceString("%name") returns "Project Name"</li>
	 *     <li>getResourceString("%name Hello World") returns "Project Name"</li>
	 *     <li>getResourceString("%abcd Hello World") returns "Hello World"</li>
	 *     <li>getResourceString("%abcd") returns "%abcd"</li>
	 *     <li>getResourceString("%%name") returns "%name"</li>
	 *     <li>getResourceString(<code>null</code>) returns <code>null</code></li>
	 * </pre>
	 * </p>
	 *
	 * @param value the value or <code>null</code>
	 * @param b the resource bundle or <code>null</code>
	 * @return the resource string
	 */
	protected String getResourceString(String value, ResourceBundle b) {
		
		if(value == null)
			return null;
		String s = value.trim();

		if (!s.startsWith(KEY_PREFIX))
			return s;

		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" "); //$NON-NLS-1$
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (b == null)
			return dflt;

		try {
			return b.getString(key.substring(1));
		} catch (MissingResourceException e) {
			reportINIFailure(e, "Property \"" + key + "\" not found");//$NON-NLS-1$ //$NON-NLS-2$
			return dflt;
		}
	}

	/**
	 * Read the ini file.
	 */
	protected abstract void readINIFile(URL iniURL, URL propertiesURL)
		throws CoreException;
	
	/**
	 * Report an ini failure
	 */
	protected void reportINIFailure(Exception e, String message) {
		if (!WorkbenchPlugin.DEBUG) {
			// only report ini problems if the -debug command line argument is used
			return;
		}
		
		IStatus iniStatus = new Status(IStatus.ERROR, WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
										0, message, e);
		WorkbenchPlugin.log("Problem reading configuration info.", iniStatus);//$NON-NLS-1$
	}
}
