/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal;
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.contributors.xml1_0.HelpContributionManager;
import org.eclipse.help.internal.contributors1_0.ContributionManager;
import org.eclipse.help.internal.navigation1_0.HelpNavigationManager;
import org.eclipse.help.internal.server.HelpServer;
import org.eclipse.help.internal.topics.*;
import org.eclipse.help.internal.util.*;
/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();
	// TopicsContributorsManager for topics contributors
	protected TopicsContributorsManager topicsContributorsManager;
	// 1.0 nav support
	// ContributionManager for help v1.0 contributions
	protected ContributionManager contributionManager;
	// Help Naviagation Manager for v1.0 navigation
	protected HelpNavigationManager navigationManager;
	// eo 1.0 nav support
	// Topics NavigationManager for topics navigation
	protected TopicsNavigationManager topicsNavigationManager;
	protected IContextManager contextManager;

	private String browserPath;
	// constants
	private static final String SEARCH_ENGINE_EXTENSION_POINT =
		"org.eclipse.help.searchEngine";
	private static final String SEARCH_ENGINE_CONFIG = "config";
	private String localServerAddress;
	private String localServerPort;
	protected HelpPreferences preferences = null;
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
	public static IContextManager getContextManager() {
		if (getInstance().contextManager == null)
			getInstance().contextManager = new ContextManager();
		return getInstance().contextManager;
	}
	// 1.0 nav support
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
	 * Used to obtain Help Navigation Manager
	 * @return instance of HelpNavigationManager
	 */
	public static HelpNavigationManager getNavigationManager() {
		if (getInstance().navigationManager == null) {
			getInstance().navigationManager = new HelpNavigationManager();
		}
		return getInstance().navigationManager;
	}
	// eof 1.0 nav support

	public static HelpPreferences getPreferences() {
		return getInstance().preferences;
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
	 * Used to obtain TopicsContributorsManager
	 * @return instance of TopicsContributorsManager
	 */
	public static TopicsContributorsManager getTopicsContributorsManager() {
		if (getInstance().topicsContributorsManager == null) {
			getInstance().topicsContributorsManager = new TopicsContributorsManager();
		}
		return getInstance().topicsContributorsManager;
	}
	/**
	 * Used to obtain Topics Naviagiont Manager
	 * @return instance of TopicsNavigationManager
	 */
	public static TopicsNavigationManager getTopicsNavigationManager() {
		if (getInstance().topicsNavigationManager == null) {
			getInstance().topicsNavigationManager = new TopicsNavigationManager();
		}
		return getInstance().topicsNavigationManager;
	}
	/**
	 */
	public HelpSystem newInstance() {
		return null;
	}
	public static void setBrowserPath(String path) {
		getInstance().browserPath = path;
	}
	public static void setLocalServerInfo(String addr, String port) {
		getInstance().localServerAddress = addr;
		getInstance().localServerPort = port;
		HelpServer.setAddress(addr, port);
	}
	public static void setPreferences(HelpPreferences newPreferences) {
		getInstance().preferences = newPreferences;
			
		Logger.setDebugLevel(newPreferences.getInt(HelpPreferences.LOG_LEVEL_KEY));
				
		if (newPreferences.getInt(HelpPreferences.LOCAL_SERVER_CONFIG) > 0) {
			setLocalServerInfo(
				newPreferences.getString(HelpPreferences.LOCAL_SERVER_ADDRESS_KEY),
				newPreferences.getString(HelpPreferences.LOCAL_SERVER_PORT_KEY));
		} else {
			setLocalServerInfo(null, "0");
		}

		HelpSystem.setBrowserPath(
			newPreferences.getString(HelpPreferences.BROWSER_PATH_KEY));
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
			setPreferences(new HelpPreferences());
		} catch (Exception e) {
			HelpPlugin.getDefault().getLog().log(
				new Status(
					Status.ERROR,
					HelpPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					0,
					Resources.getString("E005"),
					e));
		}
		Logger.logInfo(Resources.getString("I002"));
	}
}