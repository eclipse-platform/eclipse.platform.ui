/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.core.resources.IResource;

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
	private IResource fTarget;

		
	/**
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	/* package */ PatchWizard(ISelection selection) {
		
		fTarget= getResource(selection);

		fPatcher= new Patcher();
		
		setWindowTitle("Resource Patcher");
		
		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY); //$NON-NLS-1$
		if (section == null)
			fHasNewDialogSettings= true;
		else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}	
	}
	
	static IResource getResource(ISelection selection) {
		IResource[] rs= Utilities.getResources(selection);
		if (rs != null && rs.length > 0)
			return rs[0];
		return null;
	}
		
	Patcher getPatcher() {
		return fPatcher;
	}
	
	IResource getTarget() {
		return fTarget;
	}
	
	void setTarget(IResource target) {
		fTarget= target;
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

		CompareUI.openCompareEditor(new PatchCompareInput(cc, fPatcher, new StructuredSelection(fTarget)));

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

