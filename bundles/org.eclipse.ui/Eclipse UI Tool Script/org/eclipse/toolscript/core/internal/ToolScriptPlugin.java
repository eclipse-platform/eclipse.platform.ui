package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.toolscript.ui.internal.LogConsoleView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * Fake plugin class until tool scripts becomes a real
 * plugin outside of eclipse ui.
 */
public final class ToolScriptPlugin {
	public static final String PLUGIN_ID = "org.eclipse.toolscript"; //$NON-NLS-1$;
	private static final String LOG_CONSOLE_ID = PLUGIN_ID + ".LogConsoleView"; //$NON-NLS-1$;
	
	public static final String IMG_ANT_SCRIPT= "icons/full/eview16/ant_view.gif"; //$NON-NLS-1$;
	public static final String IMG_BUILDER= "icons/full/eview16/build_exec.gif"; //$NON-NLS-1$;
	
	private static ToolScriptPlugin plugin;
	private ToolScriptRegistry registry;
	
	/**
	 * Create an instance of the WorkbenchPlugin.
	 * The workbench plugin is effectively the "application" for the workbench UI.
	 * The entire UI operates as a good plugin citizen.
	 */
	public ToolScriptPlugin(IPluginDescriptor descriptor) {
//********* Uncomment when real plugin		
		//super(descriptor);
		super();
		plugin = this;
	}
	
	/**
	 * Returns the default instance of the receiver.
	 * This represents the runtime plugin.
	 */
	public static ToolScriptPlugin getDefault() {
//********* Remove lazy initialize code when real plugin
		if (plugin == null)
			plugin = new ToolScriptPlugin(null);
		return plugin;
	}

	/**
	 * Clears the log messages recorded so far.
	 */
	public void clearLogDocument() {
	}
	
	/**
	 * Returns the registry of tool scripts that the
	 * user can run using the tool script menu. Does
	 * not include tool scripts part of the build process.
	 */
	public ToolScriptRegistry getRegistry() {
		if (registry == null)
			registry = new ToolScriptRegistry();
		return registry;
	}

	/**
	 * Returns the tool script runner for the specified
	 * type, or <code>null</code> if none registered.
	 */
	public ToolScriptRunner getToolScriptRunner(String type) {
		if (ToolScript.SCRIPT_TYPE_PROGRAM.equals(type))
			return new ProgramScriptRunner();
		if (ToolScript.SCRIPT_TYPE_ANT.equals(type))
			return new AntScriptRunner();
		return null;
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
	
	/**
	 * Forces the log console view to open. Returns the view
	 * part if successful, otherwise <code>null</code>.
	 */
	public LogConsoleView showLogConsole(IWorkbenchWindow window) {
		IWorkbenchPage page = window.getActivePage();
		LogConsoleView console = null;
		try {
			if (page != null)
				console = (LogConsoleView) page.showView(LOG_CONSOLE_ID);
		} catch (PartInitException e) {
			getLog().log(e.getStatus());
		}
		return console;
	}
	
	/**
	 * Fake this out until the tool scripts becomes a
	 * real plugin.
	 */
	public final org.eclipse.core.runtime.ILog getLog() {
//********* Remove this method when real plugin
		return org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getLog();
	}

	/**
	 * Fake this out until the tool scripts becomes a
	 * real plugin.
	 */
	public final org.eclipse.jface.preference.IPreferenceStore getPreferenceStore() {
//********* Remove this method when real plugin
		return org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Fake this out until tool scripts becomes a
	 * real plugin
	 */
	public final org.eclipse.core.runtime.IPath getStateLocation() {
//********* Remove this method when real plugin
		return org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getStateLocation();
	}

	/**
	 * Fake this out until tool scripts becomes a
	 * real plugin
	 */
	public final IPluginDescriptor getDescriptor() {
//********* Remove this method when real plugin
		return org.eclipse.ui.internal.WorkbenchPlugin.getDefault().getDescriptor();
	}
}
