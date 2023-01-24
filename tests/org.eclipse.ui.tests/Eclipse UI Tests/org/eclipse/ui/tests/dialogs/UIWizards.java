/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ExportWizard;
import org.eclipse.ui.internal.dialogs.ImportWizard;
import org.eclipse.ui.internal.dialogs.NewWizard;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.junit.Ignore;
import org.junit.Test;

public class UIWizards {
	private static final int SIZING_WIZARD_WIDTH = 470;

	private static final int SIZING_WIZARD_HEIGHT = 550;

	private static final int SIZING_WIZARD_WIDTH_2 = 500;

	private static final int SIZING_WIZARD_HEIGHT_2 = 500;

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

	@Test
	public void testExportResources() {//reference: ExportResourcesAction
		Dialog dialog = exportWizard(null);
		DialogCheck.assertDialog(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testFileSystemExport() {
//		Dialog dialog = exportWizard(DataTransferTestStub.newFileSystemResourceExportPage1(null));
//		DialogCheck.assertDialog(dialog);
	}

	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testZipFileExport() {
//		Dialog dialog = exportWizard(DataTransferTestStub.newZipFileResourceExportPage1(null));
//		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testImportResources() {//reference: ImportResourcesAction
		Dialog dialog = importWizard(null);
		DialogCheck.assertDialog(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testFileSystemImport() {
//		Dialog dialog = importWizard(DataTransferTestStub.newFileSystemResourceImportPage1(
//				WorkbenchPlugin.getDefault().getWorkbench(), StructuredSelection.EMPTY));
//		DialogCheck.assertDialog(dialog);
	}

	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testZipFileImport() {
//		Dialog dialog = importWizard(DataTransferTestStub.newZipFileResourceImportPage1(null));
//		DialogCheck.assertDialog(dialog);
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
		DialogCheck.assertDialog(dialog);
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
		DialogCheck.assertDialog(dialog);
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
		DialogCheck.assertDialog(dialog);
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
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testNewProject() {
		// Create wizard selection wizard.
		NewWizard wizard = new NewWizard();
		wizard.setProjectsOnly(true);
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

		// Create wizard dialog.
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IIDEHelpContextIds.NEW_PROJECT_WIZARD);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testNewResource() {
		NewWizard wizard = new NewWizard();
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

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT_2);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IWorkbenchHelpContextIds.NEW_WIZARD);
		DialogCheck.assertDialog(dialog);
	}
}

