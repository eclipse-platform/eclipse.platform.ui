/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.server.HelpServer;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.help.internal.util.*;
/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();

	// TocManager
	protected TocManager tocManager;
	protected ContextManager contextManager;
	private String browserPath;
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
	public static ContextManager getContextManager() {
		if (getInstance().contextManager == null)
			getInstance().contextManager = new ContextManager();
		return getInstance().contextManager;
	}

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
	 * Used to obtain Toc Naviagiont Manager
	 * @return instance of TocManager
	 */
	public static TocManager getTocManager() {
		if (getInstance().tocManager == null) {
			getInstance().tocManager = new TocManager();
		}
		return getInstance().tocManager;
	}
	/**
	 */
	public HelpSystem newInstance() {
		return null;
	}
	static void setBrowserPath(String path) {
		getInstance().browserPath = path;
	}
	static void setLocalServerInfo(String addr, String port) {
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
	public static void shutdown() throws CoreException {
		getPreferences().save();
		Logger.logInfo(Resources.getString("I003"));
		Logger.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public static void startup() {
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