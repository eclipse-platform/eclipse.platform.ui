package org.eclipse.ui.internal;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IInstallInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.misc.PluginFileFinder;

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

	private static final String KEY_PREFIX = "%";
	private static final String KEY_DOUBLE_PREFIX = "%%";
	
	protected ConfigurationInfo(String ini, String properties) {
		iniFilename = ini;
		propertiesFilename = properties;
	}

	/**
	 * R1.0 platform.ini handling using "main" plugin and fragments for NL
	 */
	public void readINIFile() throws CoreException {
		// determine the identifier of the "dominant" application 
		IInstallInfo ii = BootLoader.getInstallationInfo();
		String configName = ii.getApplicationConfigurationIdentifier();
		if (configName == null)
			reportINIFailure(null, "Unknown configuration identifier"); //$NON-NLS-1$

		// attempt to locate its corresponding "main" plugin
		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null)
			reportINIFailure(null, "Plugin registry is null"); //$NON-NLS-1$
		int index = configName.lastIndexOf("_");
		if (index == -1)
			this.desc = reg.getPluginDescriptor(configName);
		else {
			String mainPluginName = configName.substring(0, index);
			PluginVersionIdentifier mainPluginVersion = null;
			try {
				mainPluginVersion =
					new PluginVersionIdentifier(configName.substring(index + 1));
			} catch (Exception e) {
				reportINIFailure(e, "Unknown plugin version"); //$NON-NLS-1$
			}
			this.desc = reg.getPluginDescriptor(mainPluginName, mainPluginVersion);
		}
		if (this.desc == null)
			reportINIFailure(null, "Plugin descriptor is null"); //$NON-NLS-1$
		this.baseURL = desc.getInstallURL();

		// load the platform.ini and platform.properties file	
		URL iniURL = PluginFileFinder.getResource(this.desc, iniFilename);
		URL propertiesURL =
			PluginFileFinder.getResource(this.desc, propertiesFilename);
		if (iniURL == null)
			reportINIFailure(null, "Plugin file not found"); //$NON-NLS-1$
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
	 *     getResourceString("Hello World") returns "Hello World"</li>
	 *     getResourceString("%name") returns "Project Name"</li>
	 *     getResourceString("%name Hello World") returns "Project Name"</li>
	 *     getResourceString("%abcd Hello World") returns "Hello World"</li>
	 *     getResourceString("%abcd") returns "%abcd"</li>
	 *     getResourceString("%%name") returns "%name"</li>
	 * </pre>
	 * </p>
	 *
	 * @param value the value
	 * @param b the resource bundle or <code>null</code>
	 * @return the resource string
	 */
	protected String getResourceString(String value, ResourceBundle b) {

		String s = value.trim();

		if (!s.startsWith(KEY_PREFIX))
			return s;

		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" ");
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (b == null)
			return dflt;

		try {
			return b.getString(key.substring(1));
		} catch (MissingResourceException e) {
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
	protected void reportINIFailure(Exception e, String message)
		throws CoreException {
		throw new CoreException(
			new Status(
				IStatus.ERROR,
				WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				0,
				message,
				e));
	}
}