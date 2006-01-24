/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;
import org.osgi.framework.Bundle;

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

	protected BookmarkManager bookmarkManager;

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

	/**
	 * Used to obtain Bookmark Manager
	 * 
	 * @return instance of BookmarkManager
	 */
	public static synchronized BookmarkManager getBookmarkManager() {
		if (getInstance().bookmarkManager == null) {
			getInstance().bookmarkManager = new BookmarkManager();
		}
		return getInstance().bookmarkManager;
	}

	/**
	 * Allows Help UI to plug-in a soft adapter that delegates all the work to
	 * the workbench browser support.
	 * 
	 * @since 3.1
	 * @param browser
	 *            the instance to use when external browser is needed
	 */

	public synchronized void setBrowserInstance(IBrowser browser) {
		this.browser = browser;
	}

	public static synchronized IBrowser getHelpBrowser(boolean forceExternal) {
		if (!forceExternal
				&& !BrowserManager.getInstance().isAlwaysUseExternal()) {
			if (getInstance().internalBrowser == null)
				getInstance().internalBrowser = BrowserManager.getInstance()
						.createBrowser(false);
			return getInstance().internalBrowser;
		}
		if (getInstance().browser == null)
			getInstance().browser = BrowserManager.getInstance()
					.createBrowser(true);
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

		if (getInstance().bookmarkManager != null) {
			getInstance().bookmarkManager.close();
			getInstance().bookmarkManager = null;
		}

		if (getInstance().searchManager != null) {
			getInstance().searchManager.close();
			getInstance().searchManager = null;
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
						HelpBaseResources.HelpWebappNotStarted);
				return false;
			}
			getInstance().webappRunning = true;

		}
		return getInstance().webappRunning;
	}

	public static URL resolve(String href, boolean documentOnly) {
		String url = null;
		if (href == null || href.indexOf("://") != -1) //$NON-NLS-1$
			url = href;
		else {
			BaseHelpSystem.ensureWebappRunning();
			String base = getBase(documentOnly);
			if (href.startsWith("/")) //$NON-NLS-1$
				url = base + href;
			else
				url = base + "/" + href; //$NON-NLS-1$
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static URL resolve(String href, String servlet) {
		String url = null;
		if (href == null || href.indexOf("://") != -1) //$NON-NLS-1$
			url = href;
		else {
			BaseHelpSystem.ensureWebappRunning();
			String base = getBase(servlet);
			if (href.startsWith("/")) //$NON-NLS-1$
				url = base + href;
			else
				url = base + "/" + href; //$NON-NLS-1$
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static String unresolve(URL url) {
		return unresolve(url.toString());
	}

	public static String unresolve(String href) {
		String[] baseVariants = { getBase("/help/topic"), //$NON-NLS-1$
				getBase("/help/nftopic"),  //$NON-NLS-1$
				getBase("/help/ntopic") }; //$NON-NLS-1$
		for (int i = 0; i < baseVariants.length; i++) {
			if (href.startsWith(baseVariants[i]))
				return href.substring(baseVariants[i].length());
		}
		return href;
	}

	private static String getBase(boolean documentOnly) {
		String servlet = documentOnly ? "/help/nftopic" : "/help/topic";//$NON-NLS-1$ //$NON-NLS-2$
		return getBase(servlet);
	}

	private static String getBase(String servlet) {
		return "http://" //$NON-NLS-1$
				+ WebappManager.getHost() + ":" //$NON-NLS-1$
				+ WebappManager.getPort() + servlet;
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
		HelpSystem.setShared(mode == MODE_INFOCENTER);
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
						return elements[i].getNamespace();
					}
				}
				// if reached this point, then then pick the first (default)
				// webapp
				if (elements.length > 0)
					return elements[0].getNamespace();
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
		String orientation = System.getProperty("eclipse.orientation"); //$NON-NLS-1$
		if ("rtl".equals(orientation)) { //$NON-NLS-1$
			return true;
		} else if ("ltr".equals(orientation)) { //$NON-NLS-1$
			return false;
		}
		// from command line
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if ("-dir".equalsIgnoreCase(args[i])) { //$NON-NLS-1$
				if ((i + 1) < args.length
						&& "rtl".equalsIgnoreCase(args[i + 1])) { //$NON-NLS-1$
					return true;
				}
				return false;
			}
		}

		// Check if the user property is set. If not do not
		// rely on the vm.
		if (System.getProperty("osgi.nl.user") == null) //$NON-NLS-1$
			return false;

		// guess from default locale
		String locale = Platform.getNL();
		if (locale == null) {
			locale = Locale.getDefault().toString();
		}
		if (locale.startsWith("ar") || locale.startsWith("fa") //$NON-NLS-1$//$NON-NLS-2$
				|| locale.startsWith("he") || locale.startsWith("iw") //$NON-NLS-1$//$NON-NLS-2$
				|| locale.startsWith("ur")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	public static boolean isRTL() {
		return getInstance().rtl;
	}

	public static void runLiveHelp(String pluginID, String className, String arg) {	
		Bundle bundle = Platform.getBundle(pluginID);
		if (bundle == null) {
			return;
		}
	
		try {
			Class c = bundle.loadClass(className);
			Object o = c.newInstance();
			//Runnable runnable = null;
			if (o != null && o instanceof ILiveHelpAction) {
				ILiveHelpAction helpExt = (ILiveHelpAction) o;
				if (arg != null)
					helpExt.setInitializationString(arg);
				Thread runnableLiveHelp = new Thread(helpExt);
				runnableLiveHelp.setDaemon(true);
				runnableLiveHelp.start();
			}
		} catch (ThreadDeath td) {
			throw td;
		} catch (Exception e) {
		}
	}
}
