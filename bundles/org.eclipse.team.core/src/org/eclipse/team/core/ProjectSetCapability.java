/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.core;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provisional
 */

public abstract class ProjectSetCapability {
	/**
	 * Notify the provider that a project set has been created at path.
	 * Only providers identified as having projects in the project set will be
	 * notified.  The project set may or may not be created in a workspace
	 * project (thus may not be a resource).
	 * 
	 * @param File the project set file that was created
	 */	
	public void projectSetCreated(File file, Object context, IProgressMonitor monitor) {
		//default is to do nothing
	}
		
	/**
	 * Returns true if when importing a project set the projects can be created
	 * at a specified file system location different than the default.
	 * 
	 * NOTE: If this method is overriden to return true, then the provider
	 * <b>must</b> also override addToWorkspace(String[], String, IPath, Object,
	 * IProgressMonitor);
	 * 
	 * @return boolean
	 */
	public boolean supportsProjectSetImportRelocation() {
		return false;
	}
	
}
