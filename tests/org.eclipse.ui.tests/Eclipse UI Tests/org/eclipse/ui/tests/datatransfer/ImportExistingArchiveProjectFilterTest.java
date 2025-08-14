/*******************************************************************************
 * Copyright (c) ETAS GmbH 2023, all rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETAS GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.datatransfer;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImportExistingArchiveProjectFilterTest extends UITestCase {

	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";
	private static final String ARCHIVE_JAVA_PROJECT = "ExcludeFilter_Import";

	public ImportExistingArchiveProjectFilterTest() {
		super(ImportExistingArchiveProjectFilterTest.class.getName());
	}

	@Override
	protected void doTearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = wsRoot.getProjects();
		for (int i = projects.length - 1; i >= 0; i--) {
			FileUtil.deleteProject(projects[i]);
		}
		super.doTearDown();
	}

	// Testcase for GitHub Issue
	// https://github.com/eclipse-platform/eclipse.platform.ui/issues/748
	@Test
	public void testFolderVisibilityPostZipProjectImport()
			throws CoreException, IOException, OperationCanceledException, InterruptedException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}

		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				new Path(DATA_PATH_PREFIX + ARCHIVE_JAVA_PROJECT + ".zip"), null));

		WizardProjectsImportPage wpip = getNewWizard();
		Set<String> projects = Set.of(ARCHIVE_JAVA_PROJECT);

		wpip.getProjectFromDirectoryRadio().setSelection((false));
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
		wpip.createProjects();

		workspaceProjects = root.getProjects();
		assertEquals("Incorrect Number of projects imported", 1, workspaceProjects.length);

		IWorkbenchPage page = getWorkbench().showPerspective(EmptyPerspective.PERSP_ID,
				getWorkbench().getActiveWorkbenchWindow());
		page.hideView(page.findView("org.eclipse.ui.internal.introview"));
		IViewPart navigator = page.showView(IPageLayout.ID_PROJECT_EXPLORER);
		assertNotNull("failed to open project explorer", navigator);

		ProjectExplorer projectExplorer = (ProjectExplorer) navigator;
		// Check project explorer for visibility of res folder for which resource filter
		// is applied to hide on import
		TreeViewer treeViewer = projectExplorer.getCommonViewer();
		projectExplorer.getCommonViewer().expandToLevel(2);
		processEvents();

		// Check Project explorer tree viewer if res folder is present

		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);

		ITreeContentProvider contentProvider = (ITreeContentProvider) treeViewer.getContentProvider();
		Object[] rootElements = contentProvider.getElements(treeViewer.getInput());
		for (Object rootElement : rootElements) {
			processElementAndChildren(rootElement, contentProvider);
		}
	}

	/**
	 * @param rootElement
	 * @param contentProvider
	 */
	private void processElementAndChildren(Object element, ITreeContentProvider contentProvider) {
		if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			assertFalse(folder.getName().equalsIgnoreCase("res"));
		} else if (element instanceof IResource) { // to expensive to walk other contributions like whole JRE from JDT
			Object[] children = contentProvider.getChildren(element);
			for (Object child : children) {
				processElementAndChildren(child, contentProvider);
			}
		}
	}

	private WizardDialog dialog;
	public WizardProjectsImportPage getNewWizard() {
		ImportExportWizard wizard = new ImportExportWizard(ImportExportWizard.IMPORT);
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("ImportExportTests");
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings.addNewSection("ImportExportTests");
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
		dialog.getShell().setSize(Math.max(100, dialog.getShell().getSize().x), 100);

		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		wpip.setWizard(dialog.getCurrentPage().getWizard());
		wpip.createControl(parent);
		return wpip;
	}

	private Shell getShell() {
		return getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}
