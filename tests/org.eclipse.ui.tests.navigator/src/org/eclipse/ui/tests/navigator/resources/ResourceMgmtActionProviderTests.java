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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
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
	 * Test for a file selected together with an open project: Close Project must
	 * be both present and enabled. Regression test for the bug where
	 * selectionIsOfType(PROJECT) disabled the action for any mixed selection.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testFillContextMenu_fileAndOpenProjectSelection_closeProjectEnabled() throws CoreException {
		// _p1 is already open; _project has a known 'src' folder + files
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		// Select a file alongside a project (the typical Ctrl+A expanded scenario)
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
	 * Test for mixed selection: an open project alongside a non-adaptable element
	 * (e.g. a working set header from Ctrl+A in Project Explorer). Close Project
	 * and Refresh must still appear — regression test for issue #3790.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testFillContextMenu_mixedSelectionOpenProjectAndNonAdaptableElement() throws CoreException {
		IProject openProj = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		openProj.open(null);
		// Plain Object does not implement IAdaptable, so it is never resolved to a
		// project — it counts as a non-project element in the selection.
		Object nonResource = new Object();
		ResourceMgmtActionProvider provider = providerForObjects(openProj, nonResource);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, true, false, true, true);
	}

	/**
	 * Test for a fully expanded selection: two open projects plus child resources
	 * from both (simulating Ctrl+A when both projects are expanded). Close Project
	 * must still appear for the open projects in the selection.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testFillContextMenu_twoOpenProjectsWithChildResourcesSelection() throws CoreException {
		// _p1 and _p2 are already opened in setUp()
		IFolder srcFolder = _project.getFolder("src");
		IFolder binFolder = _project.getFolder("bin");
		ResourceMgmtActionProvider provider = providerForObjects(_p1, _p2, srcFolder, binFolder);
		provider.fillContextMenu(manager);
		checkMenuHasCorrectContributions(false, true, false, true, true);
	}

	/**
	 * Regression test for the ClassCastException that the always-on provider
	 * enablement could expose: when Close Project is invoked on a mixed selection
	 * (project plus a file plus a non-resource element, as produced by Ctrl+A),
	 * the action must reduce the selection to projects only. Otherwise run() casts
	 * every selected resource to IProject while building the scheduling rule.
	 *
	 * @throws CoreException
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
	 * Regression test for the ClassCastException in OpenResourceAction on a mixed
	 * selection: the action must reduce the selection to projects only, otherwise
	 * hasOtherClosedProjects() casts a non-project resource to IProject while
	 * opening projects with their references.
	 *
	 * @throws CoreException
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
