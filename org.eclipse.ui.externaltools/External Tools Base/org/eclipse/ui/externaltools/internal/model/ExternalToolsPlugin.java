package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.registry.ArgumentVariableRegistry;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolTypeRegistry;
import org.eclipse.ui.externaltools.internal.registry.PathLocationVariableRegistry;
import org.eclipse.ui.externaltools.internal.registry.RefreshScopeVariableRegistry;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin {
	/**
	 * Status representing no problems encountered during operation.
	 */
	public static final IStatus OK_STATUS = new Status(IStatus.OK, IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$

	private static ExternalToolsPlugin plugin;
	
	private ExternalToolRegistry toolRegistry;
	private ExternalToolTypeRegistry typeRegistry;
	private RefreshScopeVariableRegistry refreshVarRegistry;
	private PathLocationVariableRegistry fileLocVarRegistry;
	private PathLocationVariableRegistry dirLocVarRegistry;
	private ArgumentVariableRegistry argumentVarRegistry;
	
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
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(Status.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, exception);
	}
	
	/**
	 * Returns a new <code>CoreException</code> for this plug-in
	 */
	public static CoreException newError(String message, Throwable exception) {
		return new CoreException(new Status(Status.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, exception));
	}
	
	/**
	 * Returns the registry of refresh scope variables.
	 */
	public ArgumentVariableRegistry getArgumentVariableRegistry() {
		if (argumentVarRegistry == null)
			argumentVarRegistry = new ArgumentVariableRegistry();
		return argumentVarRegistry;
	}
	
	/**
	 * Returns the registry of directory location variables.
	 */
	public PathLocationVariableRegistry getDirectoryLocationVariableRegistry() {
		if (dirLocVarRegistry == null)
			dirLocVarRegistry = new PathLocationVariableRegistry(IExternalToolConstants.PL_DIRECTORY_VARIABLES);
		return dirLocVarRegistry;
	}
	
	/**
	 * Returns the registry of file location variables.
	 */
	public PathLocationVariableRegistry getFileLocationVariableRegistry() {
		if (fileLocVarRegistry == null)
			fileLocVarRegistry = new PathLocationVariableRegistry(IExternalToolConstants.PL_FILE_VARIABLES);
		return fileLocVarRegistry;
	}
	
	/**
	 * Returns the registry of refresh scope variables.
	 */
	public RefreshScopeVariableRegistry getRefreshVariableRegistry() {
		if (refreshVarRegistry == null)
			refreshVarRegistry = new RefreshScopeVariableRegistry();
		return refreshVarRegistry;
	}
	
	/**
	 * Returns the registry of external tools.
	 * 
	 * @param shell the shell to use for displaying any errors
	 * 		when loading external tool definitions from storage
	 * 		or <code>null</code> to not report these problems.
	 */
	public ExternalToolRegistry getToolRegistry(Shell shell) {
		if (toolRegistry == null)
			toolRegistry = new ExternalToolRegistry(shell);
		return toolRegistry;
	}

	/**
	 * Returns the registry of external tool types.
	 */
	public ExternalToolTypeRegistry getTypeRegistry() {
		if (typeRegistry == null)
			typeRegistry = new ExternalToolTypeRegistry();
		return typeRegistry;
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		getLog().log(status);
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
