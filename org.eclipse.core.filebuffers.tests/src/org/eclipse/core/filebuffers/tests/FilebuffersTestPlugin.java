/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * The main plug-in class to be used in the desktop.
 * 
 * @since 3.0
 */
public class FilebuffersTestPlugin extends Plugin {
	//The shared instance.
	private static FilebuffersTestPlugin fPlugin;
	
	/**
	 * The constructor.
	 */
	public FilebuffersTestPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fPlugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static FilebuffersTestPlugin getDefault() {
		return fPlugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL= new URL(plugin.getDescriptor().getInstallURL(), path.toString());
			URL localURL= Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
}
