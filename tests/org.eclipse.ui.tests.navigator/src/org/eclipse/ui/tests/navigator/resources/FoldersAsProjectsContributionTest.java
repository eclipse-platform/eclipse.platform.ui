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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.navigator.resources.actions.FoldersAsProjectsActionProvider;
import org.eclipse.ui.internal.navigator.resources.actions.OpenFolderAsProjectAction;
import org.eclipse.ui.internal.navigator.resources.actions.SelectProjectForFolderAction;
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
		assertFalse("SelectProjectForFolderAction contributions were added on not an adaptable-to-IFolder selection",
				contributionAdded(SelectProjectForFolderAction.class));
		assertFalse("OpenFolderAsProjectAction contributions were added on not an adaptable-to-IFolder selection",
				contributionAdded(OpenFolderAsProjectAction.class));
	}

	@Test
	public void noDescription() {
		IFolder justAFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path("some/folder"));
		provider(new StructuredSelection(justAFolder)).fillContextMenu(manager);
		assertFalse("SelectProjectForFolderAction contributions were added on an IFolder without project description",
				contributionAdded(SelectProjectForFolderAction.class));
		assertFalse("OpenFolderAsProjectAction contributions were added on an IFolder without project description",
				contributionAdded(OpenFolderAsProjectAction.class));
	}

	@Test
	public void alreadyAdded() throws CoreException {
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertTrue(NLS.bind("A SelectProjectForFolderAction contribution was not added. Contribution List is: {0}",
				contributionsList()), contributionAdded(SelectProjectForFolderAction.class));
	}

	@Test
	public void notYetImported() throws CoreException {
		workspace.deleteLeavingContents("inner1");
		workspace.deleteLeavingContents("inner2");
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertTrue(NLS.bind("A OpenFolderAsProjectAction contribution was not added. Contribution List is: {0}",
				contributionsList()), contributionAdded(OpenFolderAsProjectAction.class));
	}

	@Test
	public void ambiguity() throws CoreException {
		workspace.deleteLeavingContents("inner1");
		provider(new StructuredSelection(projectTree())).fillContextMenu(manager);
		assertFalse(
				"There were both imported and not-imported projects in selection, but SelectProjectForFolderAction contributions were added",
				contributionAdded(SelectProjectForFolderAction.class));
		assertFalse(
				"There were both imported and not-imported projects in selection, but OpenFolderAsProjectAction contributions were added",
				contributionAdded(OpenFolderAsProjectAction.class));
	}

	@After
	public void clean() throws CoreException {
		manager.removeAll();
		workspace.clear();
	}

	private boolean contributionAdded(Class<?> action) {
		return Stream.of(manager.getItems()) //
				.filter(ActionContributionItem.class::isInstance) //
				.map(ActionContributionItem.class::cast) //
				.map(ActionContributionItem::getAction) //
				.filter(action::isInstance) //
				.findFirst().isPresent();
	}

	private FoldersAsProjectsActionProvider provider(StructuredSelection selection) {
		FoldersAsProjectsActionProvider provider = new FoldersAsProjectsActionProvider();
		provider.setContext(new ActionContext(selection));
		return provider;
	}

	private List<IFolder> projectTree() throws CoreException {
		return Arrays.asList( //
				ResourcesPlugin.getWorkspace().getRoot().getProject("outer").getFolder("inner1"), //
				ResourcesPlugin.getWorkspace().getRoot().getProject("outer").getFolder("inner2"));
	}

	private String contributionsList() {
		return Stream.of(manager.getItems()).map(IContributionItem::getClass).map(Class::getName)
				.collect(Collectors.joining(","));
	}

}
