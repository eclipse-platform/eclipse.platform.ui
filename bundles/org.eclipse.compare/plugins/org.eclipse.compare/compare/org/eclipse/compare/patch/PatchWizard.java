/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.Differencer;


/* package */ class PatchWizard extends Wizard {
	
	// dialog store id constants
	private final static String DIALOG_SETTINGS_KEY= "PatchWizard"; //$NON-NLS-1$

	private boolean fHasNewDialogSettings;
	
	private InputPatchPage fPatchWizardPage;
	private PreviewPatchPage fPreviewPatchPage;
	
	private Patcher fPatcher;
	private ISelection fSelection;

		
	/**
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	/* package */ PatchWizard(ISelection selection) {
		
		fSelection= selection;
		fPatcher= new Patcher();
		
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
	
	Patcher getPatcher() {
		return fPatcher;
	}
	
	ISelection getSelection() {
		return fSelection;
	}
	
	/*package */ IFile existsInSelection(IPath path) {
		if (fSelection != null && !fSelection.isEmpty()) {
			IResource[] selection= Utilities.getResources(fSelection);
			for (int i= 0; i < selection.length; i++) {
				IResource r= selection[i];
				if (r instanceof IContainer) {
					IContainer c= (IContainer) r;
					if (c.exists(path))
						return c.getFile(path);
				}
			} 
		}
		return null;
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
		
		CompareConfiguration cc= new CompareConfiguration() {
			public Image getImage(int kind) {
				if (kind == Differencer.ADDITION)
					kind= Differencer.DELETION;
				else if (kind == Differencer.DELETION)
					kind= Differencer.ADDITION;
				return super.getImage(kind);
			}
			public Image getImage(Image base, int kind) {
				if (kind == Differencer.ADDITION)
					kind= Differencer.DELETION;
				else if (kind == Differencer.DELETION)
					kind= Differencer.ADDITION;
				return super.getImage(base, kind);
			}
		};
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));

		fPatcher.setName(fPatchWizardPage.getPatchName());
		fPatcher.setStripPrefixSegments(fPreviewPatchPage.getStripPrefixSegments());
		fPatcher.setFuzz(3);
		fPatcher.setIgnoreWhitespace(true);
		
		CompareUI.openCompareEditor(new PatchCompareInput(cc, fPatcher, fSelection));

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

