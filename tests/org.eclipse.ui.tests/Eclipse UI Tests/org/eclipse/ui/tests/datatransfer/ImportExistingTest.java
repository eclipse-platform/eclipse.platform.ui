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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.performance.FileTool;
import org.eclipse.ui.tests.util.DialogCheck;
import org.eclipse.ui.tests.util.FileUtil;

public class ImportExistingTest extends DataTransferTestCase {
	private static final String PLUGIN_ID = "org.eclipse.ui.tests";
	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";
	private static final String WS_DATA_PREFIX = "data/workspaces";
	private static final String WS_DATA_LOCATION = "importExistingFromDirTest";
	
	private String dataLocation = null;
	
	public ImportExistingTest(String testName) {
		super(testName);
	}
	
	private Shell getShell() {
		return DialogCheck.getShell();
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
				root.delete();
			}
		}
		dataLocation = null;	// reset for next test
	}

	public void testFindSingleZip() {
		try {
			URL helloworld = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX+"helloworld.zip")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(helloworld.getPath());

			ProjectRecord[] selectedProjects= wpip.getProjects();
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
			URL helloworld = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX+"helloworld.tar")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(helloworld.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getProjects();
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

			ProjectRecord[] selectedProjects = wpip.getProjects();
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
	
	public void testImportSingleZip() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProject[] workspaceProjects = root.getProjects();
            for (int i = 0; i < workspaceProjects.length; i++) 
            	FileUtil.deleteProject(workspaceProjects[i]);
			URL helloworld = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
					new Path(DATA_PATH_PREFIX+"helloworld.zip")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(helloworld.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getProjects();
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
            URL helloworld = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
            		new Path(DATA_PATH_PREFIX+"helloworld.tar")));
			WizardProjectsImportPage wpip = getNewWizard();
			HashSet projects = new HashSet();
			projects.add("HelloWorld");
			
			wpip.getProjectFromDirectoryRadio().setSelection((false)); //We want the other one selected
			wpip.updateProjectsList(helloworld.getPath());
			
			ProjectRecord[] selectedProjects= wpip.getProjects();
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
			ProjectRecord[] selectedProjects= wpip.getProjects();
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

		} catch (IOException e) {
			fail(e.toString());
		} catch (CoreException e) {
			fail(e.toString());
		}
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
		WizardProjectsImportPage wpip = new WizardProjectsImportPage();
		
		Shell shell = getShell();
		wpip.createControl(shell);
		
		ImportExportWizard wizard = new ImportExportWizard();
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
		.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
		.getSection("ImportExportAction");
		if (wizardSettings == null)
			wizardSettings = workbenchSettings
			.addNewSection("ImportExportAction");
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);
		
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(100, dialog.getShell().getSize().x),
				100);
		
		wpip.setWizard(dialog.getCurrentPage().getWizard());
		return wpip;
	}
}
