/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class Bug_61277 extends EclipseWorkspaceTest {

	public static Test suite() {
		return new TestSuite(Bug_61277.class);
	}

	public Bug_61277() {
		super();
	}

	public Bug_61277(String name) {
		super(name);
	}

	/**
	 * Bug 61277 - preferences and project moves
	 *
	 * Investigate what happens with project preferences when the
	 * project is moved.
	 */
	public void testBug() {
		IProject project = getWorkspace().getRoot().getProject("source");
		IProject destProject = getWorkspace().getRoot().getProject("dest");
		ensureExistsInWorkspace(project, true);
		ensureDoesNotExistInWorkspace(destProject);
		IScopeContext context = new ProjectScope(project);
		String qualifier = getUniqueString();
		Preferences node = context.getNode(qualifier);
		String key = getUniqueString();
		String value = getUniqueString();
		node.put(key, value);
		assertEquals("1.0", value, node.get(key, null));

		try {
			// save the prefs
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.1", e);
		}

		// rename the project
		try {
			project.move(destProject.getFullPath(), true, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		context = new ProjectScope(destProject);
		node = context.getNode(qualifier);
		assertEquals("3.0", value, node.get(key, null));
	}

	public void testPrefsMovingProject() {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject("Project1");
		IProject project2 = workspace.getRoot().getProject("Project2");
		ensureExistsInWorkspace(new IResource[] {project1}, true);
		Preferences node = new ProjectScope(project1).getNode(ResourcesPlugin.PI_RESOURCES);
		node.put("key", "value");
		assertTrue("1.0", !project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.1", project1.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		// move project and ensures charsets settings are preserved
		try {
			project1.move(project2.getFullPath(), false, null);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		assertTrue("2.0", project2.getFolder(".settings").getFile(ResourcesPlugin.PI_RESOURCES + ".prefs").exists());
		node = new ProjectScope(project2).getNode(ResourcesPlugin.PI_RESOURCES);
		assertEquals("2.1", "value", node.get("key", null));
	}

}