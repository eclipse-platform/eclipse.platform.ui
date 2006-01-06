/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * Copyright (c) 2005 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Bits of importWizard from DeprecatedUIWizards 
 *     Red Hat, Inc - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.datatransfer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.tests.harness.util.FileTool;
import org.eclipse.ui.tests.harness.util.FileUtil;

public class ImportExistingTest extends DataTransferTestCase {
	private static final String PLUGIN_ID = "org.eclipse.ui.tests";
	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";
	private static final String WS_DATA_PREFIX = "data/workspaces";
	private static final String WS_DATA_LOCATION = "importExistingFromDirTest";
	private static final String ARCHIVE_HELLOWORLD = "helloworld";
	private static final String ARCHIVE_FILE_WITH_EMPTY_FOLDER = "EmptyFolderInArchive";
	
	private static final String[] FILE_LIST = new String[] {
			"HelloWorld.java", "HelloWorld.class", ".project", ".classpath" };
	
	private static final String[] ARCHIVE_FILE_EMPTY_FOLDER_LIST = new String[]{
			"empty", "folder" };
	
	private String dataLocation = null;
	
	private boolean originalRefreshSetting;
	
	public ImportExistingTest(String testName) {
		super(testName);
	}
	
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	
	protected void doSetUp() throws Exception {
		super.doSetUp();
		originalRefreshSetting = ResourcesPlugin.getPlugin()
        	.getPluginPreferences().getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
		ResourcesPlugin.getPlugin()
    		.getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
	}

	protected void doTearDown() throws Exception {
		super.doTearDown();
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = wsRoot.getProjects();
		for (int i = 0; i < projects.length; i++){
			FileUtil.deleteProject(projects[i]);
		}
		// clean up any data directories created
		if (dataLocation != null){
			File root = new File(dataLocation);
			if (root.exists()){
				deleteDirectory(root);
			}
		}
		dataLocation = null;	// reset for next test
		
		ResourcesPlugin.getPlugin()
			.getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, 
						originalRefreshSetting);
	}

	public void testFindSingleZip() {
		try {
			URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());

			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));
		} catch (IOException e) {
			fail(e.toString());
		}
	}
	
	public void testFindSingleTar() {
		try {
			URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".tar")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}
			
			assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	public void testFindSingleDirectory() {
		try {
			dataLocation = copyDataLocation();
			IPath wsPath = new Path(dataLocation).append(PLUGIN_ID).append(
					DATA_PATH_PREFIX).append("HelloWorld");
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			// We're importing a directory
			wpip.getProjectFromDirectoryRadio().setSelection((true)); 
			wpip.updateProjectsList(wsPath.toOSString());

			ProjectRecord[] selectedProjects = wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in directory",
					projectNames.containsAll(projects));
		} catch (IOException e) {
			fail(e.toString());
		}
	}
	
	
	public void testDoNotShowProjectWithSameName(){
		try {
			dataLocation = copyDataLocation();
			IPath wsPath = new Path(dataLocation).append(PLUGIN_ID).append(
					DATA_PATH_PREFIX);
			
			FileUtil.createProject("HelloWorld");
			
			WizardProjectsImportPage wpip = getNewWizard();
			// We're importing a directory
			wpip.getProjectFromDirectoryRadio().setSelection((true)); 
			wpip.updateProjectsList(wsPath.toOSString());

			ProjectRecord[] selectedProjects = wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertEquals("there should only be the WorldHello project left", 
					1, projectNames.size());
			
			assertTrue("HelloWorld project should not be found in project list.",
					!projectNames.contains("HelloWorld"));
		} catch (Exception e) {
			fail(e.toString());
		}		
	}
	
	public void testImportSingleZip() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".zip")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}

	}
	
	public void testImportZipWithEmptyFolder(){
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX + ARCHIVE_FILE_WITH_EMPTY_FOLDER + ".zip")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("A");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], ARCHIVE_FILE_EMPTY_FOLDER_LIST, false);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}
	}
	
	public void testImportSingleTar() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
            URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
            		new Path(DATA_PATH_PREFIX + ARCHIVE_HELLOWORLD + ".tar")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}

	}
	
	public void testImportTarWithEmptyFolder(){
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
            URL archiveFile = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
            		new Path(DATA_PATH_PREFIX + ARCHIVE_FILE_WITH_EMPTY_FOLDER + ".tar")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("A");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(archiveFile.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in tar", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("A");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], ARCHIVE_FILE_EMPTY_FOLDER_LIST, false);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}

	}
	
	public void testImportSingleDirectory() {
		IPath wsPath = null;
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			
            dataLocation = copyDataLocation();
            wsPath = new Path(dataLocation).append(PLUGIN_ID).append(DATA_PATH_PREFIX).append("HelloWorld");
            WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((true)); 
			wpip.updateProjectsList(wsPath.toOSString());
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in directory", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(false, workspaceProjects[0], FILE_LIST, true);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}
	}
	
	public void testImportSingleDirectoryWithCopy() {
		IPath wsPath = null;
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			
            dataLocation = copyDataLocation();
            wsPath = new Path(dataLocation).append(PLUGIN_ID).append(DATA_PATH_PREFIX).append("HelloWorld");
            WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((true));
			wpip.getCopyCheckbox().setSelection(true);
			wpip.saveWidgetValues();
			wpip.restoreWidgetValues();

			wpip.updateProjectsList(wsPath.toOSString());
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}
	}
	
	public void testImportSingleDirectoryWithCopyDeleteProjectKeepContents(){
		IPath wsPath = null;
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			
            dataLocation = copyDataLocation();
            wsPath = new Path(dataLocation).append(PLUGIN_ID).append(DATA_PATH_PREFIX).append("HelloWorld");
            WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((true));
			wpip.getCopyCheckbox().setSelection(true);
			wpip.saveWidgetValues();
			wpip.restoreWidgetValues();

			wpip.updateProjectsList(wsPath.toOSString());
			ProjectRecord[] selectedProjects= wpip.getValidProjects();
			ArrayList projectNames = new ArrayList();
			for (int i = 0; i < selectedProjects.length; i++) {
				projectNames.add(selectedProjects[i].getProjectName());
			}

			assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

			CheckboxTreeViewer projectsList= wpip.getProjectsList();
			projectsList.setCheckedElements(selectedProjects);
			wpip.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported: " + workspaceProjects.length + " projects in workspace.");
			IFolder helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);
			
			// delete projects in workspace but not contents
			for (int i = 0; i < workspaceProjects.length; i++){
				workspaceProjects[i].delete(false, true, null);
			}
			assertTrue("Test project not deleted successfully.", root.getProjects().length == 0);
			
			// perform same test again, but this time import from this workspace
			final WizardProjectsImportPage wpip2 = getNewWizard();
			HashSet projects2 = new HashSet();
			projects2.add("HelloWorld");
			
			wpip2.getProjectFromDirectoryRadio().setSelection((true));
			wpip2.getCopyCheckbox().setSelection(true);
			wpip2.saveWidgetValues();
			wpip2.restoreWidgetValues();

			wpip2.updateProjectsList(wsPath.toOSString());
			ProjectRecord[] selectedProjects2 = wpip2.getValidProjects();
			assertTrue("Not all projects were found correctly in zip (2).", selectedProjects2.length == 1);
			
			ArrayList projectNames2 = new ArrayList();
			for (int i = 0; i < selectedProjects2.length; i++) {
				projectNames2.add(selectedProjects2[i].getProjectName());
			}
			
			assertTrue("Not all projects were found correctly in zip (2)", projectNames2.containsAll(projects2));

			CheckboxTreeViewer projects2List= wpip2.getProjectsList();
			projects2List.setCheckedElements(selectedProjects2);
           	wpip2.createProjects(); // Try importing all the projects we found

			// "HelloWorld" should be the only project in the workspace
			workspaceProjects = root.getProjects();
			if (workspaceProjects.length != 1)
				fail("Incorrect Number of projects imported");
			helloFolder = workspaceProjects[0].getFolder("HelloWorld");
			if (helloFolder.exists())
				fail("Project was imported as a folder into itself (2)");
			
			verifyProjectInWorkspace(true, workspaceProjects[0], FILE_LIST, true);

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}
	}
	
	/**
	 * Verify whether or not the imported project is in the current workspace location
	 * (i.e. copy projects was true) or in another workspace location (i.e. copy projects
	 * was false).  
	 * 
	 * @param inWorkspace
	 * @param project
	 */
	private void verifyProjectInWorkspace(final boolean inWorkspace, 
			final IProject project, String[] fileList, boolean isListFiles){
		try {
			Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, new NullProgressMonitor());
		} catch (OperationCanceledException e) {
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			fail(e.getLocalizedMessage());
		}
		
		IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath projectLocation = project.getLocation();
		boolean isProjectInWorkspace = rootLocation.isPrefixOf(projectLocation);
		if (inWorkspace){
			if (!isProjectInWorkspace)
				fail(project.getName() + " should be in the workspace location: " + rootLocation.toOSString());
		}
		else{
			if (isProjectInWorkspace)
				fail(project.getName() + " should not be in the workspace location: " + rootLocation.toOSString());
		}
		StringBuffer filesNotImported = new StringBuffer();
		// make sure the files in the project were imported
		for (int i = 0; i < fileList.length; i++){
			IResource res = isListFiles ? (IResource)project.getFile(fileList[i]) 
					: (IResource)project.getFolder(fileList[i]);
			if (!res.exists())
				filesNotImported.append(res.getName() + ", ");
		}
		assertTrue(
				"Files expected but not in workspace: " + filesNotImported.toString(), 
				filesNotImported.length() == 0);	
	}
	
	/**
	 * Copies the data to a temporary directory and returns the new location.
	 * 
	 * @return the location
	 */
	private String copyDataLocation() throws IOException {
        TestPlugin plugin = TestPlugin.getDefault();
        if (plugin == null)
            throw new IllegalStateException(
                    "TestPlugin default reference is null");
        
        URL fullPathString = plugin.getDescriptor().find(
				new Path(WS_DATA_PREFIX).append(WS_DATA_LOCATION + ".zip"));
        
        if (fullPathString == null) 
        	throw new IllegalArgumentException();
        
        IPath path = new Path(fullPathString.getPath());

        File origin = path.toFile();
        if (!origin.exists())
			throw new IllegalArgumentException();
        
        ZipFile zFile = new ZipFile(origin);        
		
		File destination = new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()).toOSString());
		FileTool.unzip(zFile, destination);
		return destination.getAbsolutePath();
	}
	
	private WizardProjectsImportPage getNewWizard(){
		ImportExportWizard wizard = new ImportExportWizard(ImportExportWizard.IMPORT);
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
		.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
		.getSection("ImportExportTests");
		if (wizardSettings == null)
			wizardSettings = workbenchSettings
			.addNewSection("ImportExportTests");
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);
		
		WizardProjectsImportPage wpip = new WizardProjectsImportPage();
		
		Shell shell = getShell();
		
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(100, dialog.getShell().getSize().x),
				100);

		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());		
		wpip.setWizard(dialog.getCurrentPage().getWizard());
		wpip.createControl(parent);
		return wpip;
	}
}
