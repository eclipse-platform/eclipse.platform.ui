/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

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
		
		setTargets(getResource(selection));

		fPatcher= new Patcher();
		
		setWindowTitle(PatchMessages.getString("PatchWizard.title")); //$NON-NLS-1$
		
		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY); //$NON-NLS-1$
		if (section == null)
			fHasNewDialogSettings= true;
		else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}	
	}
	
	static IResource[] getResource(ISelection selection) {
		IResource[] rs= Utilities.getResources(selection);
		ArrayList list= null;
		for (int i= 0; i < rs.length; i++) {
			IResource r= rs[i];
			if (r != null && r.isAccessible()) {
				if (list == null)
					list= new ArrayList();
				list.add(r);
			}
		}
		if (list != null && list.size() > 0)
			return (IResource[]) list.toArray(new IResource[list.size()]);
		return null;
	}
		
	Patcher getPatcher() {
		return fPatcher;
	}
	
	IResource getTarget() {
		return fTarget;
	}
	
	void setTargets(IResource[] targets) {
		fTarget= targets[0];	// right now we can only deal with a single selection
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
	public boolean needsProgressMonitor() {
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		
		if (false) {
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
		} else {
			fPatcher.setName(fPatchWizardPage.getPatchName());

			try {
				WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							fPatcher.applyAll(getTarget(), monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
				getContainer().run(true, false, op);

			} catch (InvocationTargetException e) {
				ExceptionHandler.handle(e,
						PatchMessages.getString("PatchWizard.title"),	//$NON-NLS-1$ 
						PatchMessages.getString("PatchWizard.unexpectedException.message"));	//$NON-NLS-1$
			} catch (InterruptedException e) {
				// cannot happen
			}
		}
		
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

