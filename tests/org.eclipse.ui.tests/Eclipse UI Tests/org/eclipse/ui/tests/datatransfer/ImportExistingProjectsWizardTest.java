/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Bits of importWizard from DeprecatedUIWizards
 *     Red Hat, Inc - initial API and implementation
 *     Paul Pazderski - Bug 546546: migrate to JUnit4 test
 *******************************************************************************/

package org.eclipse.ui.tests.datatransfer;

import static org.eclipse.jface.dialogs.IMessageProvider.WARNING;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.datatransfer.ImportTestUtils.restoreWorkspaceConfiguration;
import static org.eclipse.ui.tests.datatransfer.ImportTestUtils.setWorkspaceAutoBuild;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.datatransfer.ImportTestUtils.TestBuilder;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ImportExistingProjectsWizardTest {

	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";
	private static final String WS_DATA_LOCATION = "importExistingFromDirTest";
	private static final String WS_NESTED_DATA_LOCATION = "importExistingNestedTest";
	private static final String ARCHIVE_HELLOWORLD = "helloworld";
	private static final String ARCHIVE_FILE_WITH_EMPTY_FOLDER = "EmptyFolderInArchive";
	private static final String PROJECTS_ARCHIVE = "ProjectsArchive";
	private static final String CORRUPT_PROJECTS_ARCHIVE = "CorruptProjectsArchive";

	private static final String[] FILE_LIST = { "test-file-1.txt", "test-file-2.doc", ".project" };

	private static final String[] ARCHIVE_FILE_EMPTY_FOLDER_LIST = { "empty", "folder" };

	private String dataLocation = null;

	private String zipLocation = null;

	private WizardDialog dialog;

	private boolean originalRefreshSetting;

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private Shell getShell() {
		return getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	@Before
	public void doSetUp() throws Exception {
		originalRefreshSetting = ResourcesPlugin.getPlugin()
				.getPluginPreferences().getBoolean(
						ResourcesPlugin.PREF_AUTO_REFRESH);
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(
				ResourcesPlugin.PREF_AUTO_REFRESH, true);
		setWorkspaceAutoBuild(true);
	}

	@After
	public void doTearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}

		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = wsRoot.getProjects();
		for (int i = projects.length - 1; i >= 0; i--) {
			FileUtil.deleteProject(projects[i]);
		}
		// clean up any data directories created
		if (dataLocation != null) {
			File root = new File(dataLocation);
			if (root.exists()) {
				FileSystemHelper.clear(root);
			}
		}
		dataLocation = null; // reset for next test

		// do same for zip location
		if (zipLocation != null) {
			File root = new File(zipLocation);
			if (root.exists()) {
				// zipLocation is the zip file, not the temp directory it was
				// copied to
				FileSystemHelper.clear(root.getParentFile());
			}
		}
		zipLocation = null; // reset for next test

		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(
				ResourcesPlugin.PREF_AUTO_REFRESH, originalRefreshSetting);
		restoreWorkspaceConfiguration();
	}

	private void waitForRefresh() {
		try {
			getWorkbench().getProgressService().busyCursorWhile(
					monitor -> Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH,
							new NullProgressMonitor()));
		} catch (InvocationTargetException | InterruptedException e) {
			fail(e.getLocalizedMessage());
		}
	}

	// Note: this and all other tests are numbered because they must run in a
	// specific order.
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=369660
	// Tests are now numbered and run in method name order.
	@Test
	public void test01FindSingleZip() throws IOException {
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			assertFalse(selectedProject.hasConflicts());
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));
	}

	@Test
	public void test02FindSingleTar() throws IOException {
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".tar"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			assertFalse(selectedProject.hasConflicts());
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));
	}

	@Test
	public void test03FindSingleDirectory() throws IOException {
		dataLocation = ImportTestUtils.copyDataLocation(WS_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation).append("HelloWorld");
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");
		// We're importing a directory
		wpip.getProjectFromDirectoryRadio().setSelection((true));
		wpip.updateProjectsList(wsPath.toOSString());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			assertFalse(selectedProject.hasConflicts());
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in directory", projectNames.containsAll(projects));
	}

	@Test
	public void test04DoNotShowProjectWithSameName() throws IOException, CoreException {
		dataLocation = ImportTestUtils.copyDataLocation(WS_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation);

		FileUtil.createProject("HelloWorld");

		WizardProjectsImportPage wpip = getNewWizard();
		// We're importing a directory
		wpip.getProjectFromDirectoryRadio().setSelection((true));
		wpip.updateProjectsList(wsPath.toOSString());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		for (ProjectRecord selectedProject : selectedProjects) {
			if (selectedProject.getProjectName().equals("HelloWorld")) {
				assertTrue(selectedProject.hasConflicts());
			}
		}
	}

	@Test
	public void test05ImportSingleZip() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			assertFalse(selectedProject.hasConflicts());
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

	}

	@Test
	public void test06ImportZipWithEmptyFolder() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_FILE_WITH_EMPTY_FOLDER + ".zip"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("A");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], ARCHIVE_FILE_EMPTY_FOLDER_LIST, false);

	}

	@Test
	public void test07ImportSingleTar() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".tar"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);
	}

	@Test
	public void test08ImportTarWithEmptyFolder() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_FILE_WITH_EMPTY_FOLDER + ".tar"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("A");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported: Expected=1, Actual=" + workspaceProjects.length);
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("A");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], ARCHIVE_FILE_EMPTY_FOLDER_LIST, false);
	}

	@Test
	public void test09ImportSingleDirectory() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		dataLocation = ImportTestUtils.copyDataLocation(WS_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation).append("HelloWorld");
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((true));
		wpip.updateProjectsList(wsPath.toOSString());
		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in directory", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(false, workspaceProjects[0], FILE_LIST, true);

	}

	@Test
	public void test10ImportSingleDirectoryWithCopy() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		dataLocation = ImportTestUtils.copyDataLocation(WS_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation).append("HelloWorld");
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((true));
		wpip.getCopyCheckbox().setSelection(true);
		wpip.saveWidgetValues();
		wpip.restoreWidgetValues();

		wpip.updateProjectsList(wsPath.toOSString());
		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);
	}

	@Test
	public void test11ImportSingleDirectoryWithCopyDeleteProjectKeepContents() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		dataLocation = ImportTestUtils.copyDataLocation(WS_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation).append("HelloWorld");
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((true));
		wpip.getCopyCheckbox().setSelection(true);
		wpip.saveWidgetValues();
		wpip.restoreWidgetValues();

		wpip.updateProjectsList(wsPath.toOSString());
		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported: " + workspaceProjects.length + " projects in workspace.");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		// delete projects in workspace but not contents
		for (IProject workspaceProject : workspaceProjects) {
			workspaceProject.delete(false, true, null);
		}
		assertEquals("Test project not deleted successfully.", 0, root.getProjects().length);

		// perform same test again, but this time import from this workspace
		final WizardProjectsImportPage wpip2 = getNewWizard();
		Set<String> projects2 = Set.of("HelloWorld");

		wpip2.getProjectFromDirectoryRadio().setSelection((true));
		wpip2.getCopyCheckbox().setSelection(true);
		wpip2.saveWidgetValues();
		wpip2.restoreWidgetValues();

		wpip2.updateProjectsList(wsPath.toOSString());
		ProjectRecord[] selectedProjects2 = wpip2.getProjectRecords();
		assertEquals("Not all projects were found correctly in zip (2).", 1, selectedProjects2.length);

		ArrayList<String> projectNames2 = new ArrayList<>();
		for (ProjectRecord element : selectedProjects2) {
			projectNames2.add(element.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip (2)", projectNames2.containsAll(projects2));

		CheckboxTreeViewer projects2List = wpip2.getProjectsList();
		projects2List.setCheckedElements(selectedProjects2);
		wpip2.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself (2)");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);
	}

	@Test
	public void test12ImportZipDeleteContentsImportAgain() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 1) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("Project was imported as a folder into itself");
		}

		verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		// delete projects in workspace but not contents
		for (IProject workspaceProject : workspaceProjects) {
			workspaceProject.delete(true, true, null);
		}
		assertEquals("Test project not deleted successfully.", 0, root.getProjects().length);

		// import again
		IProject[] workspaceProjects2 = root.getProjects();
		for (IProject element : workspaceProjects2) {
			FileUtil.deleteProject(element);
		}
		URL archiveFile2 = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip"), null));
		WizardProjectsImportPage wpip2 = getNewWizard();
		Set<String> projects2 = Set.of("HelloWorld");

		wpip2.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip2.updateProjectsList(archiveFile2.getPath());

		ProjectRecord[] selectedProjects2 = wpip2.getProjectRecords();
		ArrayList<String> projectNames2 = new ArrayList<>();
		for (ProjectRecord element : selectedProjects2) {
			projectNames2.add(element.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip (2)", projectNames2.containsAll(projects2));

		CheckboxTreeViewer projectsList2 = wpip2.getProjectsList();
		projectsList2.setCheckedElements(selectedProjects2);
		wpip2.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects2 = root.getProjects();
		if (workspaceProjects2.length != 1) {
			fail("Incorrect Number of projects imported (2)");
		}
		IFolder helloFolder2 = workspaceProjects2[0].getFolder("HelloWorld");
		if (helloFolder2.exists()) {
			fail("Project was imported as a folder into itself (2)");
		}

		verifyProjectInWorkspace(true, workspaceProjects2[0], FILE_LIST, true);
	}

	@Test
	public void test13ImportDirectoryNested() throws IOException, CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		dataLocation = ImportTestUtils.copyDataLocation(WS_NESTED_DATA_LOCATION);
		IPath wsPath = IPath.fromOSString(dataLocation).append("A");
		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("A", "B", "C");

		wpip.getProjectFromDirectoryRadio().setSelection(true);
		wpip.getNestedProjectsCheckbox().setSelection(true);
		wpip.getCopyCheckbox().setSelection(false);
		wpip.saveWidgetValues();
		wpip.restoreWidgetValues();

		wpip.updateProjectsList(wsPath.toOSString());
		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in directory", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "A", "B", and "C" should be the only projects in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 3) {
			fail("Incorrect number of projects imported");
		}

		IFolder aFolder = workspaceProjects[0].getFolder("A");
		if (aFolder.exists()) {
			fail("Project A was imported as a folder into itself");
		}

		IFolder bFolder = workspaceProjects[1].getFolder("B");
		if (bFolder.exists()) {
			fail("Project B was imported as a folder into itself");
		}

		IFolder cFolder = workspaceProjects[2].getFolder("C");
		if (cFolder.exists()) {
			fail("Project C was imported as a folder into itself");
		}

		workspaceProjects[0].refreshLocal(IResource.DEPTH_INFINITE, null);

		verifyProjectInWorkspace(false, workspaceProjects[0], FILE_LIST, true);
		verifyProjectInWorkspace(false, workspaceProjects[1], FILE_LIST, true);
		verifyProjectInWorkspace(false, workspaceProjects[2], FILE_LIST, true);
	}

	@Test
	public void test14InitialValue() throws IOException, CoreException {

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			zipLocation = copyZipLocation(WS_DATA_LOCATION);
			IProject[] workspaceProjects = root.getProjects();
			for (IProject workspaceProject : workspaceProjects) {
				FileUtil.deleteProject(workspaceProject);
			}

			WizardProjectsImportPage wpip = getExternalImportWizard(zipLocation);
			ProjectRecord[] selectedProjects = wpip.getProjectRecords();
			ArrayList<String> projectNames = new ArrayList<>();
			for (ProjectRecord selectedProject : selectedProjects) {
				projectNames.add(selectedProject.getProjectName());
			}

			Set<String> projects = Set.of("HelloWorld","WorldHello");
			assertTrue("Not all projects were found correctly in zip",

			projectNames.containsAll(projects));

			// no initial value, no projects
			wpip = getExternalImportWizard(null);
			selectedProjects = wpip.getProjectRecords();
			assertEquals(0, selectedProjects.length);
	}

	@Test
	public void test15ImportArchiveMultiProject() throws IOException, CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		zipLocation = copyZipLocation(WS_DATA_LOCATION);

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of("HelloWorld", "WorldHello");

		wpip.getProjectFromDirectoryRadio().setSelection((false)); // We
																	// want
																	// the
																	// other
																	// one
																	// selected
		wpip.updateProjectsList(zipLocation);

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wpip.createProjects(); // Try importing all the projects we found
		waitForRefresh();

		// "HelloWorld" should be the only project in the workspace
		workspaceProjects = root.getProjects();
		if (workspaceProjects.length != 2) {
			fail("Incorrect Number of projects imported");
		}
		IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
		if (helloFolder.exists()) {
			fail("HelloWorld was imported as a folder into itself");
		}
		IFolder folder2 = workspaceProjects[0].getFolder("WorldHello");
		if (folder2.exists()) {
			fail("WorldHello was imported as a folder into itself");
		}

		for (IProject workspaceProject : workspaceProjects) {
			verifyProjectInWorkspace(true, workspaceProject, FILE_LIST, true);
		}
	}

	/**
	 * Verify whether or not the imported project is in the current workspace
	 * location (i.e. copy projects was true) or in another workspace location
	 * (i.e. copy projects was false).
	 */
	private void verifyProjectInWorkspace(final boolean inWorkspace,
			final IProject project, String[] fileList, boolean isListFiles) {

		IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation();
		IPath projectLocation = project.getLocation();
		boolean isProjectInWorkspace = rootLocation.isPrefixOf(projectLocation);
		if (inWorkspace) {
			if (!isProjectInWorkspace) {
				fail(project.getName()
						+ " should be in the workspace location: "
						+ rootLocation.toOSString());
			}
		} else if (isProjectInWorkspace) {
			fail(project.getName()
					+ " should not be in the workspace location: "
					+ rootLocation.toOSString());
		}
		StringBuilder filesNotImported = new StringBuilder();
		// make sure the files in the project were imported
		for (String element : fileList) {
			IResource res = isListFiles ? (IResource) project
					.getFile(element) : (IResource) project
					.getFolder(element);
			if (!res.exists()) {
				filesNotImported.append(res.getName() + ", ");
			}
		}
		assertEquals(
				"Files expected but not in workspace for project \"" + project.getName() + "\": " + filesNotImported, 0,
				filesNotImported.length());
	}



	private String copyZipLocation(String zipLocation) throws IOException {
		return ImportTestUtils.copyZipLocation(zipLocation, ARCHIVE_HELLOWORLD);
	}

	private WizardProjectsImportPage getNewWizard() {
		ImportExportWizard wizard = new ImportExportWizard(
				ImportExportWizard.IMPORT);
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
				.getSection("ImportExportTests");
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
					.addNewSection("ImportExportTests");
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		WizardProjectsImportPage wpip = new WizardProjectsImportPage();

		Shell shell = getShell();

		if (dialog != null) {
			dialog.close();
		}
		dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(100, dialog.getShell().getSize().x),
				100);

		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		wpip.setWizard(dialog.getCurrentPage().getWizard());
		wpip.createControl(parent);
		return wpip;
	}

	@Test
	public void test16GetProjectRecords() throws Exception {

		Set<String> expectedNames = Set.of("Project1", "Project2", "Project3", "Project4", "Project5");

		URL projectsArchive = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + PROJECTS_ARCHIVE + ".zip"), null));

		List<String> projectNames = getNonConflictingProjectsFromArchive(projectsArchive);

		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(expectedNames));

		FileUtil.createProject("Project1");
		projectNames = getNonConflictingProjectsFromArchive(projectsArchive);

		assertFalse("Conflict flag is not set on the projects correctly", projectNames.contains("Project1"));

	}

	private List<String> getNonConflictingProjectsFromArchive(URL projectsArchive) {
		WizardProjectsImportPage newWizard = getNewWizard();
		newWizard.getProjectFromDirectoryRadio().setSelection(false);
		newWizard.updateProjectsList(projectsArchive.getPath());

		ProjectRecord[] projectRecords = newWizard.getProjectRecords();

		List<String> projectNames = new ArrayList<>();
		for (ProjectRecord projectRecord : projectRecords) {
			if (!projectRecord.hasConflicts()) {
				projectNames.add(projectRecord.getProjectName());
			}
		}
		return projectNames;
	}

	@Test
	public void test17GetProjectRecordsShouldHandleCorruptProjects() throws Exception {

		URL projectsArchive = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + CORRUPT_PROJECTS_ARCHIVE + ".zip"), null));

		WizardProjectsImportPage newWizard = spy(getNewWizard());
		ProjectRecord[] projectRecords = getProjectsFromArchive(newWizard, projectsArchive);
		verify(newWizard).setMessage(DataTransferMessages.WizardProjectsImportPage_projectsInvalid, WARNING);

		List<String> invalidProjectNames = getInvalidProjects(projectRecords);
		assertEquals("Expected to find invalid projects", 2, invalidProjectNames.size());
		assertEquals("Expected pseudo name for first invalid project",
				DataTransferMessages.WizardProjectsImportPage_invalidProjectName, invalidProjectNames.get(0));
		assertEquals("Expected pseudo name for second invalid project",
				DataTransferMessages.WizardProjectsImportPage_invalidProjectName, invalidProjectNames.get(1));

		List<String> validProjectNames = getValidProjects(projectRecords);
		assertEquals("Expected to find only one valid project", 1, validProjectNames.size());
		assertEquals("Expected to find valid project", "Project1", validProjectNames.get(0));

	}

	@Test
	public void test18GetProjectRecordsShouldHandleCorruptAndConflictingProjects() throws Exception {

		URL projectsArchive = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + CORRUPT_PROJECTS_ARCHIVE + ".zip"), null));

		WizardProjectsImportPage newWizard = spy(getNewWizard());
		FileUtil.createProject("Project1");

		ProjectRecord[] projectRecords = getProjectsFromArchive(newWizard, projectsArchive);
		verify(newWizard).setMessage(DataTransferMessages.WizardProjectsImportPage_projectsInWorkspaceAndInvalid,
				WARNING);

		List<String> invalidProjectNames = getInvalidProjects(projectRecords);
		assertEquals("Expected to find invalid projects", 2, invalidProjectNames.size());

		List<String> validProjectNames = getValidProjects(projectRecords);
		assertEquals("Expected to find one valid project", 1, validProjectNames.size());
		assertEquals("Expected to find valid project", "Project1", validProjectNames.get(0));

		List<String> conflictingProjectNames = getProjectsWithConflicts(projectRecords);
		assertEquals("Expected to find one existing project", 1, conflictingProjectNames.size());
		assertEquals("Expected to find existing project", "Project1", conflictingProjectNames.get(0));

	}

	@Test
	public void test19CloseImportedProjectsZipFile() throws Exception {
		ImportTestUtils.deleteWorkspaceProjects();
		WizardProjectsImportPage wpip = getNewWizard();

		try (AutoCloseable restore = setPageSetting(wpip, "WizardProjectsImportPage.STORE_CLOSE_CREATED_PROJECTS_ID", true)) {
			useDataLocationProject(wpip, "ImportExistingProjectsWizardTestRebuildProject");
			assertTrue("Failed to import project", wpip.createProjects());

			IProject testProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject("ImportExistingProjectsWizardTestRebuildProject");
			assertTrue("Failed to import test project", testProject.exists());
			assertFalse("Expected imported project to be closed due to setting dialog option checkbox",
					testProject.isOpen());
		}
	}

	@Test
	public void test20FullBuildAfterImportedProjectsZipFile() throws Exception {
		WizardProjectsImportPage wpip = createImportWizardWithZipLocation(
				"ImportExistingProjectsWizardTestRebuildProject");

		ImportTestUtils.TestBuilder.resetCallCount();
		assertTrue("Failed to import project", wpip.createProjects());
		processEvents();
		ImportTestUtils.waitForBuild();

		ImportTestUtils.TestBuilder.assertFullBuildWasDone();
	}

	@Test
	public void test21FullBuildAfterImportedProjects() throws Exception {
		WizardProjectsImportPage wpip = createImportWizardWithDataLocation(
				"ImportExistingProjectsWizardTestRebuildProject"); // located in data/workspaces/

		ImportTestUtils.TestBuilder.resetCallCount();
		assertTrue("Failed to import project", wpip.createProjects());
		processEvents();
		ImportTestUtils.waitForBuild();

		TestBuilder.assertFullBuildWasDone();
	}

	@Test
	public void test22FullBuildAfterImportedProjectsWithCopy() throws Exception {
		ImportTestUtils.deleteWorkspaceProjects();
		WizardProjectsImportPage wpip = getNewWizard();

		try (AutoCloseable restore = setPageSetting(wpip, "WizardProjectsImportPage.STORE_COPY_PROJECT_ID", true)) {
			useDataLocationProject(wpip, "ImportExistingProjectsWizardTestRebuildProject");

			ImportTestUtils.TestBuilder.resetCallCount();
			assertTrue("Failed to import project", wpip.createProjects());
			processEvents();
			ImportTestUtils.waitForBuild();

			TestBuilder.assertFullBuildWasDone();
		}
	}

	private WizardProjectsImportPage createImportWizardWithZipLocation(String testProject) throws Exception {
		ImportTestUtils.deleteWorkspaceProjects();

		WizardProjectsImportPage wpip = getNewWizard();
		useZipLocationProject(wpip, testProject);

		return wpip;
	}

	private void useZipLocationProject(WizardProjectsImportPage wpip, String testProject) throws Exception {
		zipLocation = ImportTestUtils.copyZipLocation(testProject, testProject); // located in data/workspaces/
		wpip.getProjectFromDirectoryRadio().setSelection(false); // select the other option
		wpip.updateProjectsList(zipLocation);
		selectTestProject(wpip, testProject);
	}

	private WizardProjectsImportPage createImportWizardWithDataLocation(String testProject) throws Exception {
		ImportTestUtils.deleteWorkspaceProjects();

		WizardProjectsImportPage wpip = getNewWizard();
		useDataLocationProject(wpip, testProject);

		return wpip;
	}

	private void useDataLocationProject(WizardProjectsImportPage wpip, String testProject) throws Exception {
		dataLocation = ImportTestUtils.copyDataLocation("ImportExistingProjectsWizardTestRebuildProject"); // located in
		wpip.getProjectFromDirectoryRadio().setSelection(true);
		wpip.updateProjectsList(dataLocation);
		selectTestProject(wpip, testProject);
	}

	private void selectTestProject(WizardProjectsImportPage wpip, String testProject) {
		Set<String> projects = Set.of(testProject);
		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			projectNames.add(selectedProject.getProjectName());
		}

		assertTrue("Expected import wizard to find projects: " + projects + ", instead it detects: " + projectNames,
				projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wpip.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
	}

	private ProjectRecord[] getProjectsFromArchive(WizardProjectsImportPage newWizard, URL projectsArchive) {
		newWizard.getProjectFromDirectoryRadio().setSelection(false);
		newWizard.updateProjectsList(projectsArchive.getPath());
		return newWizard.getProjectRecords();
	}

	private List<String> getValidProjects(ProjectRecord[] projectRecords) {
		List<String> projectNames = new ArrayList<>();
		for (ProjectRecord projectRecord : projectRecords) {
			if (!projectRecord.isInvalidProject()) {
				projectNames.add(projectRecord.getProjectName());
			}
		}
		return projectNames;
	}

	private AutoCloseable setPageSetting(WizardProjectsImportPage wpip, String settingName, boolean settingValue) {
		IDialogSettings dialogSettings = wpip.getWizard().getDialogSettings();
		wpip.saveWidgetValues();
		boolean originalValue = dialogSettings.getBoolean(settingName);
		dialogSettings.put(settingName, settingValue);
		wpip.restoreWidgetValues();
		return () -> {
			dialogSettings.put(settingName, originalValue);
			wpip.restoreWidgetValues();
		};
	}

	private List<String> getInvalidProjects(ProjectRecord[] projectRecords) {
		List<String> projectNames = new ArrayList<>();
		for (ProjectRecord projectRecord : projectRecords) {
			if (projectRecord.isInvalidProject()) {
				projectNames.add(projectRecord.getProjectName());
			}
		}
		return projectNames;
	}

	private List<String> getProjectsWithConflicts(ProjectRecord[] projectRecords) {
		List<String> projectNames = new ArrayList<>();
		for (ProjectRecord projectRecord : projectRecords) {
			if (projectRecord.hasConflicts()) {
				projectNames.add(projectRecord.getProjectName());
			}
		}
		return projectNames;
	}

	private WizardProjectsImportPage getExternalImportWizard(String initialPath) {

		ExternalProjectImportWizard wizard = new ExternalProjectImportWizard(
				initialPath);
		wizard.init(getWorkbench(), null);
		if (dialog != null) {
			dialog.close();
		}
		dialog = new WizardDialog(getShell(), wizard);
		dialog.create();

		dialog.getShell().setSize(Math.max(100, dialog.getShell().getSize().x),
				100);
		return (WizardProjectsImportPage) wizard
				.getPage("wizardExternalProjectsPage");
	}


	@Test
	public void test23DoNotShowProjectwithSameNameForZipImport() throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		FileUtil.createProject("HelloWorld");

		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				IPath.fromOSString(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip"), null));
		WizardProjectsImportPage wpip = getNewWizard();
		// We want the other one selected as we are importing an archive file
		wpip.getProjectFromDirectoryRadio().setSelection((false));
		wpip.updateProjectsList(archiveFile.getPath());

		ProjectRecord[] selectedProjects = wpip.getProjectRecords();
		for (ProjectRecord selectedProject : selectedProjects) {
			if (selectedProject.getProjectName().equals("HelloWorld")) {
				assertTrue(selectedProject.hasConflicts());
			}
		}
	}

}
