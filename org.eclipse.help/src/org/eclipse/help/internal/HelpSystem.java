/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal;
import org.eclipse.core.runtime.*;
import org.eclipse.help.AppServer;
import org.eclipse.help.internal.context.ContextManager;
import org.eclipse.help.internal.search.SearchManager;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.help.internal.util.*;
/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();

	public final static String LOG_LEVEL_KEY = "log_level";
	public final static String BANNER_KEY = "banner";
	public final static String BANNER_HEIGHT_KEY = "banner_height";
	public final static String LINKS_VIEW_KEY = "linksView";
	public final static String BASE_TOCS_KEY = "baseTOCS";
	public final static String BOOKMARKS = "bookmarks";
	public final static int MODE_WORKBENCH = 0;
	public final static int MODE_INFOCENTER = 1;
	public final static int MODE_STANDALONE = 2;

	protected TocManager tocManager;
	protected ContextManager contextManager;
	protected SearchManager searchManager;
	private int mode = MODE_WORKBENCH;
	private boolean webappStarted = false;
	private boolean webappRunning = false;
	/**
	 * HelpSystem constructor comment.
	 */
	private HelpSystem() {
		super();
	}
	/**
	 * Used to obtain Context Manager
	 * returns an instance of ContextManager
	 */
	public static ContextManager getContextManager() {
		if (getInstance().contextManager == null)
			getInstance().contextManager = new ContextManager();
		return getInstance().contextManager;
	}

	public static HelpSystem getInstance() {
		return instance;
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
	 * Used to obtain Search Manager
	 * @return instance of SearchManager
	 */
	public static SearchManager getSearchManager() {
		if (getInstance().searchManager == null) {
			getInstance().searchManager = new SearchManager();
		}
		return getInstance().searchManager;
	}
	/**
	 */
	public HelpSystem newInstance() {
		return null;
	}

	/**
	 * Shuts down the Help System.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public static void shutdown() throws CoreException {
		Logger.logInfo(Resources.getString("I003"));
		Logger.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public static void startup() {
		try {
			Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
			Logger.setDebugLevel(prefs.getInt(LOG_LEVEL_KEY));
		} catch (Exception e) {
			HelpPlugin.getDefault().getLog().log(
				new Status(
					Status.ERROR,
					HelpPlugin
						.getDefault()
						.getDescriptor()
						.getUniqueIdentifier(),
					0,
					Resources.getString("E005"),
					e));
		}
		Logger.logInfo(Resources.getString("I002"));
	}
	public static boolean ensureWebappRunning() {
		if (!getInstance().webappStarted) {
			getInstance().webappStarted = true;
			if (getMode()!=MODE_WORKBENCH) {
				// start the help control web app
				AppServer.add("helpControl", "org.eclipse.help.webapp", "");
			}
			// start the help web app
			getInstance().webappRunning =
				AppServer.add("help", "org.eclipse.help.webapp", "");
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

}
