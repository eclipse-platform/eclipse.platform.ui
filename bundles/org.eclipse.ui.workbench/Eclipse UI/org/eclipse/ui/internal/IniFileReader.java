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
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

/**
 * Reads the information found in an "INI" file. This file must be in a
 * standard Java properties format. A properties file may also be provided
 * to NL values in the INI file - values must start with the % prefix. A
 * mapping file may also be provided that contains "fill-ins" for the
 * properties file - format being "n = some text", where n is a number.
 */
public class IniFileReader {
	private static final Status OK_STATUS = new Status(IStatus.OK,PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$
	private static final String NLS_TAG = "$nl$"; //$NON-NLS-1$

	private IPluginDescriptor pluginDescriptor;
	private String featureId;
	private String iniFilename;
	private String propertiesFilename;
	private String mappingsFilename;
	private Properties ini = null;
	private PropertyResourceBundle properties = null;
	private String[] mappings = null;

	/**
	 * Creates an INI file reader that can parse the contents into key,value pairs.
	 * 
	 * @param featureId the unique identifier of the feature, must not be <code>null</code>
	 * @param iniFilename the INI file name, must not be <code>null</code>
	 * @param propertiesFilename the properties filename, can be <code>null</code> if not required
	 * @param mappingsFilename the mappings filename, can be <code>null</code> if not required
	 */
	public IniFileReader(String featureId, String iniFilename, String propertiesFilename, String mappingsFilename) {
		super();
		
		if (featureId == null || iniFilename == null) {
			throw new IllegalArgumentException();
		}
			
		this.featureId = featureId;
		this.iniFilename = iniFilename;
		this.propertiesFilename = propertiesFilename;
		this.mappingsFilename = mappingsFilename;
	}

	/**
	 * Read the contents of the INI, properties, and mappings files.
	 * Does nothing if the content has already been read and parsed.
	 * 
	 * @return an <code>IStatus</code> indicating the success or failure
	 * 	of reading and parsing the INI file content
	 */
	public IStatus load() {
		if (ini != null)
			return OK_STATUS;
			
		// attempt to locate the corresponding plugin
		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null) {
			String message = WorkbenchMessages.getString("IniFileReader.MissingReg"); //$NON-NLS-1$
			return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null);
		}
		if (getPluginDescriptor(reg) == null) {
			String message = WorkbenchMessages.format("IniFileReader.MissingDesc", new Object[] {featureId}); //$NON-NLS-1$
			return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null);
		}

		// Determine the ini file location
		URL iniURL = null;
		IOException ioe = null;
		try {
			iniURL = pluginDescriptor.find(new Path(NLS_TAG).append(iniFilename));
			if (iniURL != null)
				iniURL = Platform.resolve(iniURL);
		} catch (IOException e) {
			ioe = e;
		}
		if (iniURL == null) {
			String message = WorkbenchMessages.format("IniFileReader.OpenINIError", new Object[] {iniFilename}); //$NON-NLS-1$
			return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, ioe);
		}
		
		// Determine the properties file location
		URL propertiesURL = null;
		if (propertiesFilename != null & propertiesFilename.length() > 0) {
			try {
				propertiesURL = pluginDescriptor.find(new Path(NLS_TAG).append(propertiesFilename));
				if (propertiesURL != null)
					propertiesURL = Platform.resolve(propertiesURL);
			} catch (IOException e) {
				String message = WorkbenchMessages.format("IniFileReader.OpenPropError", new Object[] {propertiesFilename}); //$NON-NLS-1$
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
			}
		}

		// Determine the mappings file location
		URL mappingsURL = null;
		if (mappingsFilename != null && mappingsFilename.length() > 0) {
			try {
				mappingsURL = pluginDescriptor.find(new Path(NLS_TAG).append(mappingsFilename));
				if (mappingsURL != null)
					mappingsURL = Platform.resolve(mappingsURL);
			} catch (IOException e) {
				String message = WorkbenchMessages.format("IniFileReader.OpenMapError", new Object[] {mappingsFilename}); //$NON-NLS-1$
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
			}
		}

		// OK to pass null properties and/or mapping file
		return load(iniURL, propertiesURL, mappingsURL);
	}
		
	/*
	 * Gets the plugin descriptor for the feature id
	 * @return the <code>IPluginDescriptor</code> or <code>null</code>
	 */
	private IPluginDescriptor getPluginDescriptor(IPluginRegistry reg) {
		if (pluginDescriptor == null) {
			IPlatformConfiguration platformConfiguration = BootLoader.getCurrentPlatformConfiguration();
			IPlatformConfiguration.IFeatureEntry feature = platformConfiguration.findConfiguredFeatureEntry(featureId);
			if (feature == null)
				return null;
			String pluginId = feature.getFeaturePluginIdentifier();
			String pluginVersion = feature.getFeaturePluginVersion();
			if (pluginVersion == null) {
				pluginDescriptor = reg.getPluginDescriptor(pluginId);
			} else {
				PluginVersionIdentifier vid = new PluginVersionIdentifier(pluginVersion);	
				pluginDescriptor = reg.getPluginDescriptor(pluginId, vid);
				if (pluginDescriptor == null) {
					// try ignoring the version
					pluginDescriptor = reg.getPluginDescriptor(pluginId);
				}
			}
		}		
		return pluginDescriptor;
	}
	
	/**
	 * Returns the descriptor for the corresponding plug-in of this feature.
	 * 
	 * @return the plug-in descriptor or <code>null</code> if none found
	 */
	public IPluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
	}
	
	/**
	 * Returns the string value for the given key, or <code>null</code>.
	 * The string value is NLS if requested.
	 * 
	 * @return the string value for the given key, or <code>null</code>
	 */
	public String getString(String key, boolean doNls, Hashtable runtimeMappings) {
		if (ini == null)
			return null;
		String value = ini.getProperty(key);
		if (value != null && doNls)
			return getResourceString(value, runtimeMappings);
		return value;
	}
	/**
	 * Returns an image descriptor for the given key, or <code>null</code>.
	 * 
	 * @return an image descriptor for the given key, or <code>null</code>
	 */
	public ImageDescriptor getImage(String key) {
		if (ini == null)
			return null;
			
		URL url = getURL(key);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return null;
	}

	/**
	 * Returns a URL for the given key, or <code>null</code>.
	 * 
	 * @return a URL for the given key, or <code>null</code>
	 */
	public URL getURL(String key) {
		if (ini == null)
			return null;

		URL url = null;
		String fileName = ini.getProperty(key);
		if (fileName != null) {
			if (pluginDescriptor == null)
				return null;
			url = pluginDescriptor.find(new Path(fileName));
		}
		return url;
	}

	/**
	 * Returns the feature plugin label, or <code>null</code>.
	 * 
	 * @return the feature plugin lable, or <code>null</code> if none.
	 */
	public String getFeaturePluginLabel() {
		if (pluginDescriptor == null)
			return null;
		else
			return pluginDescriptor.getLabel();
	}
	
	/**
	 * Returns the provider name for this feature, or <code>null</code>.
	 * 
	 * @return the provider name for this feature, or <code>null</code>
	 */
	public String getProviderName() {
		if (pluginDescriptor == null)
			return null;
		else
			return pluginDescriptor.getProviderName();
	}
	
	/*
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
	 * @param runtimeMappings runtime mappings or <code>null</code>
	 * @return the resource string
	 */
	private String getResourceString(String value, Hashtable runtimeMappings) {
		
		if (value == null)
			return null;
		String s = value.trim();

		if (!s.startsWith(KEY_PREFIX))
			return s;

		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" "); //$NON-NLS-1$
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (properties == null)
			return dflt;

		String result = null;
		try {
			result = properties.getString(key.substring(1));
		} catch (MissingResourceException e) {
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

	/*
	 * Read the contents of the ini, properties, and mappings files.
	 */
	private IStatus load(URL iniURL, URL propertiesURL, URL mappingsURL) {

		InputStream is = null;
		try {
			is = iniURL.openStream();
			ini = new Properties();
			ini.load(is);
		} catch (IOException e) {
			ini = null;
			String message = WorkbenchMessages.format("IniFileReader.ReadIniError", new Object[] {iniURL}); //$NON-NLS-1$
			return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}

		if (propertiesURL != null) {
			is = null;
			try {
				is = propertiesURL.openStream();
				properties = new PropertyResourceBundle(is);
			} catch (IOException e) {
				properties = null;
				String message = WorkbenchMessages.format("IniFileReader.ReadPropError", new Object[] {propertiesURL}); //$NON-NLS-1$
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}
		}

		PropertyResourceBundle bundle = null;
		if (mappingsURL != null) {
			is = null;
			try {
				is = mappingsURL.openStream();
				bundle = new PropertyResourceBundle(is);
			} catch (IOException e) {
				bundle = null;
				String message = WorkbenchMessages.format("IniFileReader.ReadMapError", new Object[] {mappingsURL}); //$NON-NLS-1$
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}
		}

		ArrayList mappingsList = new ArrayList();
		if (bundle != null) {
			boolean found = true;
			int i = 0;
			while (found) {
				try {
					mappingsList.add(bundle.getString(Integer.toString(i)));
				} catch (MissingResourceException e) {
					found = false;
				}
				i++;
			}
		}
		mappings = (String[])mappingsList.toArray(new String[mappingsList.size()]);
		
		return OK_STATUS;
	}
}
