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
package org.eclipse.search.tests.filesearch;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.search.tests.ResourceHelper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class JUnitSourceSetup extends TestSetup {
	
	public static final String STANDARD_PROJECT_NAME= "JUnitSource";
	
	private IProject fProject= null;
	private final String fProjectName;
		
	public static IProject getStandardProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(STANDARD_PROJECT_NAME);
	}

	public JUnitSourceSetup(Test test) {
		this(test, STANDARD_PROJECT_NAME);
	}
	
	public JUnitSourceSetup(Test test, String projectName) {
		super(test);
		fProjectName= projectName;
	}
		
	protected void setUp() throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(fProjectName);
		if (!project.exists()) { // allow nesting of JUnitSetups
			fProject= ResourceHelper.createJUnitSourceProject(fProjectName);
		}
	}
	
	protected void tearDown() throws Exception {
		if (fProject != null) { // delete only by the setup who created the project
			ResourceHelper.deleteProject(fProjectName);
			fProject= null;
		}
	}
	

	

}
