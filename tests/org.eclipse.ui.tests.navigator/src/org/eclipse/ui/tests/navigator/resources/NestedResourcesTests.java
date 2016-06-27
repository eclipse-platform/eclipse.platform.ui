/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.navigator.resources.nested.NestedProjectManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class NestedResourcesTests {

	private Set<IProject> testProjects = new HashSet<>();

	@Test
	public void testProjectHierarchy() throws Exception {
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject projectA = root.getProject("a");
		projectA.create(monitor);
		projectA.open(monitor);
		IFolder folderAA = projectA.getFolder("aa");
		folderAA.create(true, true, monitor);
		IProjectDescription projectAAADesc = root.getWorkspace().newProjectDescription("aaa");
		projectAAADesc.setLocation(folderAA.getLocation().append(projectAAADesc.getName()));
		IProject projectAAA = root.getProject(projectAAADesc.getName());
		projectAAA.create(projectAAADesc, monitor);
		projectAAA.open(monitor);
		IProjectDescription projectABDesc = projectA.getWorkspace().newProjectDescription("ab");
		projectABDesc.setLocation(projectA.getLocation().append(projectABDesc.getName()));
		IProject projectAB = projectA.getWorkspace().getRoot().getProject(projectABDesc.getName());
		projectAB.create(projectABDesc, monitor);
		projectAB.open(monitor);
		IProjectDescription projectABADesc = projectAB.getWorkspace().newProjectDescription("aba");
		projectABADesc.setLocation(projectAB.getLocation().append(projectABADesc.getName()));
		IProject projectABA = root.getProject(projectABADesc.getName());
		projectABA.create(projectABADesc, monitor);
		projectABA.open(monitor);
		IProjectDescription projectABBDesc = projectAB.getWorkspace().newProjectDescription("abb");
		projectABBDesc.setLocation(projectAB.getLocation().append(projectABBDesc.getName()));
		IProject projectABB = root.getProject(projectABBDesc.getName());
		projectABB.create(projectABBDesc, monitor);
		projectABB.open(monitor);
		projectAB.getFolder("abc").create(true, true, monitor);

		testProjects.add(projectA);
		testProjects.add(projectAAA);
		testProjects.add(projectAB);
		testProjects.add(projectABA);
		testProjects.add(projectABB);

		IProject[] childrenOfProjectA = NestedProjectManager.getInstance().getDirectChildrenProjects(projectA);
		Assert.assertEquals(1, childrenOfProjectA.length);
		Assert.assertEquals(projectAB, childrenOfProjectA[0]);
		Assert.assertNull(NestedProjectManager.getInstance().getMostDirectOpenContainer(projectA));

		IProject[] childrenOfFolderAA = NestedProjectManager.getInstance().getDirectChildrenProjects(folderAA);
		Assert.assertEquals(1, childrenOfFolderAA.length);
		Assert.assertEquals("aaa", childrenOfFolderAA[0].getName());
		Assert.assertEquals(folderAA,
				NestedProjectManager.getInstance().getMostDirectOpenContainer(childrenOfFolderAA[0]));

		IProject[] childrenOfProjectAB = NestedProjectManager.getInstance().getDirectChildrenProjects(projectAB);
		Assert.assertEquals(2, childrenOfProjectAB.length);
	}

	@Test
	public void testDashInProject() throws Exception {
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		//
		IProject projectA = root.getProject("a");
		projectA.create(monitor);
		projectA.open(monitor);
		//
		IProjectDescription projectAChildDesc = root.getWorkspace().newProjectDescription("child");
		projectAChildDesc.setLocation(projectA.getLocation().append(projectAChildDesc.getName()));
		IProject projectAChild = root.getProject(projectAChildDesc.getName());
		projectAChild.create(projectAChildDesc, monitor);
		projectAChild.open(monitor);
		//
		IProject projectA_A = root.getProject("a-a");
		projectA_A.create(monitor);
		projectA_A.open(monitor);
		// Built projects in a/, a/child/ and a-a/
		testProjects.add(projectA);
		testProjects.add(projectAChild);
		testProjects.add(projectA_A);

		Assert.assertTrue(NestedProjectManager.getInstance().hasDirectChildrenProjects(projectA));
		Assert.assertEquals(projectAChild, NestedProjectManager.getInstance().getDirectChildrenProjects(projectA)[0]);
	}

	@After
	public void deleteProjects() throws Exception {
		IProgressMonitor monitor = new NullProgressMonitor();
		for (IProject testProject : testProjects) {
			testProject.delete(true, true, monitor);
		}
		testProjects.clear();
	}

}
