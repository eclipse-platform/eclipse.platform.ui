package org.eclipse.help.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.navigation.*;
import org.eclipse.help.internal.server.HelpServer;
import org.eclipse.help.internal.contributors.xml.*;
import org.eclipse.help.internal.search.ISearchEngine;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;

/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();
	protected ContributionManager contributionManager;
	protected HelpNavigationManager navigationManager;
	protected ContextManager contextManager;
	protected ISearchEngine searchManager;
	protected Plugin plugin;

	int debug_level;
	private String browserPath;

	// constants
	private static final String SEARCH_ENGINE_EXTENSION_POINT =
		"org.eclipse.help.searchEngine";
	private static final String SEARCH_ENGINE_CONFIG = "config";
	private static final String SEARCH_ENGINE_CLASS = "class";

	// public constants (preferences)
	public static final int INSTALL_LOCAL = 0;
	public static final int INSTALL_CLIENT = 1;
	public static final int INSTALL_SERVER = 2;

	// Contants indicating level of logging
	public static final int LOG_ERROR = 0; // log errors
	public static final int LOG_WARNING = 1; // log errors and warnings
	public static final int LOG_DEBUG = 2;
	// log errors, warning, debug messages, and information messages

	// configuration settings
	int install = INSTALL_LOCAL;
	private String remoteServerPath;
	private URL remoteServerURL;

	private String localServerAddress;
	private String localServerPort;

	/**
	 * HelpSystem constructor comment.
	 */
	private HelpSystem() {
		super();
	}
	/**
	 * Obtains path to the HTML browser
	 * @return java.lang.String
	 */
	public static String getBrowserPath() {
		return getInstance().browserPath;
	}
	/**
	 * Used to obtain Print Manager
	 * returns an instance of HelpPrintManager
	 */
	public static ContextManager getContextManager() {
		if (getInstance().contextManager == null)
			getInstance().contextManager = new HelpContextManager();
		return getInstance().contextManager;
	}
	/**
	 * Used to obtain Contribution Manager
	 * @return instance of ContributionManager
	 */
	public static ContributionManager getContributionManager() {
		if (getInstance().contributionManager == null) {
			getInstance().contributionManager = new HelpContributionManager();
		}
		return getInstance().contributionManager;
	}
	/**
	 */
	public static int getDebugLevel() {
		return getInstance().debug_level;
	}
	public static HelpSystem getInstance() {
		return instance;
	}
	/**
	 * Returns the ip and port, as http://ip:port
	 */
	public static URL getLocalHelpServerURL() {
		return HelpServer.getAddress();
	}
	/**
	 * Used to obtain Contribution Manager
	 * @return instance of ContributionManager
	 */
	public static HelpNavigationManager getNavigationManager() {
		if (getInstance().navigationManager == null) {
			// launch the help server to serve documents
			// Do this first to ensure that the HelpSystem server info is valid.
			HelpServer.instance();
			
			getInstance().navigationManager = new HelpNavigationManager();
		}
		return getInstance().navigationManager;
	}
	public static Plugin getPlugin() {
		return getInstance().plugin;
	}
	/**
	 * Returns the path to the help server.
	 * This is usually empty, but for remote install it can be
	 * something like /eclipse/servlet/help
	 */
	public static String getRemoteHelpServerPath() {
		return getInstance().remoteServerPath;
	}
	/**
	 * Returns the ip and port, as http://ip:port
	 */
	public static URL getRemoteHelpServerURL() {
		return getInstance().remoteServerURL;
	}
	/**
	 * Used to obtain Search Manager
	 * @return instance of SearchManager
	 */
	public static synchronized ISearchEngine getSearchManager() {
		if (getInstance().searchManager == null) {
			// obtain searchengine configuration from registry
			IPluginRegistry registry = Platform.getPluginRegistry();
			IExtensionPoint xpt = registry.getExtensionPoint(SEARCH_ENGINE_EXTENSION_POINT);
			if (xpt == null)
				return null;

			IExtension[] extList = xpt.getExtensions();
			if (extList.length == 0)
				return null;

			// only one pluggable viewer allowed ... always take first (only)
			// extension and its first (only) element
			IConfigurationElement[] cfigList = extList[0].getConfigurationElements();
			if (cfigList.length == 0)
				return null;

			if (!cfigList[0].getName().equals(SEARCH_ENGINE_CONFIG))
				return null;
			IConfigurationElement searchCfig = cfigList[0];

			// create executable engine and cache it
			try {
				getInstance().searchManager =
					(ISearchEngine) searchCfig.createExecutableExtension(SEARCH_ENGINE_CLASS);
			} catch (Exception e) {
				return null;
			}
		}
		return getInstance().searchManager;
	}
	public static boolean isClient() {
		return getInstance().install == INSTALL_CLIENT;
	}
	public static boolean isLocal() {
		return getInstance().install == INSTALL_LOCAL;
	}
	public static boolean isServer() {
		return getInstance().install == INSTALL_SERVER;
	}
	/**
	 */
	public HelpSystem newInstance() {
		return null;
	}
	public static void setBrowserPath(String path) {
		getInstance().browserPath = path;
	}
	public static void setDebugLevel(int debug_level) {
		getInstance().debug_level = debug_level;
		Logger.setDebugLevel(debug_level);
	}
	public static void setInstall(int install) {
		int oldInstall = getInstance().install;
		getInstance().install = install;
		// when the user has new preference we may need
		// to cleanup the managers
		if (oldInstall != install) {
			// contextManager stays the same

			// need new contribution manager
			if (getInstance().contributionManager != null)
				getInstance().contributionManager = null;

			// need new navigation manager
			if (getInstance().navigationManager != null)
				getInstance().navigationManager = null;

			// need new search manager
			if (getInstance().searchManager != null)
				getInstance().searchManager = null;
		}
	}
	public static void setLocalServerInfo(String addr, String port) {
		getInstance().localServerAddress = addr;
		getInstance().localServerPort = port;
		HelpServer.setAddress(addr, port);
	}
	public static void setPlugin(Plugin plugin) {
		getInstance().plugin = plugin;
	}
	public static void setRemoteServerInfo(String url) {
		try {
			if (url != null) {
				URL fullURL = new URL(url);
				getInstance().remoteServerURL =
					new URL(fullURL.getProtocol(), fullURL.getHost(), fullURL.getPort(), "");
				getInstance().remoteServerPath = fullURL.getFile();
			}
		} catch (MalformedURLException mue) {
		}
	}
	/**
	 * Shuts down the Help System.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public static void shutdown() throws CoreException {
		HelpServer.instance().close();
		Logger.logInfo(Resources.getString("I003"));
		Logger.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public static void startup() {
		try {
			if (isServer()) {
				// This is a server install, so need to generate navigation first
				getNavigationManager();
			}
			Logger.logInfo(Resources.getString("I002"));
		} catch (Exception e) {
			getPlugin().getLog().log(
				new Status(
					Status.ERROR,
					getPlugin().getDescriptor().getUniqueIdentifier(),
					0,
					Resources.getString("E005"),
					e));
		}
	}
}
