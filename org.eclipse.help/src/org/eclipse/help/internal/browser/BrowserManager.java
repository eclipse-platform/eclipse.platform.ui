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
package org.eclipse.help.internal.browser;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * Creates browser by delegating
 * to appropriate browser adapter
 */
public class BrowserManager {
	public static final String DEFAULT_BROWSER_ID_KEY = "default_browser";
	private static BrowserManager instance;
	private boolean initialized = false;
	private BrowserDescriptor currentBrowserDesc;
	private BrowserDescriptor defaultBrowserDesc;
	private BrowserDescriptor[] browsersDescriptors;
	private Collection browsers = new ArrayList();
	/**
	 * Private Constructor
	 */
	private BrowserManager() {
	}
	/**
	 * Initialize
	 */
	private void init() {
		initialized = true;

		// Find all available browsers
		browsersDescriptors = createBrowserDescriptors();

		// 1. set default browser from preferences
		String defBrowserID =
			HelpPlugin.getDefault().getPluginPreferences().getDefaultString(
				DEFAULT_BROWSER_ID_KEY);
		if (defBrowserID != null && (!"".equals(defBrowserID))) {
			setDefaultBrowserID(defBrowserID);
		}

		if (defaultBrowserDesc == null) {
			// No default browser in properties!
			// Set default browser to prefered implementation
			if (System.getProperty("os.name").startsWith("Win")) {
				setDefaultBrowserID("org.eclipse.help.ui.iexplorer");
				if (defaultBrowserDesc == null) {
					setDefaultBrowserID("org.eclipse.help.ui.systembrowser");
				}
				if (defaultBrowserDesc == null) {
					setDefaultBrowserID("org.eclipse.help.custombrowser");
				}

			} else if (System.getProperty("os.name").startsWith("Linux")) {
				setDefaultBrowserID("org.eclipse.help.mozillaLinux");
				if (defaultBrowserDesc == null) {
					setDefaultBrowserID("org.eclipse.help.netscapeLinux");
				}

			} else if (System.getProperty("os.name").startsWith("SunOS")) {
				setDefaultBrowserID("org.eclipse.help.netscapeSolaris");
			} else if (System.getProperty("os.name").startsWith("AIX")) {
				setDefaultBrowserID("org.eclipse.help.netscapeAIX");
			} else if (
				System.getProperty("os.name").toLowerCase().startsWith("hp")) {
				setDefaultBrowserID("org.eclipse.help.mozillaHPUX");
				if (defaultBrowserDesc == null) {
					setDefaultBrowserID("org.eclipse.help.netscapeHPUX");
				}
			} else {
				setDefaultBrowserID("org.eclipse.help.mozillaLinux");
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
				new BrowserDescriptor(
					"",
					"Null Browser",
					new IBrowserFactory() {
				public boolean isAvailable() {
					return true;
				}
				public IBrowser createBrowser() {
					return new IBrowser() {
						public void close() {
						}
						public void displayURL(String url) {
							String msg =
								Resources.getString("no_browsers", url);
							HelpPlugin.logError(msg, null);
							HelpSystem.getDefaultErrorUtil().displayError(msg);
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

		// initialize current browser
		String curBrowserID =
			HelpPlugin.getDefault().getPluginPreferences().getString(
				DEFAULT_BROWSER_ID_KEY);
		if (curBrowserID != null && (!"".equals(curBrowserID))) {
			setCurrentBrowserID(curBrowserID);
		} else {
			setCurrentBrowserID(getDefaultBrowserID());
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
				"org.eclipse.help",
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
				Object adapter =
					configElements[i].createExecutableExtension("factoryclass");
				if (!(adapter instanceof IBrowserFactory))
					continue;
				if (((IBrowserFactory) adapter).isAvailable()) {
					bDescriptors.add(
						new BrowserDescriptor(
							id,
							label,
							(IBrowserFactory) adapter));
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
	public BrowserDescriptor[] getBrowserDescriptors() {
		if (!initialized) {
			init();
		}
		return this.browsersDescriptors;
	}
	/**
	 * Gets the currentBrowserID.
	 * @return Returns a String or null if not set
	 */
	public String getCurrentBrowserID() {
		if (!initialized) {
			init();
		}
		if (currentBrowserDesc == null)
			return null;
		return currentBrowserDesc.getID();
	}
	/**
	 * Gets the currentBrowserID.
	 * @return Returns a String or null if not set
	 */
	public String getDefaultBrowserID() {
		if (!initialized) {
			init();
		}
		if (defaultBrowserDesc == null)
			return null;
		return defaultBrowserDesc.getID();
	}
	/**
	 * Sets the currentBrowserID.
	 * If browser of given ID does not exists,
	 * the method does nothing
	 * @param currentAdapterrID The ID of the adapter to to set as current
	 */
	public void setCurrentBrowserID(String currentAdapterID) {
		if (!initialized) {
			init();
		}
		for (int i = 0; i < browsersDescriptors.length; i++) {
			if (browsersDescriptors[i].getID().equals(currentAdapterID)) {
				currentBrowserDesc = browsersDescriptors[i];
				return;
			}
		}
	}
	/**
	 * Sets the defaultBrowserID.
	 * If browser of given ID does not exists,
	 * the method does nothing
	 * @param currentAdapterrID The ID of the adapter to to set as current
	 */
	private void setDefaultBrowserID(String defaultAdapterID) {
		if (!initialized) {
			init();
		}
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
		if (!initialized) {
			init();
		}
		return new CurrentBrowser(
			createBrowserAdapter(),
			getCurrentBrowserID());
	}
	/**
	 * Creates web browser
	 */
	private IBrowser createBrowserAdapter() {
		IBrowser browser = currentBrowserDesc.getFactory().createBrowser();
		browsers.add(browser);
		return browser;
	}
	/**
	 * Closes all browsers created
	 */
	public void closeAll() {
		if (!initialized) {
			// nothing to do, do not initialize
			return;
		}
		for (Iterator it = browsers.iterator(); it.hasNext();) {
			IBrowser browser = (IBrowser) it.next();
			browser.close();
		}
	}
}
