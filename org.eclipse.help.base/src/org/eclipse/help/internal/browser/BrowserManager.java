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
import org.eclipse.help.internal.base.*;

/**
 * Creates browser by delegating to appropriate browser adapter
 */
public class BrowserManager {
	public static final String ALWAYS_EXTERNAL_BROWSER_KEY = "always_external_browser";
	public static final String DEFAULT_BROWSER_ID_KEY = "default_browser";
	private static BrowserManager instance;
	private boolean initialized = false;
	private BrowserDescriptor currentBrowserDesc;
	private BrowserDescriptor defaultBrowserDesc;
	private BrowserDescriptor[] browsersDescriptors;
	private BrowserDescriptor internalBrowserDesc;
	private Collection browsers = new ArrayList();
	private boolean alwaysUseExternal = false;
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
			HelpBasePlugin
				.getDefault()
				.getPluginPreferences()
				.getDefaultString(
				DEFAULT_BROWSER_ID_KEY);
		if (defBrowserID != null && (!"".equals(defBrowserID))) {
			setDefaultBrowserID(defBrowserID);
		}

		// 2. set default browser to embedded
		if (defaultBrowserDesc == null) {
			setDefaultBrowserID("org.eclipse.help.ui.embeddedbrowser");
		}
		
		// 3. set default browser to help implementation of system specific browser
		String os = System.getProperty("os.name").toLowerCase();
		if (defaultBrowserDesc == null) {
			if (os.startsWith("win")) {
				setDefaultBrowserID("org.eclipse.help.ui.systembrowser");
			} else if (os.startsWith("linux") || os.startsWith("aix")
					|| os.startsWith("hp") || os.startsWith("sunos")) {
				setDefaultBrowserID("org.eclipse.help.base.mozilla");
				if (defaultBrowserDesc == null) {
					setDefaultBrowserID("org.eclipse.help.base.netscape");
				}
			} else if (os.equals("Mac OS X")) {
				setDefaultBrowserID("org.eclipse.help.base.defaultBrowserMacOSX");
			} else {
				setDefaultBrowserID("org.eclipse.help.base.mozillaLinux");
			}
		}

		// 4. set browser to one of externally contributed
		if (defaultBrowserDesc == null) {
			for (int i = 0; i < browsersDescriptors.length; i++) {
				if ("org.eclipse.help.base.custombrowser"
					.equals(browsersDescriptors[i].getID())) {
					defaultBrowserDesc = browsersDescriptors[i];
				}
			}
		}
		
		// 5. let user specify program
		if (defaultBrowserDesc == null) {
			setDefaultBrowserID("org.eclipse.help.base.custombrowser");
		}

		// 6. use null browser
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
								HelpBaseResources.getString("no_browsers", url);
							HelpBasePlugin.logError(msg, null);
							BaseHelpSystem.getDefaultErrorUtil().displayError(
								msg);
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
			HelpBasePlugin.getDefault().getPluginPreferences().getString(
				DEFAULT_BROWSER_ID_KEY);
		if (curBrowserID != null && (!"".equals(curBrowserID))) {
			setCurrentBrowserID(curBrowserID);
			// may fail if such browser does not exist
		}
		if (currentBrowserDesc == null) {
			setCurrentBrowserID(getDefaultBrowserID());
		}
		setAlwaysUseExternal(HelpBasePlugin
				.getDefault()
				.getPluginPreferences()
				.getBoolean(ALWAYS_EXTERNAL_BROWSER_KEY));

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
	 * Creates all adapters, and returns available ones.
	 */
	private BrowserDescriptor[] createBrowserDescriptors() {
		if (this.browsersDescriptors != null)
			return this.browsersDescriptors;
		Collection bDescriptors = new ArrayList();
		IConfigurationElement configElements[] =
			Platform.getPluginRegistry().getConfigurationElementsFor(
				HelpBasePlugin.PLUGIN_ID,
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
					BrowserDescriptor descriptor = new BrowserDescriptor(
							id,
							label,
							(IBrowserFactory) adapter);
					if(descriptor.isExternal()){
					bDescriptors.add(
						descriptor);
					}else{
						internalBrowserDesc = descriptor;
					}
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
	 * 
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
	 * 
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
	 * Sets the currentBrowserID. If browser of given ID does not exists, the
	 * method does nothing
	 * 
	 * @param currentAdapterrID
	 *            The ID of the adapter to to set as current
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
	 * Sets the defaultBrowserID. If browser of given ID does not exists, the
	 * method does nothing
	 * 
	 * @param currentAdapterrID
	 *            The ID of the adapter to to set as current
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
	public IBrowser createBrowser(boolean forceExternal) {
		if (!initialized) {
			init();
		}
		forceExternal = forceExternal || alwaysUseExternal;
		return createBrowserAdapter(forceExternal);
		// TODO fix and use CurrentBrowser
		//	return new CurrentBrowser(createBrowserAdapter(forceExternal), getCurrentBrowserID());
	}
	/**
	 * Creates web browser
	 */
	// TODO deprecate createBrowser(void)
	public IBrowser createBrowser() {
		return createBrowser(true);
	}
	/**
	 * Creates web browser
	 */
	private IBrowser createBrowserAdapter(boolean forceExternal) {
		if (!initialized) {
			init();
		}
		IBrowser browser = null;
		if(! forceExternal && internalBrowserDesc != null){
			browser = internalBrowserDesc.getFactory().createBrowser();
		}else{
			browser = currentBrowserDesc.getFactory().createBrowser();
		}
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
	public boolean isEmbeddedBrowserPresent(){
		if (!initialized) {
			init();
		}
		return internalBrowserDesc != null;
	}
	public void setAlwaysUseExternal(boolean alwaysExternal){
		if (!initialized) {
			init();
		}
		
		alwaysUseExternal = alwaysExternal || !isEmbeddedBrowserPresent();
	}
	public boolean isAlwaysUseExternal(){
		if (!initialized) {
			init();
		}
		if(!isEmbeddedBrowserPresent()){
			return true;
		}
		return alwaysUseExternal;
	}
}
