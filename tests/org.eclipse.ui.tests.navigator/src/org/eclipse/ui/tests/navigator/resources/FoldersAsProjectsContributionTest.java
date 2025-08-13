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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class FoldersAsProjectsContributionTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

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
	@Ignore
	public void alreadyAdded() throws IOException, CoreException {
		IProject outer = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.alreadyAdded.outer");
		IProject inner1 = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.alreadyAdded.inner1");
		IProject inner2 = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.alreadyAdded.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);
		try {
			Job.getJobManager().beginRule(rule, null);
			File root = folder.newFolder(outer.getName());
			createProject(root, outer);
			createProject(new Path(root.getAbsolutePath()).append(inner1.getName()).toFile(), inner1);
			createProject(new Path(root.getAbsolutePath()).append(inner2.getName()).toFile(), inner2);
		} finally {
			Job.getJobManager().endRule(rule);
		}

		try {
			IMenuManager manager = menuManager();
			ensureDescriptionsExist(outer, inner1, inner2);
			provider(new StructuredSelection(
					Arrays.asList(outer.getFolder(inner1.getName()), outer.getFolder(inner2.getName()))))
							.fillContextMenu(manager);
			assertTrue(
					NLS.bind("A SelectProjectForFolderAction contribution was not added. Contribution List is: {0}",
							contributionsList(manager)),
					contributionAdded(manager, SelectProjectForFolderAction.class));
		} finally {
			clear(projects);
		}
	}

	@Test
	@Ignore
	public void notYetImported() throws IOException, CoreException {
		IProject outer = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.notYetImported.outer");
		IProject inner1 = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("foldersasprojects.notYetImported.inner1");
		IProject inner2 = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("foldersasprojects.notYetImported.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);
		try {
			Job.getJobManager().beginRule(rule, null);
			File root = folder.newFolder(outer.getName());
			createProject(root, outer);
			createProject(new Path(root.getAbsolutePath()).append(inner1.getName()).toFile(), inner1);
			createProject(new Path(root.getAbsolutePath()).append(inner2.getName()).toFile(), inner2);
			inner1.delete(false, true, new NullProgressMonitor());
			inner2.delete(false, true, new NullProgressMonitor());
		} finally {
			Job.getJobManager().endRule(rule);
		}
		try {
			IMenuManager manager = menuManager();
			ensureDescriptionsExist(outer, inner1, inner2);
			provider(new StructuredSelection(
					Arrays.asList(outer.getFolder(inner1.getName()), outer.getFolder(inner2.getName()))))
							.fillContextMenu(manager);
			assertTrue(NLS.bind("A OpenFolderAsProjectAction contribution was not added. Contribution List is: {0}",
					contributionsList(manager)), contributionAdded(manager, OpenFolderAsProjectAction.class));
		} finally {
			clear(projects);
		}
	}

	@Test
	public void ambiguity() throws CoreException, IOException {
		IProject outer = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.ambiguity.outer");
		IProject inner1 = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.ambiguity.inner1");
		IProject inner2 = ResourcesPlugin.getWorkspace().getRoot().getProject("foldersasprojects.ambiguity.inner2");
		List<IProject> projects = Arrays.asList(outer, inner1, inner2);
		ISchedulingRule rule = new AffectedProjectsSchedulingRule(projects);
		try {
			Job.getJobManager().beginRule(rule, null);
			File root = folder.newFolder(outer.getName());
			createProject(root, outer);
			createProject(new Path(root.getAbsolutePath()).append(inner1.getName()).toFile(), inner1);
			createProject(new Path(root.getAbsolutePath()).append(inner2.getName()).toFile(), inner2);
			inner1.delete(false, true, new NullProgressMonitor());
		} finally {
			Job.getJobManager().endRule(rule);
		}
		try {
			IMenuManager manager = menuManager();
			provider(new StructuredSelection(
					Arrays.asList(outer.getFolder(inner1.getName()), outer.getFolder(inner2.getName()))))
							.fillContextMenu(manager);
			assertFalse(
					"There were both imported and not-imported projects in selection, but SelectProjectForFolderAction contributions were added",
					contributionAdded(manager, SelectProjectForFolderAction.class));
			assertFalse(
					"There were both imported and not-imported projects in selection, but OpenFolderAsProjectAction contributions were added",
					contributionAdded(manager, OpenFolderAsProjectAction.class));
		} finally {
			clear(projects);
		}
	}

	private void clear(List<IProject> projects) {
		projects.forEach(handle -> {
			try {
				handle.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				// Ignore
			}
		});
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

	private String contributionsList(IMenuManager manager) {
		return Stream.of(manager.getItems()) //
				.map(IContributionItem::getClass) //
				.map(Class::getName) //
				.collect(Collectors.joining(","));
	}

	private void createProject(File location, IProject actual) throws CoreException {
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(actual.getName());
		description.setLocation(new Path(location.getAbsolutePath()));
		actual.create(description, new NullProgressMonitor());
		actual.open(new NullProgressMonitor());
	}

	private void ensureDescriptionsExist(IProject outer, IProject inner1, IProject inner2) throws IOException {
		ensureFileExists(outer.getFolder(inner1.getName()).getFile(IProjectDescription.DESCRIPTION_FILE_NAME),
				inner1.getName());
		ensureFileExists(outer.getFolder(inner2.getName()).getFile(IProjectDescription.DESCRIPTION_FILE_NAME),
				inner2.getName());
	}

	private void ensureFileExists(IFile description, String name) throws IOException {
		if (description.exists()) {
			return;
		}
		// If project description does not exist after creation (for whatever reason),
		// create it explicitly with empty content
		Files.createFile(Paths.get(description.getLocationURI()));
		assertTrue(String.format("Project description for %s does not exist", name), description.exists());
	}

}