package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * General utility class dealing with Ant files
 */
public final class AntUtil {
	// Holds the current monitor that the Ant build logger can access
	private static IProgressMonitor monitor;
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}

	/**
	 * Creates an intialized Ant project for the given
	 * Ant file. Returns <code>null</code> if file
	 * is invalid Ant format.
	 */	
	public static Project createAntProject(IPath path) {
		// create an ant project and initialize it
		Project antProject = new Project();
		antProject.init();		
		antProject.setProperty("ant.file", path.toOSString()); //$NON-NLS-1$;

		try {
			ProjectHelper.configureProject(antProject, new File(path.toOSString()));
		} catch (VirtualMachineError e) {
			throw e;		// Let others handle this
		} catch (Throwable t) {
			return null;	// Assume invalid format problems
		}
		
		return antProject;
	}
	
	/**
	 * Returns the last known progress monitor that the
	 * Ant build logger can use
	 */
	public static IProgressMonitor getCurrentProgressMonitor() {
		return AntUtil.monitor;
	}
	
	/**
	 * Sets the last known progress monitor that the
	 * Ant build logger can use
	 */
	public static void setCurrentProgressMonitor(IProgressMonitor monitor) {
		AntUtil.monitor = monitor;
	}
}
