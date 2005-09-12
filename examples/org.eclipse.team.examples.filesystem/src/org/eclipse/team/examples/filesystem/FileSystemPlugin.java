/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProviderPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is the plugin class for the file system examples. It provides the following:
 * 
 * <ol>
 * <li>public fields for the plugin and provider IDs as defined in the plugin.xml
 * <li>initialization on startup of Policy class that provides internationalization of strings
 * <li>helper methods for outputing IStatus objects to the log
 * <li>helper methods for converting CoreExceptions and IOExceptions to TeamExceptions
 * </ol>
 */
public class FileSystemPlugin extends AbstractUIPlugin {
	
	/**
	 * This is the ID of the plugin as defined in the plugin.xml
	 */
	public static final String ID = "org.eclipse.team.examples.filesystem"; //$NON-NLS-1$
	
	/**
	 * This is the provider ID of the plugin as defined in the plugin.xml
	 */
	public static final String PROVIDER_ID = ID + ".FileSystemProvider"; //$NON-NLS-1$
	
	// This static field will hold the singleton instance of the plugin class
	private static FileSystemPlugin plugin;
	
	/**
	 * Override the standard plugin constructor.
	 * 
	 * @param descriptor the plugin descriptor
	 */
	public FileSystemPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		// record this instance as the singleton
		plugin = this;
		// Instanctiate pessimistic provider
		new PessimisticFilesystemProviderPlugin(descriptor);
	}
	
	/**
	 * Return the singlton instance of the plugin class to allow other
	 * classes in the plugin access to plugin instance methods such as 
	 * those for logging errors, etc.
	 */
	public static FileSystemPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Helper method to convert a CoreException into a TeamException.
	 * We do this to maintain the core status and code. This type of
	 * mapping may not be appropriate in more complicated exception 
	 * handling situations.
	 * 
	 * @param e the CoreException
	 */
	public static TeamException wrapException(CoreException e) {
		return new TeamException(e.getStatus());
	}

	/**
	 * Helper method to convert an IOException into a TeamException.
	 * This type of mapping may not be appropriate in more complicated 
	 * exception handling situations.
	 * 
	 * @param e the CoreException
	 */
	public static TeamException wrapException(IOException e) {
		return new TeamException(new Status(IStatus.ERROR, FileSystemPlugin.ID, 
			TeamException.IO_FAILED, e.getMessage(), e));
	}
	
	/**
	 * Helper method to log an exception status.
	 * 
	 * @param status the status to be logged
	 */
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}
}

