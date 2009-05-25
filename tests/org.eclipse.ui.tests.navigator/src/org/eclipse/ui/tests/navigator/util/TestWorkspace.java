/***************************************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others. All rights reserved. This program and the
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
	public static final String TEST_PROJECT_NAME= "Test"; //$NON-NLS-1$
	public static final String P1_PROJECT_NAME= "p1"; //$NON-NLS-1$
	public static final String P2_PROJECT_NAME= "p2"; //$NON-NLS-1$

	private static final String TEST_TESTDATA= "/testdata/Test.zip"; //$NON-NLS-1$
	private static final String TEST_P1= "/testdata/p1.zip"; //$NON-NLS-1$
	private static final String TEST_P2= "/testdata/p2.zip"; //$NON-NLS-1$


	public static void init() {
		initProject(new ProjectUnzipUtil(new Path(TEST_TESTDATA), new String[]{TEST_PROJECT_NAME}), TEST_PROJECT_NAME);
		initProject(new ProjectUnzipUtil(new Path(TEST_P1), new String[]{P1_PROJECT_NAME}), P1_PROJECT_NAME);
		initProject(new ProjectUnzipUtil(new Path(TEST_P2), new String[]{P2_PROJECT_NAME}), P2_PROJECT_NAME);
	}

	public static void initProject(ProjectUnzipUtil util, String projectName) {
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).isAccessible()) {
			util.createProjects();
		} else {
			util.reset();
		}
	}

	public static IProject getTestProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
	}
}
