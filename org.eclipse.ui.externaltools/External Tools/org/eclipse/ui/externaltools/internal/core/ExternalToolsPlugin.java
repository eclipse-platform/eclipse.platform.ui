package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ui.externaltools"; //$NON-NLS-1$;
	/*package*/ static final String LOG_CONSOLE_ID = PLUGIN_ID + ".LogConsoleView"; //$NON-NLS-1$;
	
	public static final String IMG_ANT_TOOL= "icons/full/obj16/ant_file.gif"; //$NON-NLS-1$;
	public static final String IMG_BUILDER= "icons/full/obj16/builder.gif"; //$NON-NLS-1$;
	public static final String IMG_JAR_FILE = "icons/full/obj16/jar_l_obj.gif"; //$NON-NLS-1$;
	public static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;
	public static final String IMG_TYPE = "icons/full/obj16/type.gif"; //$NON-NLS-1$;
	public static final String IMG_EXTERNAL_TOOL = "icons/full/obj16/external_tools.gif"; //$NON-NLS-1$
	public static final String IMG_INVALID_BUILD_TOOL = "icons/full/obj16/invalid_build_tool.gif"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_EXTERNAL_TOOLS = "icons/full/wizban/ext_tools_wiz.gif"; //$NON-NLS-1$
	
	private static ExternalToolsPlugin plugin;
	private ExternalToolsRegistry registry;
	
	/**
	 * Create an instance of the External Tools plug-in.
	 */
	public ExternalToolsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	
	/**
	 * Returns the default instance of the receiver.
	 * This represents the runtime plugin.
	 */
	public static ExternalToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the registry of external tools that the
	 * user can run using the external tools menu. Does
	 * not include external tools part of the build process.
	 */
	public ExternalToolsRegistry getRegistry() {
		if (registry == null)
			registry = new ExternalToolsRegistry();
		return registry;
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public void log(String message, Throwable exception) {
		Status status = new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
		getLog().log(status);
		System.err.println(message);
	}

	/**
	 * Returns the ImageDescriptor for the icon with the given path
	 * 
	 * @return the ImageDescriptor object
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		try {
			URL installURL = getDescriptor().getInstallURL();
			URL url = new URL(installURL,path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in AbstractUIPlugin.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		prefs.setDefault(IPreferenceConstants.AUTO_SAVE, false);
		prefs.setDefault(IPreferenceConstants.INFO_LEVEL, true);
		prefs.setDefault(IPreferenceConstants.VERBOSE_LEVEL, false);
		prefs.setDefault(IPreferenceConstants.DEBUG_LEVEL, false);
	
		PreferenceConverter.setDefault(prefs, IPreferenceConstants.CONSOLE_ERROR_RGB, new RGB(255, 0, 0)); 		// red - exactly the same as debug Consol
		PreferenceConverter.setDefault(prefs, IPreferenceConstants.CONSOLE_WARNING_RGB, new RGB(255, 100, 0)); 	// orange
		PreferenceConverter.setDefault(prefs, IPreferenceConstants.CONSOLE_INFO_RGB, new RGB(0, 0, 255)); 		// blue
		PreferenceConverter.setDefault(prefs, IPreferenceConstants.CONSOLE_VERBOSE_RGB, new RGB(0, 200, 125));	// green
		PreferenceConverter.setDefault(prefs, IPreferenceConstants.CONSOLE_DEBUG_RGB, new RGB(0, 0, 0));			// black
	}	
}
