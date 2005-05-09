/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import junit.framework.Test;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ProjectPreferenceSessionTest extends WorkspaceSessionTest {

	private static final String DIR_NAME = ".settings";
	private static final String FILE_EXTENSION = ".prefs";

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, ProjectPreferenceSessionTest.class);
		//		return new ProjectPreferenceSessionTest("testDeleteFileBeforeLoad2");
	}

	public ProjectPreferenceSessionTest(String name) {
		super(name);
	}

	public void testDeleteFileBeforeLoad1() {
		IProject project = getProject("testDeleteFileBeforeLoad");
		String qualifier = "test.delete.file.before.load";
		ensureExistsInWorkspace(project, true);
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode(qualifier);
		node.put("key", "value");
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
	}

	public void testDeleteFileBeforeLoad2() {
		IProject project = getProject("testDeleteFileBeforeLoad");
		String qualifier = "test.delete.file.before.load";
		IFile file = project.getFile(new Path(DIR_NAME).append(qualifier).addFileExtension(FILE_EXTENSION));
		assertTrue("1.0", project.exists());
		Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
		try {
			file.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	/*
	 * Test saving a key/value pair in one session and then ensure that they exist
	 * in the next session.
	 */
	public void testSaveLoad1() {
		IProject project = getProject("testSaveLoad");
		ensureExistsInWorkspace(project, true);
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode("test.save.load");
		node.put("key", "value");
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
	}

	public void testSaveLoad2() {
		IProject project = getProject("testSaveLoad");
		IScopeContext context = new ProjectScope(project);
		Preferences node = context.getNode("test.save.load");
		assertEquals("1.0", "value", node.get("key", null));
	}

	private static IProject getProject(String name) {
		return getWorkspace().getRoot().getProject(name);
	}
}
