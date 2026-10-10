/*******************************************************************************
 * Copyright (c) 2025 Dave Carpeneto and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.internal.ide.misc.ProjectReferenceGraph;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.extensions.CommonActionExtensionSite;
import org.eclipse.ui.internal.navigator.resources.actions.ResourceMgmtActionProvider;
import org.eclipse.ui.navigator.CommonViewerSiteFactory;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.tests.navigator.NavigatorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class ResourceMgmtActionProviderTests extends NavigatorTestBase {

	private IMenuManager manager;

	public ResourceMgmtActionProviderTests() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	@Override
	@BeforeEach
	public void setUp() throws CoreException {
		super.setUp();
		manager = new MenuManager();
		manager.add(new GroupMarker(ICommonMenuConstants.GROUP_BUILD));
	}

	/**
	 * Test for 'no selection' condition - no menu items should be included
	 */
	@Test
	public void testFillContextMenu_noSelection() {
		ResourceMgmtActionProvider provider = provider((IResource[]) null);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, false, false, false, false);
	}

	/**
	 * Test for 'folder' condition - only 'refresh' should be included
	 */
	@Test
	public void testFillContextMenu_folderSelection() {

		IFolder justAFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path("some/folder"));
		ResourceMgmtActionProvider provider = provider(justAFolder);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, true, false, false, false);
	}

	/**
	 * Test for 'closed project' - only 'open project' should be included
	 */
	@Test
	public void testFillContextMenu_closedProjectSelection() {
		IProject closedProj = ResourcesPlugin.getWorkspace().getRoot().getProject("closedProj");
		ResourceMgmtActionProvider provider = provider(closedProj);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, false, true, false, false);
	}

	/**
	 * Test for 'open project' that doesn't have a builder attached - all but
	 * 'build' &amp; 'open project' should be enabled
	 *
	 * @throws CoreException
	 */
	@Test
	public void testFillContextMenu_openProjectNoBuilderSelection() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		boolean autoBuildInitialState = ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();

		if (!autoBuildInitialState) {
			// we want to enable auto-building for this test to guarantee that the 'build'
			// menu option isn't shown
			IWorkspaceDescription wsd = ResourcesPlugin.getWorkspace().getDescription();
			wsd.setAutoBuilding(true);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
		openProj.open(null);
		ResourceMgmtActionProvider provider = provider(openProj);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, true, false, true, true);

		if (!autoBuildInitialState) {
			// clean-up: reset autobuild since we changed it
			IWorkspaceDescription wsd = ResourcesPlugin.getWorkspace().getDescription();
			wsd.setAutoBuilding(false);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
	}

	/**
	 * A file selected together with an open project keeps Close Project present
	 * and enabled.
	 */
	@Test
	public void testFillContextMenu_fileAndOpenProjectSelection_closeProjectEnabled()
			throws CoreException, InterruptedException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		// Close Unrelated Projects answers its enablement from the cached graph
		assertTrue(ProjectReferenceGraph.getInstance().refresh(null),
				"the project reference graph did not become current");
		ResourceMgmtActionProvider provider = providerForObjects(_p1, openProj.getFile(".project"));
		provider.fillContextMenu(manager);
		assertTrue(menuHasContribution("org.eclipse.ui.CloseResourceAction"),
				"Close Project should be in the menu");
		assertTrue(isMenuContributionEnabled("org.eclipse.ui.CloseResourceAction"),
				"Close Project should be enabled when open projects are in the selection");
		assertTrue(menuHasContribution("org.eclipse.ui.CloseUnrelatedProjectsAction"),
				"Close Unrelated Projects should be in the menu");
		assertTrue(isMenuContributionEnabled("org.eclipse.ui.CloseUnrelatedProjectsAction"),
				"Close Unrelated Projects should be enabled when open projects are in the selection");
	}

	/**
	 * An open project selected together with a non-resource element, such as a
	 * working set header, gets Close Project but not the Refresh it cannot run.
	 */
	@Test
	public void testFillContextMenu_mixedSelectionOpenProjectAndNonAdaptableElement() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		Object nonResource = new Object();
		ResourceMgmtActionProvider provider = providerForObjects(openProj, nonResource);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, false, false, true, true);
	}

	/**
	 * Mixed selections reach the provider through the navigator's action service,
	 * which evaluates its enablement from plugin.xml.
	 */
	@Test
	public void testActionService_mixedSelections() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		_p2.close(null);
		Object nonResource = new Object();

		assertTrue(actionServiceMenuHas(
				new StructuredSelection(new Object[] { openProj, openProj.getFile(".project"), nonResource }),
				CloseResourceAction.ID), "Close Project must be offered for an open project in a mixed selection");
		assertTrue(actionServiceMenuHas(new StructuredSelection(new Object[] { _p2, openProj.getFile(".project"), nonResource }),
				OpenResourceAction.ID), "Open Project must be offered for a closed project in a mixed selection");
	}

	/**
	 * A closed project in a mixed selection is what Open Project runs on.
	 */
	@Test
	public void testOpenResourceAction_closedProjectInMixedSelection() throws CoreException {
		_p2.close(null);
		IFile file = _project.getFile(".project");
		var action = new OpenResourceAction(() -> _commonNavigator.getViewSite().getShell()) {
			List<? extends IResource> exposedActionResources() {
				return getActionResources();
			}
		};
		action.selectionChanged(new StructuredSelection(new Object[] { _p2, file, new Object() }));

		assertTrue(action.isEnabled(), "Open Project must be enabled for a closed project in a mixed selection");
		assertEquals(List.of(_p2), action.exposedActionResources());
	}

	private boolean actionServiceMenuHas(IStructuredSelection selection, String actionId) {
		MenuManager menu = new MenuManager();
		menu.add(new GroupMarker(ICommonMenuConstants.GROUP_BUILD));
		_actionService.setContext(new ActionContext(selection));
		_actionService.fillContextMenu(menu);
		for (IContributionItem item : menu.getItems()) {
			if (actionId.equals(item.getId()) && item.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A selection without any resource or working set gets no entries.
	 */
	@Test
	public void testFillContextMenu_nonResourceSelectionOnly() {
		ResourceMgmtActionProvider provider = providerForObjects(new Object());
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, false, false, false, false);
	}

	/**
	 * Open projects selected together with their child folders still get Close
	 * Project.
	 */
	@Test
	public void testFillContextMenu_twoOpenProjectsWithChildResourcesSelection() throws CoreException {
		IFolder srcFolder = _project.getFolder("src");
		IFolder binFolder = _project.getFolder("bin");
		ResourceMgmtActionProvider provider = providerForObjects(_p1, _p2, srcFolder, binFolder);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, true, false, true, true);
	}

	/**
	 * Close Project runs on the projects of a mixed selection only.
	 */
	@Test
	public void testCloseResourceAction_actionResourcesContainProjectsOnly() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		IFile projectFile = openProj.getFile(".project");
		StructuredSelection mixed = new StructuredSelection(new Object[] { openProj, projectFile, new Object() });

		var action = new CloseResourceAction(() -> _commonNavigator.getViewSite().getShell()) {
			List<? extends IResource> exposedActionResources() {
				return getActionResources();
			}
		};
		action.selectionChanged(mixed);

		assertEquals(List.of(openProj), action.exposedActionResources(),
				"Close Project must operate on projects only, not files or non-resource elements");
	}

	/**
	 * Open Project runs on the projects of a mixed selection only.
	 */
	@Test
	public void testOpenResourceAction_actionResourcesContainProjectsOnly() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		IFile projectFile = openProj.getFile(".project");
		StructuredSelection mixed = new StructuredSelection(new Object[] { openProj, projectFile, new Object() });

		var action = new OpenResourceAction(() -> _commonNavigator.getViewSite().getShell()) {
			List<? extends IResource> exposedActionResources() {
				return getActionResources();
			}
		};
		action.selectionChanged(mixed);

		assertEquals(List.of(openProj), action.exposedActionResources(),
				"Open Project must operate on projects only, not files or non-resource elements");
	}

	/**
	 * Test for 'open project' that doesn't have a builder attached - only 'open
	 * project' should be disabled
	 *
	 * @throws CoreException
	 */
	@Test
	public void testFillContextMenu_openProjectWithBuilderSelection() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		IWorkspaceDescription wsd = ResourcesPlugin.getWorkspace().getDescription();
		boolean autobuildInitialState = wsd.isAutoBuilding();
		boolean hasNoInitialBuildCommands = false;
		IProjectDescription desc = null;
		if (autobuildInitialState) {
			wsd.setAutoBuilding(false);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
		openProj.open(null);
		desc = openProj.getDescription();
		if (desc.getBuildSpec().length == 0) {
			hasNoInitialBuildCommands = true;
			ICommand cmd = desc.newCommand();
			desc.setBuildSpec(new ICommand[] { cmd });
			openProj.setDescription(desc, null);
		}
		ResourceMgmtActionProvider provider = provider(openProj);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(true, true, false, true, true);
		// clean-up where needed: reset autobuild if we changed it & remove
		// the build config if we added it
		if (autobuildInitialState) {
			wsd.setAutoBuilding(true);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
		if (desc != null && hasNoInitialBuildCommands) {
			desc.setBuildSpec(new ICommand[0]);
			openProj.setDescription(desc, null);
		}
	}

	/*
	 * Return a provider for a mixed/arbitrary selection (Object[])
	 */
	private ResourceMgmtActionProvider providerForObjects(Object... selectedElements) {
		ICommonActionExtensionSite cfg = new CommonActionExtensionSite("NA", "NA",
				CommonViewerSiteFactory.createCommonViewerSite(_commonNavigator.getViewSite()),
				(NavigatorContentService) _contentService, _viewer);
		ResourceMgmtActionProvider provider = new ResourceMgmtActionProvider();
		provider.setContext(new ActionContext(new StructuredSelection(selectedElements)));
		provider.init(cfg);
		return provider;
	}

	/*
	 * Return a provider, given the selected navigator items
	 */
	private ResourceMgmtActionProvider provider(IResource... selectedElements) {
		ICommonActionExtensionSite cfg = new CommonActionExtensionSite("NA", "NA",
				CommonViewerSiteFactory.createCommonViewerSite(_commonNavigator.getViewSite()),
				(NavigatorContentService) _contentService, _viewer);
		ResourceMgmtActionProvider provider = new ResourceMgmtActionProvider();
		StructuredSelection selection = null;
		if (selectedElements != null && selectedElements.length > 0) {
			selection = new StructuredSelection(selectedElements);
		} else {
			selection = new StructuredSelection();
		}
		provider.setContext(new ActionContext(selection));
		provider.init(cfg);
		return provider;
	}

	/*
	 * Check the expected menu items (passed in) against what the menu actually has
	 */
	private void checkMenuHasCorrectContributions(boolean... actions) {
		if (actions.length != 5) { // there's 5 menus we check for
			fail(String.format("Incorrect number of menu items being checked : %d", actions.length));
		}
		int index = 0;
		for (String thisAction : new String[] { "org.eclipse.ui.BuildAction", "org.eclipse.ui.RefreshAction",
				"org.eclipse.ui.OpenResourceAction", "org.eclipse.ui.CloseResourceAction",
				"org.eclipse.ui.CloseUnrelatedProjectsAction" }) {
			assertTrue(actions[index] == menuHasContribution(thisAction),
					String.format("Unexpected menu membership for %s (%b)", thisAction, !actions[index]));
			index++;
		}
	}

	/*
	 * Check the menu for the named entry
	 */
	private boolean menuHasContribution(String contribution) {
		for (IContributionItem thisItem : manager.getItems()) {
			if (thisItem.getId() != null && thisItem.getId().equals(contribution)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Check whether the named menu entry is enabled
	 */
	private boolean isMenuContributionEnabled(String contribution) {
		for (IContributionItem thisItem : manager.getItems()) {
			if (thisItem.getId() != null && thisItem.getId().equals(contribution)) {
				return thisItem.isEnabled();
			}
		}
		return false;
	}

}
