/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.search.SearchManager;
/**
 * The actual implementation of the help system plugin.
 */
public final class HelpSystem {
	protected static final HelpSystem instance = new HelpSystem();

	// TocManager
	protected TocManager tocManager;
	protected ContextManager contextManager;
	protected HelpPreferences preferences = null;
	protected SearchManager searchManager;
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

	public static HelpPreferences getPreferences() {
		return getInstance().preferences;
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
	public static void setPreferences(HelpPreferences newPreferences) {
		getInstance().preferences = newPreferences;
		Logger.setDebugLevel(newPreferences.getInt(HelpPreferences.LOG_LEVEL_KEY));
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
