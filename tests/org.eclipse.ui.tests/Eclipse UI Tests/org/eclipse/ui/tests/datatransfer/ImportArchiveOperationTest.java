/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class ImportArchiveOperationTest implements IOverwriteQuery {
	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";

	private static final String ARCHIVE_SOURCE_PROPERTY = "archiveSource";

	private static final String ARCHIVE_115800_PROPERTY = "bug115800Source";

	private static final String rootResourceName = "test.txt";

	private static final String[] directoryNames = { "dir1", "dir2" };

	private static final String[] fileNames = { "file1.txt", "file2.txt" };

	private String localDirectory;
	private IProject project;

	private URL zipFileURL;

	private URL tarFileURL;

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Override
	public String queryOverwrite(String pathString) {
		//Always return an empty String - we aren't
		//doing anything interesting
		return "";
	}

	/**
	 * Tear down. Delete the project we created and all of the
	 * files on the file system.
	 */
	@After
	public void doTearDown() throws Exception {
		try {
			project.delete(true, true, null);
		}
		finally{
			localDirectory = null;
			project = null;
			zipFileURL = null;
			tarFileURL = null;
		}
	}

	private void setup(String propertyName) throws Exception{
		Class<?> testClass = Class
				.forName("org.eclipse.ui.tests.datatransfer.ImportArchiveOperationTest");
		InputStream stream = testClass.getResourceAsStream("tests.ini");
		Properties properties = new Properties();
		properties.load(stream);
		String zipFileName = properties.getProperty(propertyName);
		localDirectory = zipFileName;

		zipFileURL = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX).append(zipFileName + ".zip"), null));
		tarFileURL = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX).append(zipFileName + ".tar"), null));
	}

	@Test
	public void testZipGetStatus() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImportZipGetStatus");
		try (ZipFile zipFile = new ZipFile(zipFileURL.getPath())) {

			ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			List<ZipEntry> entries = new ArrayList<>();
			while (zipEntries.hasMoreElements()) {
				entries.add(zipEntries.nextElement());
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(zipFileURL.getPath()),
					structureProvider.getRoot(), structureProvider, this, entries);

			assertTrue(operation.getStatus().getCode() == IStatus.OK);
		}
	}

	@Test
	public void testTarGetStatus() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImportTarGetStatus");
		try (TarFile tarFile = new TarFile(tarFileURL.getPath())) {
			TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);

			Enumeration<?> tarEntries = tarFile.entries();
			List<Object> entries = new ArrayList<>();
			while (tarEntries.hasMoreElements()) {
				entries.add(tarEntries.nextElement());
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(tarFileURL.getPath()),
					structureProvider.getRoot(), structureProvider, this, entries);

			assertTrue(operation.getStatus().getCode() == IStatus.OK);
		}
	}

	@Test
	public void testZipImport() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImportZip");
		try (ZipFile zipFile = new ZipFile(zipFileURL.getPath())) {
			ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			List<ZipEntry> entries = new ArrayList<>();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = zipEntries.nextElement();
				if (!entry.isDirectory()) {
					entries.add(entry);
				}
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);

			verifyFiles(directoryNames.length, false);
		}
	}


	@Test
	public void testTarImport() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImportTar");
		try (TarFile tarFile = new TarFile(tarFileURL.getPath())) {
			TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);

			Enumeration<?> tarEntries = tarFile.entries();
			List<Object> entries = new ArrayList<>();
			while (tarEntries.hasMoreElements()) {
				entries.add(tarEntries.nextElement());
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);
		}

		verifyFiles(directoryNames.length, false);
	}

	@Test
	public void testTarSetOverwriteResources() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImportTarSetOverwriteResources");
		try (TarFile tarFile = new TarFile(tarFileURL.getPath())) {
			TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);

			Enumeration<?> tarEntries = tarFile.entries();
			List<Object> entries = new ArrayList<>();
			while (tarEntries.hasMoreElements()) {
				entries.add(tarEntries.nextElement());
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);
			operation.setOverwriteResources(true);
			openTestWindow().run(true, true, operation);
		}
		verifyFiles(directoryNames.length, false);
	}

	@Test
	public void testZipSetOverwriteResources() throws Exception {
		setup(ARCHIVE_SOURCE_PROPERTY);
		project = FileUtil.createProject("ImporZiprSetOverwriteResources");
		try (ZipFile zipFile = new ZipFile(zipFileURL.getPath())) {
			ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			List<ZipEntry> entries = new ArrayList<>();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = zipEntries.nextElement();
				if (!entry.isDirectory()) {
					entries.add(entry);
				}
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);
			operation.setOverwriteResources(true);
			openTestWindow().run(true, true, operation);
		}
		verifyFiles(directoryNames.length, false);
	}

	@Test
	public void testZipWithFileAtRoot() throws Exception {
		setup(ARCHIVE_115800_PROPERTY);
		project = FileUtil.createProject("ImportZipWithFileAtRoot");
		try (ZipFile zipFile = new ZipFile(zipFileURL.getPath())) {
			ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			List<ZipEntry> entries = new ArrayList<>();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = zipEntries.nextElement();
				if (!entry.isDirectory()) {
					entries.add(entry);
				}
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);
		}

		verifyFiles(directoryNames.length, true);
	}


	@Test
	public void testTarWithFileAtRoot() throws Exception {
		setup(ARCHIVE_115800_PROPERTY);
		project = FileUtil.createProject("ImportTarWithFileAtRoot");
		try (TarFile tarFile = new TarFile(tarFileURL.getPath())) {
			TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);

			Enumeration<?> tarEntries = tarFile.entries();
			List<Object> entries = new ArrayList<>();
			while (tarEntries.hasMoreElements()) {
				entries.add(tarEntries.nextElement());
			}
			ImportOperation operation = new ImportOperation(IPath.fromOSString(project.getName()),
					structureProvider.getRoot(), structureProvider, this, entries);

			openTestWindow().run(true, true, operation);
		}

		verifyFiles(directoryNames.length, true);

	}

	/**
	 * Verifies that all files were imported.
	 *
	 * @param folderCount number of folders that were imported
	 */
	private void verifyFiles(int folderCount, boolean hasRootMembers) throws CoreException {
		IPath path = IPath.fromOSString(localDirectory);
		IResource targetFolder = project.findMember(path.makeRelative());

		assertTrue("Import failed", targetFolder instanceof IContainer);

		IResource[] resources = ((IContainer) targetFolder).members();
		if (!hasRootMembers) {
			assertEquals("Import failed to import all directories", folderCount, resources.length);
			for (IResource resource : resources) {
				assertTrue("Import failed", resource instanceof IContainer);
				verifyFolder((IContainer) resource);
			}
		} else {
			for (IResource resource : resources) {
				if (resource instanceof IContainer c) {
					verifyFolder(c);
				} else {
					assertTrue("Root resource is not present or is not present as a file: " + rootResourceName,
							resource instanceof IFile && rootResourceName.equals(resource.getName()));
				}
			}
		}

	}

	/**
	 * Verifies that all files were imported into the specified folder.
	 */
	private void verifyFolder(IContainer folder) throws CoreException {
		IResource[] files = folder.members();
		assertEquals("Import failed to import all files", fileNames.length, files.length);
		for (String fileName : fileNames) {
			int k;
			for (k = 0; k < files.length; k++) {
				if (fileName.equals(files[k].getName())) {
					break;
				}
			}
			assertTrue("Import failed to import file " + fileName, k < fileNames.length);
		}
	}

}
