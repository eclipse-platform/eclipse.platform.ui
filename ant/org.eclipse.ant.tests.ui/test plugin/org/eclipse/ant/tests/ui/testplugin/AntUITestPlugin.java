/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.testplugin;


import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class AntUITestPlugin extends AbstractUIPlugin {
	
	private static AntUITestPlugin deflt;
	
	public AntUITestPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		deflt= this;
	}
	
	public static AntUITestPlugin getDefault() {
		return deflt;
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static void enableAutobuild(boolean enable) throws CoreException {
		// disable auto build
		IWorkspace workspace= AntUITestPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		desc.setAutoBuilding(enable);
		workspace.setDescription(desc);
	}
	
	public File getFileInPlugin(IPath path) {
		try {
			URL installURL= new URL(getDescriptor().getInstallURL(), path.toString());
			URL localURL= Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
}
