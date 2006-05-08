/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import junit.framework.TestCase;

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
import org.eclipse.ui.help.WorkbenchHelp;
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

public class UIWizards extends TestCase {
    private static final int SIZING_WIZARD_WIDTH = 470;

    private static final int SIZING_WIZARD_HEIGHT = 550;

    private static final int SIZING_WIZARD_WIDTH_2 = 500;

    private static final int SIZING_WIZARD_HEIGHT_2 = 500;

    public UIWizards(String name) {
        super(name);
    }

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
        if (wizardSettings == null)
            wizardSettings = workbenchSettings
                    .addNewSection("ExportResourcesAction");
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        WorkbenchHelp.setHelp(dialog.getShell(), IWorkbenchHelpContextIds.EXPORT_WIZARD);

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
        if (wizardSettings == null)
            wizardSettings = workbenchSettings
                    .addNewSection("ImportResourcesAction");
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        WorkbenchHelp.setHelp(dialog.getShell(), IWorkbenchHelpContextIds.IMPORT_WIZARD);

        if (page != null) {
            page.setWizard(wizard);
            dialog.showPage(page);
        }
        return dialog;
    }

    public void testExportResources() {//reference: ExportResourcesAction
        Dialog dialog = exportWizard(null);
        DialogCheck.assertDialog(dialog, this);
    }

    /**
     * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
     * 
     public void testFileSystemExport() {
     Dialog dialog = exportWizard( DataTransferTestStub.newFileSystemResourceExportPage1(null) );
     DialogCheck.assertDialog(dialog, this);
     }
     public void testZipFileExport() {
     Dialog dialog = exportWizard( DataTransferTestStub.newZipFileResourceExportPage1(null) );
     DialogCheck.assertDialog(dialog, this);
     }
     */
    public void testImportResources() {//reference: ImportResourcesAction
        Dialog dialog = importWizard(null);
        DialogCheck.assertDialog(dialog, this);
    }

    /**
     * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
     * 
     public void testFileSystemImport() {
     Dialog dialog = importWizard( DataTransferTestStub.newFileSystemResourceImportPage1(WorkbenchPlugin.getDefault().getWorkbench(), StructuredSelection.EMPTY) );
     DialogCheck.assertDialog(dialog, this);
     }
     public void testZipFileImport() {
     Dialog dialog = importWizard( DataTransferTestStub.newZipFileResourceImportPage1(null) );
     DialogCheck.assertDialog(dialog, this);
     }
     */
    public void testNewFile() {
        BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
        wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
        wizard.setNeedsProgressMonitor(true);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setText("CreateFileAction_title"); 
        WorkbenchHelp.setHelp(dialog.getShell(),
                IIDEHelpContextIds.NEW_FILE_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testNewFolder() {
        BasicNewFolderResourceWizard wizard = new BasicNewFolderResourceWizard();
        wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
        wizard.setNeedsProgressMonitor(true);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setText("CreateFolderAction_title"); 
        WorkbenchHelp.setHelp(dialog.getShell(),
        		IIDEHelpContextIds.NEW_FOLDER_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }

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
        WorkbenchHelp.setHelp(dialog.getShell(),
        		IIDEHelpContextIds.NEW_PROJECT_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }

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
        WorkbenchHelp.setHelp(dialog.getShell(),
        		IIDEHelpContextIds.NEW_PROJECT_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testNewProject() {
        // Create wizard selection wizard.
        NewWizard wizard = new NewWizard();
        wizard.setProjectsOnly(true);
        ISelection selection = getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService().getSelection();
        IStructuredSelection selectionToPass = null;
        if (selection instanceof IStructuredSelection)
            selectionToPass = (IStructuredSelection) selection;
        else
            selectionToPass = StructuredSelection.EMPTY;
        wizard.init(getWorkbench(), selectionToPass);
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings wizardSettings = workbenchSettings
                .getSection("NewWizardAction");//$NON-NLS-1$
        if (wizardSettings == null)
            wizardSettings = workbenchSettings.addNewSection("NewWizardAction");//$NON-NLS-1$
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        // Create wizard dialog.
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT_2);
        WorkbenchHelp.setHelp(dialog.getShell(),
        		IIDEHelpContextIds.NEW_PROJECT_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testNewResource() {
        NewWizard wizard = new NewWizard();
        ISelection selection = getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService().getSelection();
        IStructuredSelection selectionToPass = null;
        if (selection instanceof IStructuredSelection)
            selectionToPass = (IStructuredSelection) selection;
        else
            selectionToPass = StructuredSelection.EMPTY;
        wizard.init(getWorkbench(), selectionToPass);
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings wizardSettings = workbenchSettings
                .getSection("NewWizardAction");//$NON-NLS-1$
        if (wizardSettings == null)
            wizardSettings = workbenchSettings.addNewSection("NewWizardAction");//$NON-NLS-1$
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH_2, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT_2);
        WorkbenchHelp.setHelp(dialog.getShell(), IWorkbenchHelpContextIds.NEW_WIZARD);
        DialogCheck.assertDialog(dialog, this);
    }
}

