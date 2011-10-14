/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.model.AllRootsElement;
import org.eclipse.team.internal.ccvs.ui.model.BranchCategory;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.model.RemoteModule;
import org.eclipse.team.internal.ccvs.ui.model.VersionCategory;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class RepositoriesViewTests extends EclipseTest {

	public RepositoriesViewTests(String testName) {
		super(testName);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// clear repository root cache
		RepositoryRoot repositoryRoot = getRepositoryRoot();
		String remotePaths[] = repositoryRoot.getKnownRemotePaths();
		for (int i = 0; i < remotePaths.length; i++) {
			repositoryRoot.removeTags(remotePaths[i],
					repositoryRoot.getAllKnownTags(remotePaths[i]));
		}
	}

	private RepositoryRoot getRepositoryRoot() {
		RemoteContentProvider rcp = new RemoteContentProvider();
		AllRootsElement are = new AllRootsElement();
		Object[] repositoryRoots = rcp.getElements(are);
		for (int i = 0; i < repositoryRoots.length; i++) {
			RepositoryRoot repositoryRoot = (RepositoryRoot) repositoryRoots[i];
			if (getRepository().equals(repositoryRoot.getRoot())) {
				return repositoryRoot;
			}
		}
		fail(getRepository() + " not found");
		return null;
	}

	public static Test suite() {
		String testName = System.getProperty("eclipse.cvs.testName");
		if (testName == null) {
			TestSuite suite = new TestSuite(RepositoriesViewTests.class);
			return new CVSTestSetup(suite);
		} else {
			return new CVSTestSetup(new RepositoriesViewTests(testName));
		}
	}

	public void testBranchSubmoduleChildren() throws TeamException,
			CoreException {

		String time = Long.toString(System.currentTimeMillis());
		String moduleName = "TestBranchSubmoduleChildrenTestModule" + time;
		String branchName = "TestBranchSubmoduleChildrenBranch" + time;
		String versionName = "Root_" + branchName;

		// create project
		IProject project = getUniqueTestProject("TestBranchSubmoduleChildrenProject");
		buildResources(project, new String[] { "file1.txt" }, true);
		// share project under module
		shareProject(getRepository(), project,
				moduleName + "/" + project.getName(), DEFAULT_MONITOR);
		assertValidCheckout(project);

		// make some changes
		addResources(project, new String[] { "folder1/c.txt" }, false);

		// make branch
		CVSTag version = new CVSTag(versionName, CVSTag.VERSION);
		CVSTag branch = new CVSTag(branchName, CVSTag.BRANCH);

		makeBranch(new IResource[] { project }, version, branch, true);
		commitProject(project);

		// refresh branches
		CVSUIPlugin
				.getPlugin()
				.getRepositoryManager()
				.refreshDefinedTags(
						getRepository().getRemoteFolder(moduleName, null),
						true, true, DEFAULT_MONITOR);

		// check if module is the only branch child
		RemoteContentProvider rcp = new RemoteContentProvider();
		Object[] categories = rcp.getChildren(getRepositoryRoot());
		assertEquals(4, categories.length);
		assertTrue(categories[1] instanceof BranchCategory);
		Object[] branches = rcp.getChildren(categories[1]);
		assertEquals(1, branches.length);
		assertEquals(branchName, ((CVSTagElement) (branches[0])).getTag()
				.getName());
		Object[] modules = rcp.getChildren(branches[0]);
		assertEquals(1, modules.length);
		assertEquals(moduleName, ((RemoteResource) modules[0]).getName());
	}

	public void testTagSubmoduleChildren() throws TeamException, CoreException {

		String time = Long.toString(System.currentTimeMillis());
		String moduleName = "TestTagSubmoduleChildrenTestModule" + time;
		String versionName = "TestTagSubmoduleChildrenBranch" + time;

		// create project
		IProject project = getUniqueTestProject("TestTagSubmoduleChildrenProject");
		buildResources(project, new String[] { "file1.txt" }, true);
		// share project under module
		shareProject(getRepository(), project,
				moduleName + "/" + project.getName(), DEFAULT_MONITOR);
		assertValidCheckout(project);

		// tag project
		CVSTag tag = new CVSTag(versionName, CVSTag.VERSION);

		tagProject(project, tag, true);

		RemoteContentProvider rcp = new RemoteContentProvider();
		Object[] categories = rcp.getChildren(getRepositoryRoot());
		assertEquals(4, categories.length);

		// check if version exists for module
		assertTrue(categories[2] instanceof VersionCategory);
		Object[] modules = rcp.getChildren(categories[2]);
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] instanceof RemoteModule
					&& ((RemoteModule) (modules[i])).getCVSResource().getName()
							.equals(moduleName)) {
				Object folders[] = rcp.getChildren(modules[i]);
				assertEquals(1, folders.length);
				assertEquals(versionName, ((RemoteFolder) folders[0]).getTag()
						.getName());
				return;
			}
		}
		fail(moduleName + " not found");
	}
}
