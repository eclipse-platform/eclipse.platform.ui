/*******************************************************************************
 * Copyright (c) 2008, 2026 webtekie@gmail.com, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     webtekie@gmail.com - initial API and implementation
 *     IBM Corporation - fixed dead code warning
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonViewerMapper;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Performance tests for the Project Explorer: deleting many projects, deleting
 * many files below a collapsed project, removing such files from the viewer
 * directly, and mapping the label provider updates.
 * <p>
 * The scenarios that a workspace change drives are measured twice, once with
 * the view hidden and once with it showing, because only the difference between
 * the two is the cost of the view.
 */
public class ProjectExplorerPerformanceTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	private static final DecimalFormat NAME_FORMAT = new DecimalFormat("000");

	/** Rounds per scenario, so that the reported minimum is not a single sample. */
	private static final int ROUNDS = 10;

	/**
	 * Folders between the project and the deleted files. The viewer asks the
	 * content service for a parent once per level, so the depth is what the
	 * scenario is about.
	 */
	private static final int FOLDER_DEPTH = 10;

	private final List<Long> timings = new ArrayList<>();

	private String scenario;

	private long measuringSince;

	private CommonViewer viewer;

	@BeforeEach
	public void startScenario(TestInfo testInfo) {
		scenario = getClass().getSimpleName() + "." + testInfo.getDisplayName();
		timings.clear();
	}

	@AfterEach
	public void cleanUpWorkspace() throws CoreException {
		hideProjectExplorer();
		runInWorkspace(monitor -> {
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				project.delete(true, true, null);
			}
		});
	}

	/**
	 * Runs the given change as one workspace operation, so the view sees a single
	 * resource delta rather than one per resource.
	 */
	private void runInWorkspace(ICoreRunnable change) throws CoreException {
		ResourcesPlugin.getWorkspace().run(change, null);
		processEvents();
	}

	private void startMeasuring() {
		measuringSince = System.nanoTime();
	}

	private void stopMeasuring() {
		timings.add(System.nanoTime() - measuringSince);
	}

	private void report(String suffix) {
		reportTimings(scenario + " " + suffix, timings);
		timings.clear();
	}

	private void showProjectExplorer() throws CoreException {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = activePage.showView(ProjectExplorer.VIEW_ID);
		viewer = ((ProjectExplorer) view).getCommonViewer();
		processEvents();
	}

	private void hideProjectExplorer() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = activePage.findView(ProjectExplorer.VIEW_ID);
		if (view != null) {
			activePage.hideView(view);
		}
		viewer = null;
		processEvents();
	}

	private IProject createProject(String name) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		runInWorkspace(monitor -> {
			if (!project.exists()) {
				project.create(null);
			}
			project.open(null);
		});
		return project;
	}

	private void createFiles(IContainer container, int fileCount) throws CoreException {
		runInWorkspace(monitor -> addFiles(container, fileCount));
	}

	private void addFiles(IContainer container, int fileCount) throws CoreException {
		for (int i = 0; i < fileCount; i++) {
			IFile file = container.getFile(new Path("f" + NAME_FORMAT.format(i)));
			if (!file.exists()) {
				file.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
			}
		}
	}

	/**
	 * Creates a chain of nested folders and answers the innermost one.
	 */
	private IContainer createNestedFolders(IProject project, int depth) throws CoreException {
		IContainer[] innermost = new IContainer[] { project };
		runInWorkspace(monitor -> {
			IContainer container = project;
			for (int i = 0; i < depth; i++) {
				IFolder folder = container.getFolder(new Path("d" + NAME_FORMAT.format(i)));
				if (!folder.exists()) {
					folder.create(true, true, null);
				}
				container = folder;
			}
			innermost[0] = container;
		});
		return innermost[0];
	}

	private void createProjects(int projectCount) throws CoreException {
		runInWorkspace(monitor -> {
			for (int i = 0; i < projectCount; i++) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("p" + NAME_FORMAT.format(i));
				project.create(null);
				project.open(null);
				project.getFile("f" + NAME_FORMAT.format(0)).create(new ByteArrayInputStream(new byte[0]), true, null);
			}
		});
	}

	private void deleteAllProjects() throws CoreException {
		runInWorkspace(monitor -> {
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				project.delete(true, null);
			}
		});
	}

	private void deleteAllFiles(IContainer container) throws CoreException {
		runInWorkspace(monitor -> {
			for (IResource member : container.members()) {
				if (member instanceof IFile) {
					member.delete(true, null);
				}
			}
		});
	}

	/**
	 * Deleting many projects, which the view shows as top level elements.
	 */
	@Test
	public void testDeleteProjects() throws CoreException {
		int projectCount = 100;

		hideProjectExplorer();
		for (int round = 0; round < ROUNDS; round++) {
			createProjects(projectCount);
			startMeasuring();
			deleteAllProjects();
			stopMeasuring();
		}
		report("without Project Explorer");

		showProjectExplorer();
		for (int round = 0; round < ROUNDS; round++) {
			createProjects(projectCount);
			assertEquals(projectCount, viewer.getTree().getItemCount());
			startMeasuring();
			deleteAllProjects();
			stopMeasuring();
			assertEquals(0, viewer.getTree().getItemCount());
		}
		report("with Project Explorer");
	}

	/**
	 * Deleting many files nested below a project that was never expanded. The
	 * files have no item in the tree, so the viewer asks the content service for
	 * their parent, and it does so once per level of the ancestor chain.
	 */
	@Test
	public void testDeleteFilesInCollapsedProject() throws CoreException {
		int fileCount = 2000;

		hideProjectExplorer();
		IProject project = createProject("collapsedProject");
		IContainer folder = createNestedFolders(project, FOLDER_DEPTH);
		for (int round = 0; round < ROUNDS; round++) {
			createFiles(folder, fileCount);
			startMeasuring();
			deleteAllFiles(folder);
			stopMeasuring();
		}
		report("without Project Explorer");

		showProjectExplorer();
		for (int round = 0; round < ROUNDS; round++) {
			createFiles(folder, fileCount);
			assertEquals(1, viewer.getTree().getItemCount());
			assertFalse(viewer.getTree().getItem(0).getExpanded(), "the project must stay collapsed");
			startMeasuring();
			deleteAllFiles(folder);
			stopMeasuring();
		}
		report("with Project Explorer");
	}

	/**
	 * Removing elements that have no item in the tree, which is what a delete
	 * below a collapsed project comes down to. The viewer cannot find a widget
	 * for them, so it asks the content service for their parent instead, and
	 * that is the whole measured cost here.
	 */
	@Test
	public void testRemoveFilesOfCollapsedProject() throws CoreException {
		int fileCount = 2000;

		IProject project = createProject("collapsedProject");
		IContainer folder = createNestedFolders(project, FOLDER_DEPTH);
		createFiles(folder, fileCount);
		showProjectExplorer();
		assertEquals(1, viewer.getTree().getItemCount());
		assertFalse(viewer.getTree().getItem(0).getExpanded(), "the project must stay collapsed");

		Object[] files = folder.members();
		assertEquals(fileCount, files.length);

		for (int round = 0; round < ROUNDS; round++) {
			startMeasuring();
			viewer.remove(files);
			stopMeasuring();
		}
		report("per " + fileCount + " files");
	}

	/**
	 * Opening a project with many files, with and without the mapper that keeps
	 * the label provider updates from scanning the whole tree.
	 */
	@Test
	public void testLabelProviderMapping() throws CoreException {
		int fileCount = 2000;

		showProjectExplorer();
		IProject project = createProject("mappedProject");
		createFiles(project, fileCount);
		ICommonViewerMapper mapper = viewer.getMapper();
		processEvents();

		project.close(null);
		processEvents();
		viewer.setMapper(null);
		startMeasuring();
		project.open(null);
		processEvents();
		stopMeasuring();
		long unmapped = timings.get(0);
		report("without mapper");

		project.close(null);
		processEvents();
		viewer.setMapper(mapper);
		startMeasuring();
		project.open(null);
		processEvents();
		stopMeasuring();
		long mapped = timings.get(0);
		report("with mapper");

		assertTrue(mapped < unmapped, "the mapper should make opening the project cheaper");
	}

}
