/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
/**
 * For displaying help to the user. The Eclipse platform defines an extension 
 * point (<code>"org.eclipse.help.support"</code>) for the help system UI.
 * The help system UI is entirely optional (the standard help system UI provided
 * by the <code>"org.eclipse.help.ui"</code> plug-in is not mandatory).
 * This class provides static convenience methods for showing help to the user
 * when possible; the methods do nothing when no help system UI is unavailable.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class Help {
	// configuration information
	private static IConfigurationElement viewerCfig = null;
	// pluggable viewer
	private static IHelp helpSupport = null;
	// constants
	private static final String HELP_EXTENSION_POINT = "org.eclipse.help.support";
	private static final String HELP_CONFIG = "config";
	private static final String HELP_CLASS = "class";
	static {
		// initialize the unique help browser.
		init();
	}
	/**
	 * Hide default constructor
	 */
	private Help() {
		super();
	}
	/**
	 * Displays context-sensitive help for contexts with the given context ids.
	 * Does nothing if no help system UI is available.
	 * 
	 * @param contextIds a list of help context ids
	 * @param x horizontal position
	 * @param y verifical position
	 * @see #findContext
	 * @see IHelp#displayHelp
	 */
	public static void displayHelp(String[] contextIds, int x, int y) {
		// delegate to pluggable viewer
		if (helpSupport != null)
			helpSupport.displayHelp(contextIds, x, y);
	}
	/**
	 * Displays context-sensitive help for the given contexts.
	 * Does nothing if no help system UI is available.
	 * 
	 * @param contexts a list of help contexts
	 * @param x horizontal position
	 * @param y verifical position
	 * @see IHelp#displayHelp
	 */
	public static void displayHelp(IContext[] contexts, int x, int y) {
		// delegate to pluggable viewer
		if (helpSupport != null)
			helpSupport.displayHelp(contexts, x, y);
	}
	/**
	 * Displays help content for the specified Table of Contents.
	 * Does nothing if no help system UI is available.
	 *
	 * @param tocURL URL of Table of Contents
	 * @see IHelp#displayHelp(java.lang.String)
	 */
	public static void displayHelp(String tocURL) {
		// delegate to pluggable viewer
		if (helpSupport != null)
			helpSupport.displayHelp(tocURL);
	}
	/**
	 * Computes and returns context information for the given context id.
	 * Does nothing if no help system UI is available.
	 *
	 * @param contextId the context id
	 * @return the context, or <code>null</code> if none or if there is no
	 *   help system UI
	 * @see IHelp#findContext
	 */
	public static IContext findContext(String contextID) {
		if (helpSupport != null)
			return helpSupport.findContext(contextID);
		return null;
	}
	private static void init() {
		// obtain viewer configuration from registry
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint xpt = registry.getExtensionPoint(HELP_EXTENSION_POINT);
		if (xpt == null)
			return;
		IExtension[] extList = xpt.getExtensions();
		if (extList.length == 0)
			return;
		// only one pluggable viewer allowed ... always take first (only)
		// extension and its first (only) element
		IConfigurationElement[] cfigList = extList[0].getConfigurationElements();
		if (cfigList.length == 0)
			return;
		if (!cfigList[0].getName().equals(HELP_CONFIG))
			return;
		viewerCfig = cfigList[0];
		// create executable viewer and cache it
		try {
			//this.viewer = (IHelpViewer) viewerCfig.createExecutableExtension(VIEWER_CLASS);
			helpSupport = (IHelp) viewerCfig.createExecutableExtension(HELP_CLASS);
		} catch (Exception e) { /* die silently */
		}
	}
}