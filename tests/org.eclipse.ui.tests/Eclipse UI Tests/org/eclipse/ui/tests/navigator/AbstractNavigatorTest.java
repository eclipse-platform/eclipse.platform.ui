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
package org.eclipse.ui.tests.navigator;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * The AbstractNavigatorTest is the abstract superclass
 * of tests that use a populated Resource Navigator.
 */
public abstract class AbstractNavigatorTest extends UITestCase {

	protected IProject testProject;

	protected IFolder testFolder;

	protected IFile testFile;

	protected IViewPart navigator;

	public AbstractNavigatorTest(String testName) {
		super(testName);
	}

	protected void createTestProject() throws CoreException {
		if (testProject == null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			testProject = workspace.getRoot().getProject("TestProject");
			testProject.create(null);
			testProject.open(null);
		}
	}

	protected void createTestFolder() throws CoreException {
		if (testFolder == null) {
			createTestProject();
			testFolder = testProject.getFolder("TestFolder");
			testFolder.create(false, false, null);
		}
	}

	protected void createTestFile() throws CoreException {
		if (testFile == null) {
			createTestFolder();
			testFile = testFolder.getFile("Foo.txt");
			testFile.create(
					new ByteArrayInputStream("Some content.".getBytes()),
					false, null);
		}
	}

	/** Shows the Navigator in a new test window. */
	protected void showNav() throws PartInitException {
		IWorkbenchWindow window = openTestWindow();
		navigator = window.getActivePage().showView(IPageLayout.ID_PROJECT_EXPLORER);
	}

	@Override
	protected void doTearDown() throws Exception {
		if (testProject != null) {
			testProject.delete(true, null);
			testProject = null;
			testFolder = null;
			testFile = null;
		}
		super.doTearDown();
		navigator = null;
	}

}
