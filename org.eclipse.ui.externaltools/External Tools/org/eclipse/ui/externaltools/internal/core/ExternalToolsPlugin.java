package org.eclipse.ui.externaltools.internal.core;

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
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ui.externaltools"; //$NON-NLS-1$;
	/*package*/ static final String LOG_CONSOLE_ID = PLUGIN_ID + ".LogConsoleView"; //$NON-NLS-1$;
	
	public static final String IMG_ANT_SCRIPT= "icons/full/obj16/ant_file.gif"; //$NON-NLS-1$;
	public static final String IMG_BUILDER= "icons/full/obj16/builder.gif"; //$NON-NLS-1$;
	
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
}
