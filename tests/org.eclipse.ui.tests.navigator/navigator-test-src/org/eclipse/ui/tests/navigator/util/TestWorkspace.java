/***************************************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.tests.navigator.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

public class TestWorkspace {
	public static final String TEST_RESOURCE_PROJECT_NAME = "TestResourceProject"; //$NON-NLS-1$
	
	private static final String TEST_RESOURCE_PROJECT_TESTDATA = "/testdata/TestResourceProject.zip"; //$NON-NLS-1$
	
	public static final String MODULE1_PROJECT_NAME = "module1"; //$NON-NLS-1$
	
	private static final String MODULE1_PROJECT_TESTDATA = "/testdata/module1.zip"; //$NON-NLS-1$

	public static void init() {

		// setup test resource project
		ProjectUnzipUtil util = new ProjectUnzipUtil(new Path(
				TEST_RESOURCE_PROJECT_TESTDATA),
				new String[] { TEST_RESOURCE_PROJECT_NAME });

		if (!getResourceTestProject().isAccessible()) {
			util.createProjects();
		} else {
			util.reset();
		}

		util = new ProjectUnzipUtil(new Path(
				MODULE1_PROJECT_TESTDATA),
				new String[] { MODULE1_PROJECT_NAME });

		if (!getModuleTestProject().isAccessible()) {
			util.createProjects();
		} else {
			util.reset();
		}	
	}

	public static IProject getResourceTestProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(
				TEST_RESOURCE_PROJECT_NAME);
	}

	public static IProject getModuleTestProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(
				MODULE1_PROJECT_NAME);
	}
}
