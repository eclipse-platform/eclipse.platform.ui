package org.eclipse.toolscript.core.internal;

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

/**
 * General utility class dealing with Ant scripts
 */
public final class AntUtil {
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}

	/**
	 * Creates an intialized Ant project for the given
	 * Ant script file. Returns <code>null</code> if file
	 * is invalid Ant script.
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
}
