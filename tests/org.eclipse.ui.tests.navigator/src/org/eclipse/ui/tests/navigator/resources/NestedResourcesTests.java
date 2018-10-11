/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.navigator.resources.nested.NestedProjectManager;
import org.eclipse.ui.internal.navigator.resources.nested.NestedProjectsLabelProvider;
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

	private class NestedProjectsLabelProviderAccessor extends NestedProjectsLabelProvider {
		@Override
		public int getHighestProblemSeverity(IResource resource) {
			return super.getHighestProblemSeverity(resource);
		}
	}

	@Test
	public void testProblemDecoration() throws Exception {
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject parentProject = root.getProject("parent");
		parentProject.create(monitor);
		parentProject.open(monitor);
		IFolder intermediaryFolder = parentProject.getFolder("folder");
		intermediaryFolder.create(true, false, monitor);
		IProject firstChildProject = root.getProject("child1");
		IProjectDescription description = root.getWorkspace().newProjectDescription(firstChildProject.getName());
		description.setLocation(intermediaryFolder.getLocation().append(firstChildProject.getName()));
		firstChildProject.create(description, monitor);
		firstChildProject.open(monitor);
		IProject secondChildProject = root.getProject("child2");
		description = root.getWorkspace().newProjectDescription(secondChildProject.getName());
		description.setLocation(intermediaryFolder.getLocation().append(secondChildProject.getName()));
		secondChildProject.create(description, monitor);
		secondChildProject.open(monitor);
		//
		NestedProjectsLabelProviderAccessor labelProvider = new NestedProjectsLabelProviderAccessor();
		labelProvider.init(null);
		assertEquals(-1, labelProvider.getHighestProblemSeverity(parentProject));
		//
		root.getWorkspace().run(aMonitor -> {
			IMarker marker = firstChildProject.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		}, monitor);
		assertEquals(IMarker.SEVERITY_WARNING, labelProvider.getHighestProblemSeverity(parentProject));
		//
		root.getWorkspace().run(aMonitor -> {
			IMarker marker = secondChildProject.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}, monitor);
		assertEquals(IMarker.SEVERITY_ERROR, labelProvider.getHighestProblemSeverity(parentProject));
		//
		for (IMarker marker : secondChildProject.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
			marker.delete();
		}
		assertEquals(IMarker.SEVERITY_WARNING, labelProvider.getHighestProblemSeverity(parentProject));
		//
		root.getWorkspace().run(aMonitor -> {
			IMarker marker = secondChildProject.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}, monitor);
		assertEquals(IMarker.SEVERITY_ERROR, labelProvider.getHighestProblemSeverity(parentProject));
		secondChildProject.close(monitor);
		assertEquals(IMarker.SEVERITY_WARNING, labelProvider.getHighestProblemSeverity(parentProject));
		secondChildProject.open(monitor);
		assertEquals(IMarker.SEVERITY_ERROR, labelProvider.getHighestProblemSeverity(parentProject));
		//
		root.getWorkspace().run(aMonitor -> {
			IMarker marker = parentProject.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			secondChildProject.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)[0].delete();
		}, monitor);
		assertEquals(IMarker.SEVERITY_ERROR, labelProvider.getHighestProblemSeverity(parentProject));
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
