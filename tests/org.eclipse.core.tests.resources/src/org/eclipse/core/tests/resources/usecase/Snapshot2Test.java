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
package org.eclipse.core.tests.resources.usecase;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * This session add some resources to MyProject. All resources
 * from Project2 are removed and some resources are added to it
 * but not written to disk (only in the tree).
 * Only snapshots are taken. No full saves.
 */
public class Snapshot2Test extends SnapshotTest {
	public Snapshot2Test() {
		super();
	}

	public Snapshot2Test(String name) {
		super(name);
	}

	protected static String[] defineHierarchy1() {
		List result = new ArrayList();
		String[] old = Snapshot1Test.defineHierarchy1();
		for (int i = 0; i < old.length; i++)
			result.add(old[i]);
		result.add(new Path(PROJECT_1).append("added file").toString());
		result.add(new Path(PROJECT_1).append("yet another file").toString());
		result.add(new Path(PROJECT_1).append("a folder").addTrailingSeparator().toString());
		return (String[]) result.toArray(new String[result.size()]);
	}

	protected static String[] defineHierarchy2() {
		return new String[] {"/added file", "/yet another file", "/a folder/"};
	}

	public void testChangeMyProject() {
		// MyProject
		IProject project = getWorkspace().getRoot().getProject(PROJECT_1);
		assertTrue("0.1", project.exists());
		assertTrue("0.2", project.isOpen());

		// create some children
		IResource[] resources = buildResources(project, defineHierarchy1());
		ensureExistsInWorkspace(resources, true);
		assertExistsInFileSystem("1.1", resources);
		assertExistsInWorkspace("1.2", resources);
	}

	public void testChangeProject2() {
		IProject project = getWorkspace().getRoot().getProject("Project2");
		assertTrue("0.1", project.exists());
		assertTrue("0.2", project.isOpen());

		// remove all resources
		try {
			IResource[] children = project.members();
			getWorkspace().delete(children, true, null);
		} catch (CoreException e) {
			fail("0.5", e);
		}

		// create some children
		IResource[] resources = buildResources(project, defineHierarchy2());
		ensureExistsInWorkspace(resources, true);
		assertExistsInFileSystem("1.1", resources);
		assertExistsInWorkspace("1.2", resources);
	}

	public void testSnapshotWorkspace() {
		try {
			getWorkspace().save(false, null);
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	public void testVerifyPreviousSession() {
		// MyProject
		IProject project = getWorkspace().getRoot().getProject(PROJECT_1);
		assertTrue("0.0", project.exists());
		assertTrue("0.1", !project.isOpen());

		try {
			project.open(null);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertTrue("1.2", project.isOpen());

		// verify existence of children
		IResource[] resources = buildResources(project, Snapshot1Test.defineHierarchy1());
		assertExistsInFileSystem("2.1", resources);
		assertExistsInWorkspace("2.2", resources);

		// Project2
		project = getWorkspace().getRoot().getProject(PROJECT_2);
		assertTrue("3.0", project.exists());
		assertTrue("3.1", project.isOpen());

		// verify existence of children
		resources = buildResources(project, Snapshot1Test.defineHierarchy2());
		assertExistsInFileSystem("5.1", resources);
		assertExistsInWorkspace("5.2", resources);
	}
}
