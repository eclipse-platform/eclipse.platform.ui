/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;

public class RepositoryProviderOtherSport extends RepositoryProvider {
	
	final public static String NATURE_ID = "org.eclipse.team.tests.core.other";

	/*
	 * @see RepositoryProvider#configureProject()
	 */
	public void configureProject() throws CoreException {
	}

	/*
	 * @see RepositoryProvider#getID()
	 */
	public String getID() {
		return NATURE_ID;
	}
	/*
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}
}