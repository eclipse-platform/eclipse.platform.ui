/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.patch;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;


/* package */ class PatchWizard extends Wizard {
	
	// dialog store id constants
	private final static String DIALOG_SETTINGS_KEY= "PatchWizard"; //$NON-NLS-1$

	private boolean fHasNewDialogSettings;
	private Diff[] fDiffs;
	private ISelection fSelection;
	private InputPatchPage fPatchWizardPage;
	private PreviewPatchPage fPreviewPatchPage;
	
		
	/**
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	/* package */ PatchWizard(ISelection selection) {
		
		fSelection= selection;
		
		setWindowTitle("Apply Patch");

		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY); //$NON-NLS-1$
		if (section == null)
			fHasNewDialogSettings= true;
		else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}
	}
	
	/* package */ Diff[] getDiffs() {
		return fDiffs;
	}

	/* package */ void setDiffs(Diff[] diffs) {
		fDiffs= diffs;
	}
			
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		
		addPage(fPatchWizardPage= new InputPatchPage());
		addPage(fPreviewPatchPage= new PreviewPatchPage());
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		
		CompareConfiguration cc= new CompareConfiguration();
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
		PatchCompareInput input= new PatchCompareInput(cc);
		input.setSelection(fSelection);
		input.setDiffs(fDiffs);
		input.setPatchName(fPatchWizardPage.getPatchName());
		CompareUI.openCompareEditor(input);
		
		
//
//		if (!executeExportOperation(new JarFileExportOperation(fJarPackage, getShell())))
//			return false;
//		
		// Save the dialog settings
		if (fHasNewDialogSettings) {
			IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
			section= workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
			setDialogSettings(section);
		}
		
		fPatchWizardPage.saveWidgetValues();
		//fPreviewPatchPage.saveWidgetValues();
		
		return true;
	}
}

