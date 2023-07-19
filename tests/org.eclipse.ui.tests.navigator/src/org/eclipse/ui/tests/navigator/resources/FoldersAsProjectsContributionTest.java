/*******************************************************************************
 * Copyright (c) 2023 ArSysOp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nikifor Fedorov (ArSysOp) - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.navigator.resources.actions.FoldersAsProjectsActionProvider;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class FoldersAsProjectsContributionTest {

	private final IMenuManager manager = new MenuManager();
	private final ProjectsStructure workspace = new ProjectsStructure.Imported();

	@Before
	public void prepare() throws CoreException {
		manager.add(new GroupMarker(ICommonMenuConstants.GROUP_OPEN));
		manager.add(new GroupMarker(ICommonMenuConstants.GROUP_PORT));
		workspace.create("outer", "inner1", "inner2");
	}

	@Test
	public void notAFolder() {
		String notAFolder = "Some string";
		provider(new StructuredSelection(notAFolder)).fillContextMenu(manager);
		assertFalse(contributionAdded(manager));
	}

	@Test
	public void noDescription() {
		IFolder justAFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path("some/folder"));
		provider(new StructuredSelection(justAFolder)).fillContextMenu(manager);
		assertFalse(contributionAdded(manager));
	}

	@Test
	public void alreadyAdded() throws CoreException {
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertTrue(contributionAdded(manager));
	}

	@Test
	public void notYetImported() throws CoreException {
		workspace.deleteLeavingContents("inner1");
		workspace.deleteLeavingContents("inner2");
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertTrue(contributionAdded(manager));
	}

	@Test
	public void ambiguity() throws CoreException {
		workspace.deleteLeavingContents("inner1");
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertFalse(contributionAdded(manager));
	}

	@After
	public void clean() throws CoreException {
		manager.removeAll();
		workspace.clear();
	}

	private FoldersAsProjectsActionProvider provider(StructuredSelection selection) {
		FoldersAsProjectsActionProvider provider = new FoldersAsProjectsActionProvider();
		provider.setContext(new ActionContext(selection));
		return provider;
	}

	private boolean contributionAdded(IMenuManager manager) {
		return manager.getItems().length > 2;
	}

	private List<IFolder> projectTree() throws CoreException {
		return Arrays.asList( //
				ResourcesPlugin.getWorkspace().getRoot().getProject("outer").getFolder("inner1"), //
				ResourcesPlugin.getWorkspace().getRoot().getProject("outer").getFolder("inner2"));
	}

}
