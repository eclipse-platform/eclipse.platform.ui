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
package org.eclipse.help.internal.base;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;

/**
 * The actual implementation of the help system plugin.
 */
public final class BaseHelpSystem {
	protected static final BaseHelpSystem instance = new BaseHelpSystem();

	private final static String WEBAPP_EXTENSION_ID = HelpBasePlugin.PLUGIN_ID+".webapp";
	private static final String WEBAPP_DEFAULT_ATTRIBUTE = "default";

	private static final String HELP_SUPPORT_EXTENSION_ID =
		HelpPlugin.PLUGIN_ID+".support";
	private static final String HELP_SUPPORT_CLASS_ATTRIBUTE = "class";

	public final static String BOOKMARKS = "bookmarks";
	public final static String WORKING_SETS = "workingSets";
	public final static String WORKING_SET = "workingSet";
	public final static int MODE_WORKBENCH = 0;
	public final static int MODE_INFOCENTER = 1;
	public final static int MODE_STANDALONE = 2;

	protected SearchManager searchManager;
	protected HashMap workingSetManagers;
	private int mode = MODE_WORKBENCH;
	private boolean webappStarted = false;
	private IErrorUtil defaultErrorMessenger;
	private IBrowser browser;
	private HelpDisplay helpDisplay = null;
	private boolean webappRunning = false;

	/**
	 * HelpSystem constructor comment.
	 */
	private BaseHelpSystem() {
		super();
	}
	public static BaseHelpSystem getInstance() {
		return instance;
	}
	/**
	 * Used to obtain Search Manager
	 * @return instance of SearchManager
	 */
	public static SearchManager getSearchManager() {
		if (getInstance().searchManager == null) {
			synchronized (BaseHelpSystem.class) {
				if (getInstance().searchManager == null) {
					getInstance().searchManager = new SearchManager();
				}
			}
		}
		return getInstance().searchManager;
	}
	/**
	 * Used to obtain Working Set Manager
	 * @return instance of WorkingSetManager
	 */
	public static WorkingSetManager getWorkingSetManager() {
		return getWorkingSetManager(BootLoader.getNL());
	}

	public static synchronized WorkingSetManager getWorkingSetManager(String locale) {
		if (getInstance().workingSetManagers == null) {
			getInstance().workingSetManagers = new HashMap();
		}
		WorkingSetManager wsmgr =
			(WorkingSetManager) getInstance().workingSetManagers.get(locale);
		if (wsmgr == null) {
			wsmgr = new WorkingSetManager(locale);
			getInstance().workingSetManagers.put(locale, wsmgr);
		}
		return wsmgr;
	}

	public static synchronized IBrowser getHelpBrowser() {
		if (getInstance().browser == null)
			getInstance().browser =
				BrowserManager.getInstance().createBrowser();
		return getInstance().browser;
	}

	public static synchronized HelpDisplay getHelpDisplay() {
		if (getInstance().helpDisplay == null)
			getInstance().helpDisplay = new HelpDisplay();
		return getInstance().helpDisplay;
	}
	/**
	 */
	public BaseHelpSystem newInstance() {
		return null;
	}

	/**
	 * Shuts down the Help System.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public static void shutdown() throws CoreException {
		if (HelpBasePlugin.DEBUG) {
			System.out.println("Help System is shutting down.");
		}
		if (getInstance().searchManager != null) {
			getInstance().searchManager.close();
		}
		// stop the web apps
		WebappManager.stop("help");
		if (getMode() != MODE_WORKBENCH)
			WebappManager.stop("helpControl");

		// close any browsers created
		BrowserManager.getInstance().closeAll();

		if (HelpBasePlugin.DEBUG) {
			System.out.println("Help System is shut down.");
		}
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public static void startup() {
		try {
			setDefaultErrorUtil(new IErrorUtil() {
				public void displayError(String msg) {
					System.out.println(msg);
				}

				public void displayError(String msg, Thread uiThread) {
					System.out.println(msg);
				}

			});
			HelpBasePlugin.getDefault().getPluginPreferences();
		} catch (Exception e) {
			HelpBasePlugin.getDefault().getLog().log(
				new Status(
					Status.ERROR,
					HelpBasePlugin
						.getDefault()
						.getDescriptor()
						.getUniqueIdentifier(),
					0,
					HelpBaseResources.getString("E005"),
					e));
		}
		if (HelpBasePlugin.DEBUG) {
			System.out.println("Help System started.");
		}
	}
	public static boolean ensureWebappRunning() {
		if (!getInstance().webappStarted) {
			getInstance().webappStarted = true;

			String webappPlugin = getWebappPlugin();

			if (getMode() != MODE_WORKBENCH) {
				// start the help control web app
				try {
					WebappManager.start(
						"helpControl",
						webappPlugin,
						Path.EMPTY);
				} catch (CoreException e) {
					HelpBasePlugin.logError(HelpBaseResources.getString("E043"), e);
					return false;
				}
			}
			// start the help web app
			try {
				WebappManager.start("help", webappPlugin, Path.EMPTY);
			} catch (CoreException e) {
				HelpBasePlugin.logError(HelpBaseResources.getString("E042"), e);
				return false;
			}
			getInstance().webappRunning = true;

		}
		return getInstance().webappRunning;
	}

	/**
	 * Returns the mode.
	 * @return int
	 */
	public static int getMode() {
		return getInstance().mode;
	}

	/**
	 * Sets the mode.
	 * @param mode The mode to set
	 */
	public static void setMode(int mode) {
		getInstance().mode = mode;
	}

	/**
	 * Sets the error messenger
	 */
	public static void setDefaultErrorUtil(IErrorUtil em) {
		getInstance().defaultErrorMessenger = em;
	}

	/**
	 * Returns the default error messenger. When no UI is present, all
	 * errors are sent to System.out.
	 * @return IErrorMessenger
	 */
	public static IErrorUtil getDefaultErrorUtil() {
		return getInstance().defaultErrorMessenger;
	}

	/**
	 * Returns the plugin id that defines the help webapp
	 */
	private static String getWebappPlugin() {

		// get the webapp extension from the system plugin registry
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint point =
			pluginRegistry.getExtensionPoint(WEBAPP_EXTENSION_ID);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0) {
				// We need to pick up the non-default configuration
				IConfigurationElement[] elements =
					extensions[0].getConfigurationElements();

				for (int i = 0; i < elements.length; i++) {
					String defaultValue =
						elements[i].getAttribute(WEBAPP_DEFAULT_ATTRIBUTE);
					if (defaultValue == null || defaultValue.equals("false")) {
						return elements[i]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					}
				}
				// if reached this point, then then pick the first (default) webapp
				if (elements.length > 0)
					return elements[0]
						.getDeclaringExtension()
						.getDeclaringPluginDescriptor()
						.getUniqueIdentifier();
			}
		}

		// if all fails
		return "org.eclipse.help.webapp";
	}

	/**
	 * Obtains Name of the Eclipse product
	 * @return String
	 */
	public static String getProductName() {
		IPlatformConfiguration c = BootLoader.getCurrentPlatformConfiguration();
		String primaryFeatureId = c.getPrimaryFeatureIdentifier();
		IPluginDescriptor pfd =
			Platform.getPluginRegistry().getPluginDescriptor(primaryFeatureId);
		if (pfd == null)
			return ""; // no primary feature installed
		return pfd.getLabel();
	}

}
