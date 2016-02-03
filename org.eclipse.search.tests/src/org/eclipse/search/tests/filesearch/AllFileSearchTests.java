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
package org.eclipse.search.tests.filesearch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.tests.ResourceHelper;

@RunWith(Suite.class)
@SuiteClasses({
	AnnotationManagerTest.class,
	FileSearchTests.class,
	LineAnnotationManagerTest.class,
	PositionTrackerTest.class,
	ResultUpdaterTest.class,
	SearchResultPageTest.class,
	SortingTest.class
})
public class AllFileSearchTests {
	
	public static final String STANDARD_PROJECT_NAME= "JUnitSource";

	private static IProject fProject;

	@BeforeClass
	public static void globalSetUp() throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(STANDARD_PROJECT_NAME);
		if (!project.exists()) { // allow nesting of JUnitSetups
			fProject= ResourceHelper.createJUnitSourceProject(STANDARD_PROJECT_NAME);
		}
	}
	
	@AfterClass
	public static void globalTearDown() throws Exception {
		if (fProject != null) { // delete only by the setup who created the project
			ResourceHelper.deleteProject(STANDARD_PROJECT_NAME);
			fProject= null;
		}
	}
}
