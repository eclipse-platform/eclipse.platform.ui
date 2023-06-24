/*******************************************************************************
 * Copyright (c) 2019 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.datatransfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.FileTool;
import org.eclipse.ui.tests.harness.util.FileUtil;

public class ImportTestUtils {

	public static class TestBuilder extends IncrementalProjectBuilder {

		static AtomicInteger cleanBuildCallCount = new AtomicInteger(0);
		static AtomicInteger autoBuildCallCount = new AtomicInteger(0);
		static AtomicInteger fullBuildCallCount = new AtomicInteger(0);
		static List<Integer> otherBuildTriggerTypes = new ArrayList<>();

		public TestBuilder() {
			resetCallCount();
		}

		@Override
		protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
			if (kind == IncrementalProjectBuilder.CLEAN_BUILD) {
				clean(monitor);
			} else if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
				autoBuildCallCount.incrementAndGet();
			} else if (kind == IncrementalProjectBuilder.FULL_BUILD) {
				fullBuildCallCount.incrementAndGet();
			} else {
				otherBuildTriggerTypes.add(Integer.valueOf(kind));
			}
			return null;
		}

		@Override
		protected void clean(IProgressMonitor monitor) {
			cleanBuildCallCount.incrementAndGet();
			IProject project = getProject();
			deleteFolderContents(project, "bin");
		}

		static void resetCallCount() {
			cleanBuildCallCount.set(0);
			autoBuildCallCount.set(0);
			fullBuildCallCount.set(0);
			otherBuildTriggerTypes.clear();
		}

		static void assertAutoBuildWasDone() {
			assertEquals("Expected no clean build triggers", 0, cleanBuildCallCount.get());
			assertEquals("Expected 1 auto-build trigger", 1, autoBuildCallCount.get());
			assertEquals("Expected no full build triggers", 0, fullBuildCallCount.get());
			assertEquals("Expected only clean and auto-build triggers", Collections.EMPTY_LIST, otherBuildTriggerTypes);
		}

		static void assertFullBuildWasDone() {
			assertEquals("Expected no clean build triggers", 0, cleanBuildCallCount.get());
			assertEquals("Expected 1 full-build trigger", 1, fullBuildCallCount.get());
			assertEquals("Expected no auto-build triggers", 0, autoBuildCallCount.get());
			assertEquals("Expected only clean and auto-build triggers", Collections.EMPTY_LIST, otherBuildTriggerTypes);
		}

		static void assertNoBuildWasDone() {
			assertEquals("Expected no clean build triggers", 0, cleanBuildCallCount.get());
			assertEquals("Expected no full-build triggers", 0, fullBuildCallCount.get());
			assertEquals("Expected no auto-build triggers", 0, autoBuildCallCount.get());
			assertEquals("Expected no build triggers", Collections.EMPTY_LIST, otherBuildTriggerTypes);
		}
	}

	static final String WS_DATA_PREFIX = "data/workspaces";

	/**
	 * Copies the data to a temporary directory and returns the new location.
	 *
	 * @return the location
	 */
	static String copyDataLocation(String dataLocation) throws IOException {
		TestPlugin plugin = TestPlugin.getDefault();
		File origin = FileTool.getFileInPlugin(plugin,
				IPath.fromOSString("/" + WS_DATA_PREFIX + "/" + dataLocation + ".zip"));
		ZipFile zFile = new ZipFile(origin);
		File destination = new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()).toOSString());
		FileTool.unzip(zFile, destination);
		return destination.getAbsolutePath();
	}

	static String copyZipLocation(String sourceZipLocation, String targetZipName) throws IOException {
		TestPlugin plugin = TestPlugin.getDefault();
		File origin = FileTool.getFileInPlugin(plugin,
				IPath.fromOSString(WS_DATA_PREFIX + "/" + sourceZipLocation + ".zip"));
		File destination = new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()).toOSString()
				+ File.separator + targetZipName + ".zip");
		FileTool.copy(origin, destination);
		return destination.getAbsolutePath();
	}

	static void assertBinFolderIsCleaned(String projectName) throws CoreException {
		IWorkspaceRoot root = workspaceRoot();
		IProject project = root.getProject(projectName);
		assertTrue("Expected project to exist in workspace: " + projectName, project.isAccessible());
		assertBinFolderIsCleaned(project);
	}

	static void assertBinFolderIsCleaned(IProject project) throws CoreException {
		IFolder bin = project.getFolder("bin");
		assertTrue("Expected bin/ folder of project " + project.getName() + " to exist", bin.isAccessible());
		assertEquals("Expected bin folder to be empty", Collections.EMPTY_LIST, Arrays.asList(bin.members()));
	}

	static void disableWorkspaceAutoBuild() throws CoreException {
		setWorkspaceAutoBuild(false);
	}

	static void setWorkspaceAutoBuild(boolean autobuildOn) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceDescription description = workspace.getDescription();
		boolean oldAutoBuildingState = description.isAutoBuilding();
		if (oldAutoBuildingState != autobuildOn) {
			description.setAutoBuilding(autobuildOn);
			workspace.setDescription(description);
		}
	}

	static void waitForBuild() throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, null);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
	}

	static void deleteWorkspaceProjects() throws CoreException {
		IWorkspaceRoot root = workspaceRoot();
		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
	}

	static void deleteWorkspaceProjects(IProject... projects) throws CoreException {
		for (IProject project : projects) {
			FileUtil.deleteProject(project);
		}
	}

	private static IWorkspaceRoot workspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private static void deleteFolderContents(IProject project, String folderName) {
		try {
			IFolder folder = project.getFolder(folderName);
			folder.delete(true, null);
			folder.create(true, false, null);
		} catch (CoreException e) {
			throw new AssertionError("Failed to delete folder: " + folderName + " in project: " + project.getName(), e);
		}
	}
}
