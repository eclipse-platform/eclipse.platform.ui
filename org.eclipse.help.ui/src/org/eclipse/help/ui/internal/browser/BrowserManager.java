/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.ui.browser.*;

/**
 * Creates browser by delegating
 * to appropriate browser adapter
 */
public class BrowserManager {
	public static final String DEFAULT_BROWSER_ID_KEY = "default_browser";
	private static BrowserManager instance;
	private BrowserDescriptor defaultBrowserDesc;
	private BrowserDescriptor[] browsersDescriptors;
	private Collection browsers = new ArrayList();
	/**
	 * Private Constructor
	 */
	private BrowserManager() {
		// Find all available browsers
		browsersDescriptors = createBrowserDescriptors();
		// 1. set default browser from preferences
		String defBrowserID =
			WorkbenchHelpPlugin.getDefault().getPluginPreferences().getString(
				DEFAULT_BROWSER_ID_KEY);
		if (defBrowserID != null && (!"".equals(defBrowserID)))
			setDefaultBrowserID(defBrowserID);
		if (defaultBrowserDesc == null) {
			// No default browser in properties!
			// Set default browser to prefered implementation
			if (System.getProperty("os.name").startsWith("Win")) {
				setDefaultBrowserID("org.eclipse.help.ui.iexplorer");
			} else if (System.getProperty("os.name").startsWith("Linux")) {
				setDefaultBrowserID("org.eclipse.help.ui.mozillaLinux");
			} else if (System.getProperty("os.name").startsWith("SunOS")) {
				setDefaultBrowserID("org.eclipse.help.ui.netscapeSolaris");
			} else if (System.getProperty("os.name").startsWith("AIX")) {
				setDefaultBrowserID("org.eclipse.help.ui.netscapeAIX");
			} else if (System.getProperty("os.name").toLowerCase().startsWith("hp")) {
				setDefaultBrowserID("org.eclipse.help.ui.netscapeAIX");
			} else {
				setDefaultBrowserID("org.eclipse.help.ui.mozillaLinux");
			}
		}
		if (defaultBrowserDesc == null) {
			// No default browser in properties!
			// Set default browser to one of the available
			if (browsersDescriptors.length > 0)
				defaultBrowserDesc = browsersDescriptors[0];
		}
		if (defaultBrowserDesc == null) {
			// If no browsers at all, use the Null Browser Adapter
			defaultBrowserDesc =
				new BrowserDescriptor("", "Null Browser", new IBrowserFactory() {
				public boolean isAvailable() {
					return true;
				}
				public IBrowser createBrowser() {
					return new IBrowser() {
						public void close() {
						}
						public void displayURL(String url) {
							Logger.logError(WorkbenchResources.getString("no_browsers", url), null);
							ErrorUtil.displayErrorDialog(WorkbenchResources.getString("no_browsers", url));
						}
						public boolean isCloseSupported() {
							return false;
						}
						public boolean isSetLocationSupported() {
							return false;
						}
						public boolean isSetSizeSupported() {
							return false;
						}
						public void setLocation(int width, int height) {
						}
						public void setSize(int x, int y) {
						}
					};
				}
			});
		}
	}
	/**
	 * Obtains singleton instance.
	 */
	public static BrowserManager getInstance() {
		if (instance == null)
			instance = new BrowserManager();
		return instance;
	}
	/**
	 * Creates all adapters, and returns
	 * available ones.
	 */
	private BrowserDescriptor[] createBrowserDescriptors() {
		if (this.browsersDescriptors != null)
			return this.browsersDescriptors;
		Collection bDescriptors = new ArrayList();
		IConfigurationElement configElements[] =
			Platform.getPluginRegistry().getConfigurationElementsFor(
				"org.eclipse.help.ui",
				"browser");
		for (int i = 0; i < configElements.length; i++) {
			if (!configElements[i].getName().equals("browser"))
				continue;
			String id = configElements[i].getAttribute("id");
			if (id == null)
				continue;
			String label = configElements[i].getAttribute("name");
			if (label == null)
				continue;
			try {
				Object adapter = configElements[i].createExecutableExtension("factoryclass");
				if (!(adapter instanceof IBrowserFactory))
					continue;
				if (((IBrowserFactory) adapter).isAvailable()) {
					bDescriptors.add(new BrowserDescriptor(id, label, (IBrowserFactory) adapter));
				}
			} catch (CoreException ce) {
			}
		}
		this.browsersDescriptors =
			(BrowserDescriptor[]) bDescriptors.toArray(
				new BrowserDescriptor[bDescriptors.size()]);
		return this.browsersDescriptors;
	}
	/**
	 * Obtains browsers descriptors.
	 */
	protected BrowserDescriptor[] getBrowserDescriptors() {
		return this.browsersDescriptors;
	}
	/**
	 * Gets the defaultBrowserID.
	 * @return Returns a String or null if not set
	 */
	protected String getDefaultBrowserID() {
		if (defaultBrowserDesc == null)
			return null;
		return defaultBrowserDesc.getID();
	}
	/**
	 * Sets the defaultBrowserID.
	 * If browser of given ID does not exists,
	 * the method does nothing
	 * @param defaultBrowserID The defaultAdapterID to set
	 */
	protected void setDefaultBrowserID(String defaultAdapterID) {
		for (int i = 0; i < browsersDescriptors.length; i++) {
			if (browsersDescriptors[i].getID().equals(defaultAdapterID)) {
				defaultBrowserDesc = browsersDescriptors[i];
				return;
			}
		}
	}
	/**
	 * Creates web browser
	 */
	public IBrowser createBrowser() {
		IBrowser browser = defaultBrowserDesc.getFactory().createBrowser();
		browsers.add(browser);
		return browser;
	}
	/**
	 * Closes all browsers created
	 */
	public void closeAll() {
		for (Iterator it = browsers.iterator(); it.hasNext();) {
			IBrowser browser = (IBrowser) it.next();
			browser.close();
		}
	}
}