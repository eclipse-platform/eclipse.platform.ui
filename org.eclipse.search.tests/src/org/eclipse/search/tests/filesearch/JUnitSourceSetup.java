/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import org.junit.rules.ExternalResource;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.tests.ResourceHelper;

public class JUnitSourceSetup extends ExternalResource {
	
	public static final String STANDARD_PROJECT_NAME= "JUnitSource";
	
	private IProject fProject= null;
	private final String fProjectName;
		
	public IProject getStandardProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(STANDARD_PROJECT_NAME);
	}

	public JUnitSourceSetup() {
		this(STANDARD_PROJECT_NAME);
	}
	
	public JUnitSourceSetup(String projectName) {
		fProjectName= projectName;
	}
		
	@Override
	public void before() throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(fProjectName);
		if (!project.exists()) { // allow nesting of JUnitSetups
			fProject= ResourceHelper.createJUnitSourceProject(fProjectName);
		}
	}
	
	@Override
	public void after() /*throws Exception (but JUnit4 API is stupid...)*/ {
		if (fProject != null) { // delete only by the setup who created the project
			try {
				ResourceHelper.deleteProject(fProjectName);
			} catch (CoreException e) {
				throw new AssertionError(e); // workaround stupid JUnit4 API
			}
			fProject= null;
		}
	}

}
