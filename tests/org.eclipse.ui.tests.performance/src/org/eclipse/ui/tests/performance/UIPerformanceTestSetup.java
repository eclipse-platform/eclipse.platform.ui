/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import junit.extensions.TestSetup;
import junit.framework.Test;

public class UIPerformanceTestSetup extends TestSetup {

	public static final String PERSPECTIVE1= "org.eclipse.ui.tests.performancePerspective1";
	public static final String PERSPECTIVE2= "org.eclipse.ui.tests.performancePerspective2";

	public static final String PROJECT_NAME = "Performance Project";

	private static final String INTRO_VIEW= "org.eclipse.ui.internal.introview";
	public static final String[] EDITOR_FILE_EXTENSIONS = { "perf_basic", "perf_outline", "java" };

	private IProject testProject;

	public UIPerformanceTestSetup(Test test) {
		super(test);
	}

	@Override
	protected void setUp() throws Exception {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow= workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage= activeWindow.getActivePage();

		activePage.hideView(activePage.findViewReference(INTRO_VIEW));

		workbench.showPerspective(PERSPECTIVE1, activeWindow);

		boolean wasAutobuilding= ResourceTestHelper.disableAutoBuilding();
		setUpProject();
		ResourceTestHelper.fullBuild();
		if (wasAutobuilding) {
			ResourceTestHelper.enableAutoBuilding();
			EditorTestHelper.calmDown(2000, 30000, 1000);
		}
	}

	private void setUpProject() throws CoreException {

		// Create a java project.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		testProject = workspace.getRoot().getProject(PROJECT_NAME);
		testProject.create(null);
		testProject.open(null);

		for (String EDITOR_FILE_EXTENSION : EDITOR_FILE_EXTENSIONS) {
			createFiles(EDITOR_FILE_EXTENSION);
		}
	}


	private void createFiles(String ext) throws CoreException {
		for (int i = 0; i < 100; i++) {
			String fileName = i + "." + ext;
			IFile iFile = testProject.getFile(fileName);
			iFile.create(new ByteArrayInputStream(new byte[] { '\n' }), true, null);
		}
	}
}
