package org.eclipse.help.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributors.xml.*;
import org.eclipse.help.internal.navigation.HelpNavigationManager;
import org.eclipse.help.internal.server.HelpServer;
import org.eclipse.help.internal.util.*;
/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();
	protected ContributionManager contributionManager;
	protected HelpNavigationManager navigationManager;
	protected ContextManager contextManager;
	int debug_level;
	private String browserPath;
	// constants
	private static final String SEARCH_ENGINE_EXTENSION_POINT =
		"org.eclipse.help.searchEngine";
	private static final String SEARCH_ENGINE_CONFIG = "config";
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
	protected HelpPreferences helpPref = null;
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
	public static HelpPreferences getPreferences() {
		return getInstance().helpPref;
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
			getInstance().navigationManager = new HelpNavigationManager();
		}
		return getInstance().navigationManager;
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
	private void initializePrefFromStore() {
		helpPref = new HelpPreferences();
		HelpSystem.setInstall(helpPref.getInt(HelpPreferences.INSTALL_OPTION_KEY));
		HelpSystem.setRemoteServerInfo(
			helpPref.getString(HelpPreferences.SERVER_PATH_KEY));
		if (helpPref.getInt(HelpPreferences.LOCAL_SERVER_CONFIG) > 0) {
			HelpSystem.setLocalServerInfo(
				helpPref.getString(HelpPreferences.LOCAL_SERVER_ADDRESS_KEY),
				helpPref.getString(HelpPreferences.LOCAL_SERVER_PORT_KEY));
		} else {
			HelpSystem.setLocalServerInfo(null, "0");
		}
		HelpSystem.setDebugLevel(helpPref.getInt(HelpPreferences.LOG_LEVEL_KEY));
		HelpSystem.setBrowserPath(helpPref.getString(HelpPreferences.BROWSER_PATH_KEY));
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
		}
	}
	public static void setLocalServerInfo(String addr, String port) {
		getInstance().localServerAddress = addr;
		getInstance().localServerPort = port;
		HelpServer.setAddress(addr, port);
	}
	public static void setRemoteServerInfo(String url) {
		URL oldURL = getInstance().remoteServerURL;
		String oldPath = getInstance().remoteServerPath;
		try {
			if (url != null) {
				URL fullURL = new URL(url);
				getInstance().remoteServerURL =
					new URL(fullURL.getProtocol(), fullURL.getHost(), fullURL.getPort(), "");
				getInstance().remoteServerPath = fullURL.getFile();
			}
		} catch (MalformedURLException mue) {
		}
		if (getInstance().install == 1) { // remote
			if ((oldURL == null
				|| !oldURL.equals(getInstance().remoteServerURL)
				|| (oldPath == null || !oldPath.equals(getInstance().remoteServerPath)))) {
				// contextManager stays the same
				// contribution manager stays the same
				// need new navigation manager
				if (getInstance().navigationManager != null)
					getInstance().navigationManager = null;
			}
		}
	}
	/**
	 * Shuts down the Help System.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	static void shutdown() throws CoreException {
		getPreferences().save();
		Logger.logInfo(Resources.getString("I003"));
		Logger.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	static void startup() {
		try {
			instance.initializePrefFromStore();
		} catch (Exception e) {
			HelpPlugin.getDefault().getLog().log(
				new Status(
					Status.ERROR,
					HelpPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					0,
					Resources.getString("E005"),
					e));
		}
	}
}