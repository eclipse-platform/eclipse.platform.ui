/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;

/**
 * Base Help System.
 */
public final class BaseHelpSystem {
	protected static final BaseHelpSystem instance = new BaseHelpSystem();

	private final static String WEBAPP_EXTENSION_ID = HelpBasePlugin.PLUGIN_ID
			+ ".webapp"; //$NON-NLS-1$

	private static final String WEBAPP_DEFAULT_ATTRIBUTE = "default"; //$NON-NLS-1$

	public final static String BOOKMARKS = "bookmarks"; //$NON-NLS-1$

	public final static String WORKING_SETS = "workingSets"; //$NON-NLS-1$

	public final static String WORKING_SET = "workingSet"; //$NON-NLS-1$

	public final static int MODE_WORKBENCH = 0;

	public final static int MODE_INFOCENTER = 1;

	public final static int MODE_STANDALONE = 2;

	protected SearchManager searchManager;

	protected WorkingSetManager workingSetManager;

	private int mode = MODE_WORKBENCH;

	private boolean webappStarted = false;

	private IErrorUtil defaultErrorMessenger;

	private IBrowser browser;

	private IBrowser internalBrowser;

	private HelpDisplay helpDisplay = null;

	private boolean webappRunning = false;
	
	private boolean rtl = false;

	/**
	 * Constructor.
	 */
	private BaseHelpSystem() {
		super();
		rtl = initializeRTL();
	}

	public static BaseHelpSystem getInstance() {
		return instance;
	}

	/**
	 * Used to obtain Search Manager
	 * 
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
	 * 
	 * @return instance of WorkingSetManager
	 */
	public static synchronized WorkingSetManager getWorkingSetManager() {
		if (getInstance().workingSetManager == null) {
			getInstance().workingSetManager = new WorkingSetManager();
		}
		return getInstance().workingSetManager;
	}

	public static synchronized IBrowser getHelpBrowser(boolean forceExternal) {
		if (!forceExternal) {
			if (getInstance().internalBrowser == null)
				getInstance().internalBrowser = BrowserManager.getInstance()
						.createBrowser(false);
			return getInstance().internalBrowser;
		} else {
			if (getInstance().browser == null)
				getInstance().browser = BrowserManager.getInstance()
						.createBrowser(true);
			return getInstance().browser;
		}
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
	 * Shuts down the BaseHelpSystem.
	 * 
	 * @exception CoreException
	 *                if this method fails to shut down this plug-in
	 */
	public static void shutdown() throws CoreException {
		if (HelpBasePlugin.DEBUG) {
			System.out.println("Base Help System is shutting down."); //$NON-NLS-1$
		}
		// close any browsers created
		// BrowserManager.getInstance().closeAll();

		if (getInstance().searchManager != null) {
			getInstance().searchManager.close();
		}
		if (getInstance().webappStarted) {
			// stop the web apps
			WebappManager.stop("help"); //$NON-NLS-1$
			if (getMode() != MODE_WORKBENCH)
				WebappManager.stop("helpControl"); //$NON-NLS-1$
		}

		if (HelpBasePlugin.DEBUG) {
			System.out.println("Help System is shut down."); //$NON-NLS-1$
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
					new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, 0,
							"Error in launching help.", //$NON-NLS-1$
							e));
		}
		if (HelpBasePlugin.DEBUG) {
			System.out.println("Base Help System started."); //$NON-NLS-1$
		}
	}

	public static boolean ensureWebappRunning() {
		if (!getInstance().webappStarted) {
			getInstance().webappStarted = true;

			String webappPlugin = getWebappPlugin();

			if (getMode() != MODE_WORKBENCH) {
				// start the help control web app
				try {
					WebappManager.start("helpControl", //$NON-NLS-1$
							webappPlugin, Path.EMPTY);
				} catch (CoreException e) {
					HelpBasePlugin
							.logError(
									"Stand-alone help control web application failed to run.", //$NON-NLS-1$
									e);
					return false;
				}
			}
			// start the help web app
			try {
				WebappManager.start("help", webappPlugin, Path.EMPTY); //$NON-NLS-1$
			} catch (CoreException e) {
				HelpBasePlugin
						.logError(
								"The embedded application server could not run help web application.", e); //$NON-NLS-1$
				BaseHelpSystem.getDefaultErrorUtil().displayError(
						HelpBaseResources.getString("HelpWebappNotStarted")); //$NON-NLS-1$
				return false;
			}
			getInstance().webappRunning = true;

		}
		return getInstance().webappRunning;
	}

	/**
	 * Returns the mode.
	 * 
	 * @return int
	 */
	public static int getMode() {
		return getInstance().mode;
	}

	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 *            The mode to set
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
	 * Returns the default error messenger. When no UI is present, all errors
	 * are sent to System.out.
	 * 
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
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(WEBAPP_EXTENSION_ID);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0) {
				// We need to pick up the non-default configuration
				IConfigurationElement[] elements = extensions[0]
						.getConfigurationElements();

				for (int i = 0; i < elements.length; i++) {
					String defaultValue = elements[i]
							.getAttribute(WEBAPP_DEFAULT_ATTRIBUTE);
					if (defaultValue == null || defaultValue.equals("false")) { //$NON-NLS-1$
						return elements[i].getDeclaringExtension()
								.getNamespace();
					}
				}
				// if reached this point, then then pick the first (default)
				// webapp
				if (elements.length > 0)
					return elements[0].getDeclaringExtension().getNamespace();
			}
		}

		// if all fails
		return "org.eclipse.help.webapp"; //$NON-NLS-1$
	}

	/**
	 * Obtains name of the Eclipse product
	 * 
	 * @return String
	 */
	public static String getProductName() {
		IProduct product = Platform.getProduct();
		if (product == null) {
			return ""; //$NON-NLS-1$
		}
		String name = product.getName();
		return name == null ? "" : name; //$NON-NLS-1$
	}
	private static boolean initializeRTL() {
		// from property
		String orientation = System.getProperty("eclipse.orientation");
		if ("rtl".equals(orientation)) {
			return true;
		} else if ("ltr".equals(orientation)) {
			return false;
		}
		// from command line
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if ("-dir".equalsIgnoreCase(args[i])) { //$NON-NLS-1$
				if ((i + 1) < args.length
						&& "rtl".equalsIgnoreCase(args[i + 1])) { //$NON-NLS-1$
					return true;
				} else {
					return false;
				}
			}
		}
		// guess from default locale
		String locale = Platform.getNL();
		if (locale == null) {
			locale = Locale.getDefault().toString();
		}
		if (locale.startsWith("ar") || locale.startsWith("fa")
				|| locale.startsWith("he") || locale.startsWith("iw")) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isRTL() {
		return getInstance().rtl;
	}

}