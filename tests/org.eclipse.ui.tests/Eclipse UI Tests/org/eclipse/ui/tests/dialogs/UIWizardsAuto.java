/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.ui.tests.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ExportWizard;
import org.eclipse.ui.internal.dialogs.ImportWizard;
import org.eclipse.ui.internal.dialogs.NewWizard;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class UIWizardsAuto {
	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	private static final int SIZING_WIZARD_WIDTH = 470;

	private static final int SIZING_WIZARD_HEIGHT = 550;

	private static final int SIZING_WIZARD_WIDTH_2 = 500;

	private static final int SIZING_WIZARD_HEIGHT_2 = 500;

	private IProject project;

	private Shell getShell() {
		return DialogCheck.getShell();
	}

	private IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	private WizardDialog exportWizard(IWizardPage page) {
		ExportWizard wizard = new ExportWizard();
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
				.getSection("ExportResourcesAction");
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
					.addNewSection("ExportResourcesAction");
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IWorkbenchHelpContextIds.EXPORT_WIZARD);

		if (page != null) {
			page.setWizard(wizard);
			dialog.showPage(page);
		}
		return dialog;
	}

	private WizardDialog importWizard(IWizardPage page) {
		ImportWizard wizard = new ImportWizard();
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
				.getSection("ImportResourcesAction");
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
					.addNewSection("ImportResourcesAction");
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IWorkbenchHelpContextIds.IMPORT_WIZARD);

		if (page != null) {
			page.setWizard(wizard);
			dialog.showPage(page);
		}
		return dialog;
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		if (project != null) {
			project.delete(true, true, null);
			project = null;
		}
	}

	@Test
	public void testExportResources() {//reference: ExportResourcesAction
		Dialog dialog = exportWizard(null);
		DialogCheck.assertDialogTexts(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testFileSystemExport() {
//		Dialog dialog = exportWizard(DataTransferTestStub.newFileSystemResourceExportPage1(null));
//		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testZipFileExport() {
//		Dialog dialog = exportWizard(DataTransferTestStub.newZipFileResourceExportPage1(null));
//		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testImportResources() {//reference: ImportResourcesAction
		Dialog dialog = importWizard(null);
		DialogCheck.assertDialogTexts(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testFileSystemImport() {
//		Dialog dialog = importWizard(DataTransferTestStub.newFileSystemResourceImportPage1(
//				WorkbenchPlugin.getDefault().getWorkbench(), StructuredSelection.EMPTY));
//		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testZipFileImport() {
//		Dialog dialog = importWizard(DataTransferTestStub.newZipFileResourceImportPage1(null));
//		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewFile() {
		BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("CreateFileAction_title");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_FILE_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	/**
	 * Test for bug 30719 [Linked Resources] NullPointerException when setting filename for WizardNewFileCreationPage
	 */
	@Test
	public void testNewFile2() {
		BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard() {
			@Override
			public void addPages() {
				super.addPages();
				IWizardPage page = getPage("newFilePage1");
				assertTrue("Expected newFilePage1",
						page instanceof WizardNewFileCreationPage);
				WizardNewFileCreationPage fileCreationPage = (WizardNewFileCreationPage) page;

				try {
					project = FileUtil.createProject("testNewFile2");
				} catch (CoreException e) {
					fail(e.getMessage());
				}
				fileCreationPage.setContainerFullPath(project.getFullPath());
				fileCreationPage.setFileName("testFileName.test");
			}
		};

		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("CreateFileAction_title");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_FILE_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewFolder() {
		BasicNewFolderResourceWizard wizard = new BasicNewFolderResourceWizard();
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("CreateFolderAction_title");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_FOLDER_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewProjectPage1() {
		BasicNewProjectResourceWizard wizard = new BasicNewProjectResourceWizard();
		wizard.init(PlatformUI.getWorkbench(), null);
		wizard.setNeedsProgressMonitor(true);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		dialog.getShell().setText("CreateFileAction_title");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_PROJECT_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewProjectPage2() {
		BasicNewProjectResourceWizard wizard = new BasicNewProjectResourceWizard();
		wizard.init(PlatformUI.getWorkbench(), null);
		wizard.setNeedsProgressMonitor(true);

		WizardNewProjectReferencePage page = new WizardNewProjectReferencePage(
				"basicReferenceProjectPage");//$NON-NLS-1$
		page.setTitle(ResourceMessages.NewProject_referenceTitle);
		page.setDescription(ResourceMessages.NewProject_referenceDescription);
		page.setWizard(wizard);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		dialog.getShell().setText("CreateFileAction_title");
		dialog.showPage(page);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_PROJECT_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewProject() {
		// Create wizard selection wizard.
		NewWizard wizard = new NewWizard();
		wizard.setProjectsOnly(true);
		initNewWizard(wizard);

		// Create wizard dialog.
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_PROJECT_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testNewResource() {
		NewWizard wizard = new NewWizard();
		initNewWizard(wizard);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IWorkbenchHelpContextIds.NEW_WIZARD);
		DialogCheck.assertDialogTexts(dialog);
	}

	@Test
	public void testWizardWindowTitle() {

		checkWizardWindowTitle(null);
		checkWizardWindowTitle("My New Wizard"); //$NON-NLS-1$

	}

	private void checkWizardWindowTitle(String windowTitle) {

		NewWizard newWizard = new NewWizard();
		newWizard.setWindowTitle(windowTitle);

		initNewWizard(newWizard);

		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				newWizard);
		dialog.create();

		if(windowTitle == null) {
			windowTitle = WorkbenchMessages.NewWizard_title;
		}

		assertEquals(windowTitle, dialog.getShell().getText());

		dialog.close();
	}

	private void initNewWizard(NewWizard wizard) {
		ISelection selection = getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().getSelection();
		IStructuredSelection selectionToPass = null;
		if (selection instanceof IStructuredSelection sse) {
			selectionToPass = sse;
		} else {
			selectionToPass = StructuredSelection.EMPTY;
		}
		wizard.init(getWorkbench(), selectionToPass);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
				.getSection("NewWizardAction");//$NON-NLS-1$
		if (wizardSettings == null)
		 {
			wizardSettings = workbenchSettings.addNewSection("NewWizardAction");//$NON-NLS-1$
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);
	}


}

