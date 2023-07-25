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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
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
import org.junit.Test;

public final class FoldersAsProjectsContributionTest {

	@Test
	public void notAFolder() {
		String notAFolder = "Some string";
		IMenuManager manager = menuManager();
		provider(new StructuredSelection(notAFolder)).fillContextMenu(manager);
		assertFalse("SelectProjectForFolderAction contributions were added on not an adaptable-to-IFolder selection",
				contributionAdded(manager, SelectProjectForFolderAction.class));
		assertFalse("OpenFolderAsProjectAction contributions were added on not an adaptable-to-IFolder selection",
				contributionAdded(manager, OpenFolderAsProjectAction.class));
	}

	@Test
	public void noDescription() {
		IFolder justAFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path("some/folder"));
		IMenuManager manager = menuManager();
		provider(new StructuredSelection(justAFolder)).fillContextMenu(manager);
		assertFalse("SelectProjectForFolderAction contributions were added on an IFolder without project description",
				contributionAdded(manager, SelectProjectForFolderAction.class));
		assertFalse("OpenFolderAsProjectAction contributions were added on an IFolder without project description",
				contributionAdded(manager, OpenFolderAsProjectAction.class));
	}

	@Test
	public void alreadyAdded() {
		IProject outer = handle("foldersasprojects.alreadyAdded.outer");
		IProject inner1 = handle("foldersasprojects.alreadyAdded.inner1");
		IProject inner2 = handle("foldersasprojects.alreadyAdded.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);
		try {
			createProject(null, outer);
			createProject(outer, inner1);
			createProject(outer, inner2);
		} catch (CoreException e) {
			fail(NLS.bind("Required projects can not be created due to: {0}", e.getMessage()));
		}

		try {
			Job.getJobManager().beginRule(rule, null);
			IMenuManager manager = menuManager();
			provider(new StructuredSelection(projectTree("alreadyAdded"))).fillContextMenu(manager);
			assertTrue(
					NLS.bind("A SelectProjectForFolderAction contribution was not added. Contribution List is: {0}",
							contributionsList(manager)),
					contributionAdded(manager, SelectProjectForFolderAction.class));
		} finally {
			projects.forEach(handle -> {
				try {
					handle.delete(true, new NullProgressMonitor());
				} catch (CoreException e) {
					// Ignore
				}
			});
			Job.getJobManager().endRule(rule);
		}
	}

	@Test
	public void notYetImported() {
		IProject outer = handle("foldersasprojects.notYetImported.outer");
		IProject inner1 = handle("foldersasprojects.notYetImported.inner1");
		IProject inner2 = handle("foldersasprojects.notYetImported.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);

		try {
			createProject(null, outer);
			createProject(outer, inner1);
			createProject(outer, inner2);
			inner1.delete(false, true, new NullProgressMonitor());
			inner2.delete(false, true, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(NLS.bind("Required projects can not be created due to: {0}", e.getMessage()));
		}
		try {
			Job.getJobManager().beginRule(rule, null);
			IMenuManager manager = menuManager();
			provider(new StructuredSelection(projectTree("notYetImported"))).fillContextMenu(manager);
			assertTrue(NLS.bind("A OpenFolderAsProjectAction contribution was not added. Contribution List is: {0}",
					contributionsList(manager)), contributionAdded(manager, OpenFolderAsProjectAction.class));
		} finally {
			projects.forEach(handle -> {
				try {
					handle.delete(true, new NullProgressMonitor());
				} catch (CoreException e) {
					// Ignore
				}
			});
			Job.getJobManager().endRule(rule);
		}
	}

	@Test
	public void ambiguity() {
		IProject outer = handle("foldersasprojects.ambiguity.outer");
		IProject inner1 = handle("foldersasprojects.ambiguity.inner1");
		IProject inner2 = handle("foldersasprojects.ambiguity.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);

		try {
			createProject(null, outer);
			createProject(outer, inner1);
			createProject(outer, inner2);
			inner1.delete(false, true, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(NLS.bind("Required projects can not be created due to: {0}", e.getMessage()));
		}
		try {
			Job.getJobManager().beginRule(rule, null);
			IMenuManager manager = menuManager();
			provider(new StructuredSelection(projectTree("ambiguity"))).fillContextMenu(manager);
			assertFalse(
					"There were both imported and not-imported projects in selection, but SelectProjectForFolderAction contributions were added",
					contributionAdded(manager, SelectProjectForFolderAction.class));
			assertFalse(
					"There were both imported and not-imported projects in selection, but OpenFolderAsProjectAction contributions were added",
					contributionAdded(manager, OpenFolderAsProjectAction.class));
		} finally {
			projects.forEach(handle -> {
				try {
					handle.delete(true, new NullProgressMonitor());
				} catch (CoreException e) {
					// Ignore
				}
			});
			Job.getJobManager().endRule(rule);
		}
	}

	private IProject handle(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	private IMenuManager menuManager() {
		IMenuManager menuManager = new MenuManager();
		menuManager.add(new GroupMarker(ICommonMenuConstants.GROUP_OPEN));
		menuManager.add(new GroupMarker(ICommonMenuConstants.GROUP_PORT));
		return menuManager;
	}

	private boolean contributionAdded(IMenuManager manager, Class<?> action) {
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

	private List<IFolder> projectTree(String prefix) {
		String outer = String.format("foldersasprojects.%s.outer", prefix);
		String inner1 = String.format("foldersasprojects.%s.inner1", prefix);
		String inner2 = String.format("foldersasprojects.%s.inner2", prefix);
		return Arrays.asList(handle(outer).getFolder(inner1), handle(outer).getFolder(inner2));
	}

	private String contributionsList(IMenuManager manager) {
		return Stream.of(manager.getItems()) //
				.map(IContributionItem::getClass) //
				.map(Class::getName) //
				.collect(Collectors.joining(","));
	}

	private void createProject(IProject parent, IProject actual) throws CoreException {
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(actual.getName());
		if (parent != null) {
			description.setLocation(parent.getLocation().append(actual.getName()));
		}
		actual.create(description, new NullProgressMonitor());
		actual.open(new NullProgressMonitor());
	}

}
