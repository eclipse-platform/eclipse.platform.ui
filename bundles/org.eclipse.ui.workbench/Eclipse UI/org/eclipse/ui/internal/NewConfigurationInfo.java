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

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;

/**
 * Configuation info class;
 * <p>
 * The information within subclasses of this object is obtained from a configuration
 * "ini" file". This file resides within an install configurations directory and must
 * be a standard java property file. A properties file may also be used to NL values
 * in the ini file. 
 * </p>
 */

public abstract class NewConfigurationInfo {

	private IPluginDescriptor desc;
	private URL baseURL;
	private String featureId;
	private PluginVersionIdentifier versionId;
	private String iniFilename;
	private String propertiesFilename;
	private String mappingsFilename;

	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$
	
	protected NewConfigurationInfo(String fId, PluginVersionIdentifier vId, String ini, String properties, String mappings) {
		featureId = fId;
		versionId = vId;
		iniFilename = ini;
		propertiesFilename = properties;
		mappingsFilename = mappings;
	}

	/**
	 * R1.0 platform.ini handling using "main" plugin and fragments for NL
	 */
	public void readINIFile() throws CoreException {
		if (featureId == null) {
			reportINIFailure(null, "Unknown configuration identifier"); //$NON-NLS-1$
			return;
		}

		// attempt to locate the corresponding plugin
		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null) {
			reportINIFailure(null, "Plugin registry is null"); //$NON-NLS-1$
			return;
		}
		if (getDescriptor() == null) {
			reportINIFailure(null, "Missing plugin descriptor for " + featureId); //$NON-NLS-1$
			return;
		}
		this.baseURL = desc.getInstallURL();

		// load the ini, properties and mapping files	
		URL iniURL = null;
		try {
			iniURL = desc.find(new Path("$nl$").append(iniFilename)); //$NON-NLS-1$
			if (iniURL != null)
				iniURL = Platform.resolve(iniURL);
		} catch (IOException e) {
			// null check below
		}
		if (iniURL == null) {
			reportINIFailure(null, "Unable to load plugin file: " + iniFilename); //$NON-NLS-1$
			return;
		}
		
		URL propertiesURL = null;
		try {
			propertiesURL = desc.find(new Path("$nl$").append(propertiesFilename)); //$NON-NLS-1$
			if (propertiesURL != null)
				propertiesURL = Platform.resolve(propertiesURL);
		} catch (IOException e) {
			reportINIFailure(null, "Unable to load plugin file: " + propertiesFilename); //$NON-NLS-1$
		}

		URL mappingsURL = null;
		try {
			mappingsURL = desc.find(new Path("$nl$").append(mappingsFilename)); //$NON-NLS-1$
			if (mappingsURL != null)
				mappingsURL = Platform.resolve(mappingsURL);
		} catch (IOException e) {
			reportINIFailure(null, "Unable to load mapping file: " + mappingsURL); //$NON-NLS-1$
		}

		// OK to pass null properties and/or mapping file
		readINIFile(iniURL, propertiesURL, mappingsURL);
	}
		
	/**
	 * Gets the descriptor
	 * @return Returns a IPluginDescriptor
	 */
	public IPluginDescriptor getDescriptor() {
		if(desc == null) {
			IPlatformConfiguration platformConfiguration = BootLoader.getCurrentPlatformConfiguration();
			IPlatformConfiguration.IFeatureEntry feature = platformConfiguration.findConfiguredFeatureEntry(featureId);
			if(feature == null)
				return null;
			String pluginId = feature.getFeaturePluginIdentifier();
			String pluginVersion = feature.getFeaturePluginVersion();
			IPluginRegistry reg = Platform.getPluginRegistry();
			if (pluginVersion == null) {
				desc = reg.getPluginDescriptor(pluginId);
			} else {
				PluginVersionIdentifier vid = new PluginVersionIdentifier(pluginVersion);	
				desc = reg.getPluginDescriptor(pluginId, vid);
				if (desc == null)
					// try ignoring the version
					desc = reg.getPluginDescriptor(pluginId);
			}
		}		
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
	 * Gets the feature id
	 * @return the feature id
	 */
	public String getFeatureId() {
		return featureId;
	}
	/**
	 * Gets the version id
	 * @return the version id
	 */
	protected PluginVersionIdentifier getVersionId() {
		return versionId;
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
	 * @param mappings 
	 * @param runtime mappings or <code>null</code>
	 * @return the resource string
	 */
	protected String getResourceString(String value, ResourceBundle b, String[] mappings, Hashtable runtimeMappings) {
		
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

		String result = null;
		try {
			result = b.getString(key.substring(1));
		} catch (MissingResourceException e) {
			reportINIFailure(e, "Property \"" + key + "\" not found");//$NON-NLS-1$ //$NON-NLS-2$
			return dflt;
		}
		if (runtimeMappings != null) {
			for (Enumeration e = runtimeMappings.keys(); e.hasMoreElements();) {
				String keyValue = (String) e.nextElement();
				int i = result.indexOf(keyValue);
				if (i != -1) {
					String s1 = result.substring(0,i);
					String s2 = (String) runtimeMappings.get(keyValue);
					String s3 = result.substring(i+keyValue.length());
					result = s1 + s2 + s3;
				}
			}
		}
	
		if (result.indexOf('{') != -1) {
			// We test for the curly braces since due to NL issues we do not
			// want to use MessageFormat unless we have to.
			result = MessageFormat.format(result, mappings);
		}
		
		return result;	
	}

	/**
	 * Read the ini file.
	 */
	protected abstract void readINIFile(URL iniURL, URL propertiesURL, URL mappingURL)
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
		WorkbenchPlugin.log("Problem reading configuration info for: " + getFeatureId(), iniStatus);//$NON-NLS-1$
	}
}
