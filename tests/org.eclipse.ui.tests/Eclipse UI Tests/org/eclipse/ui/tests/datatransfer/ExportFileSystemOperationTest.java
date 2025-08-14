/*******************************************************************************
 * Copyright (c) 2005, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.internal.VirtualTestFileSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExportFileSystemOperationTest extends UITestCase implements
		IOverwriteQuery {

	private static final String[] directoryNames = { "dir1", "dir2" };

	private static final String[] fileNames = { "file1.txt", "file2.txt" };

	private String localDirectory;

	private IProject project;

	public ExportFileSystemOperationTest() {
		super(ExportFileSystemOperationTest.class.getSimpleName());
	}

	@Override
	public String queryOverwrite(String pathString) {
		return "";
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = FileUtil.createProject("Export" + getName());
		File destination =
			new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir())
				.toOSString());
		localDirectory = destination.getAbsolutePath();
		assertTrue(destination.mkdirs());
		setUpData();
	}

	private void setUpData() throws CoreException {
		for (String directoryName : directoryNames) {
			IFolder folder = project.getFolder(directoryName);
			folder.create(false, true, new NullProgressMonitor());
			for (String fileName : fileNames) {
				IFile file = folder.getFile(fileName);
				String contents = directoryName + ", " + fileName;
				file.create(new ByteArrayInputStream(contents.getBytes()), true, new NullProgressMonitor());
			}
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		// delete exported data
		File root = new File(localDirectory);
		if (root.exists()){
			FileSystemHelper.clear(root);
		}
		try {
			project.delete(true, true, null);
		} finally {
			project = null;
			localDirectory = null;
		}
	}

	@Test
	public void testGetStatus() throws Exception {
		List<IResource> resources = new ArrayList<>();
		resources.add(project);
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);

		assertTrue(operation.getStatus().getCode() == IStatus.OK);
	}

	/* Export a project, with all directories */
	@Test
	public void testExportRootResource() throws Exception {
		List<IResource> resources = new ArrayList<>();
		resources.add(project);
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);
		openTestWindow().run(true, true, operation);

		// +1 for .settings
		verifyFolders(directoryNames.length + 1);
	}

	/* Export a project, create all leadup folders. */
	@Test
	public void testExportResources() throws Exception {
		List<IResource> resources = new ArrayList<>();
		IResource[] members = project.members();
		resources.addAll(Arrays.asList(members));
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);
		openTestWindow().run(true, true, operation);

		// +1 for .settings
		verifyFolders(directoryNames.length + 1);
	}

	/* Export folders, do not create leadup folders. */
	@Test
	public void testExportFolderCreateDirectoryStructure() throws Exception {
		List<IResource> resources = new ArrayList<>();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)) {
				resources.add(member);
			}
		}
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);

		operation.setCreateContainerDirectories(true);
		operation.setCreateLeadupStructure(false);
		openTestWindow().run(true, true, operation);

		verifyFolders(directoryNames.length, false);
	}

	/* Export files, do not create leadup folders. */
	@Test
	public void testExportFilesCreateDirectoryStructure() throws Exception {
		List<IResource> resources = new ArrayList<>();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)){
				IResource[] folderMembers = ((IFolder)member).members();
				for (IResource folderMember : folderMembers) {
					if (isFile(folderMember)){
						resources.add(folderMember);
					}
				}
			}
		}
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);

		operation.setCreateContainerDirectories(true);
		operation.setCreateLeadupStructure(false);
		openTestWindow().run(true, true, operation);

		verifyFiles(resources);
	}

	/* Export files, overwrite - do not create container directories or lead up folders. */
	@Test
	public void testExportOverwrite() throws Exception {
		List<IProject> resources = new ArrayList<>();
		resources.add(project);
		FileSystemExportOperation operation =
			new FileSystemExportOperation(
					null, resources, localDirectory, this);
		openTestWindow().run(true, true, operation);
		operation.setOverwriteFiles(true);
		operation.setCreateContainerDirectories(false);
		operation.setCreateLeadupStructure(false);
		openTestWindow().run(true, true, operation);

		// overwrite successful?
		IStatus status = operation.getStatus();
		assertTrue(status.toString(), status.isOK());
		// +1 for .settings
		verifyFolders(directoryNames.length + 1);
	}

	/* Export (virtual) resources without local location. */
	@Test
	public void testExportVirtualResources() throws Exception {
		String projectName = "ExportVirtual_" + UUID.randomUUID().toString();
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		projectDescription.setLocationURI(URI.create(VirtualTestFileSystem.SCHEME + ":/" + projectName));
		IProject projectVirtual = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		try {
			projectVirtual.create(projectDescription, new NullProgressMonitor());
			projectVirtual.open(new NullProgressMonitor());
			IFile testFile = projectVirtual.getFile("test");
			testFile.create(new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8)), 0, null);

			FileSystemExportOperation operation = new FileSystemExportOperation(null, List.of(testFile), localDirectory,
					this);
			openTestWindow().run(true, true, operation);

			Path expectedFile = Path.of(localDirectory).resolve(projectName).resolve("test");
			assertTrue(Files.exists(expectedFile));
			assertEquals("hello world", new String(Files.readAllBytes(expectedFile), StandardCharsets.UTF_8));
		} finally {
			projectVirtual.delete(true, true, null);
		}
	}

	private boolean isFile(IResource resource){
		for (String fileName : fileNames) {
			if (fileName.equals(resource.getName())) {
				return true;
			}
		}
		return false;
	}

	private void verifyFiles(List<IResource> resources) {
		for (IResource resource : resources) {
			assertTrue(
				"Export should have exported " + resource.getName(),
				isFile(resource));

		}
	}

	private void verifyFolders(int folderCount){
		verifyFolders(folderCount, true);
	}

	private void verifyFolders(int folderCount, boolean includeRootFolder){
		File root;
		if (includeRootFolder){
			root = new File(localDirectory, project.getName());
			assertTrue("Export failed: " + project.getName() + " folder does not exist", root.exists());
		}
		else{
			root = new File(localDirectory);
		}
		File[] files = root.listFiles();
		List<File> directories = new ArrayList<>();
		if (files != null){
			for (File file : files) {
				if (file.isDirectory()) {
					directories.add(file);
				}
			}
		}
		assertEquals("Export failed to Export all directories",
				folderCount, directories.size());

		for (File directory : directories) {
			assertTrue("Export failed to export directory " + directory.getName(), directory.exists());
			verifyFolder(directory);
		}
	}

	private void verifyFolder(File directory){
		File[] files = directory.listFiles();
		if (files != null){
			for (File file : files) {
				assertTrue("Export failed to export file: " + file.getName(), file.exists());
			}
		}
	}

	private boolean isDirectory(IResource resource){
		for (String directoryName : directoryNames) {
			if (directoryName.equals(resource.getName())) {
				return true;
			}
		}
		return false;
	}
}
