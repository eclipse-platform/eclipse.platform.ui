/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

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
	
	/*package */ IFile existsInSelection(String path) {
		if (fSelection == null || fSelection.isEmpty())
			return null;
		
		IPath p= new Path(path);
		IPath p2= p.removeFirstSegments(1);
		
		IResource[] selection= Utilities.getResources(fSelection);
		for (int i= 0; i < selection.length; i++) {
			IResource r= selection[i];
			if (r instanceof IContainer) {
				IContainer c= (IContainer) r;
				if (c.exists(p2)) {
					return c.getFile(p2);
				}
			}
		}
		return null;
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
		
		addPage(fPatchWizardPage= new InputPatchPage(this));
		addPage(fPreviewPatchPage= new PreviewPatchPage(this));
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		
		CompareConfiguration cc= new CompareConfiguration();
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));

		CompareUI.openCompareEditor(
			new PatchCompareInput(cc, fSelection, fDiffs, fPatchWizardPage.getPatchName()));

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

