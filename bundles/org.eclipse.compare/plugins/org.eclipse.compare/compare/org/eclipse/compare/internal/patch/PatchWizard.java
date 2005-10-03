/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.compare.internal.*;


/* package */ class PatchWizard extends Wizard {
	
	// dialog store id constants
	private final static String DIALOG_SETTINGS_KEY= "PatchWizard"; //$NON-NLS-1$

	private boolean fHasNewDialogSettings;
	
	private InputPatchPage fPatchWizardPage;
	
	private Patcher fPatcher;
	private IResource fTarget;

		
	/*
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	/* package */ PatchWizard(ISelection selection) {
		
		setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.gif"));	//$NON-NLS-1$
		setWindowTitle(PatchMessages.PatchWizard_title); 

		setTargets(Utilities.getResources(selection));

		fPatcher= new Patcher();
				
		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
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
	
	IResource getTarget() {
		return fTarget;
	}
	
	void setTargets(IResource[] targets) {
		if (targets != null && targets.length > 0)
			fTarget= targets[0];	// right now we can only deal with a single selection
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		
		addPage(fPatchWizardPage= new InputPatchPage(this));
		addPage(new PreviewPatchPage(this));
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
		
		fPatcher.setName(fPatchWizardPage.getPatchName());

		try {
			WorkspaceModifyOperation op= new WorkspaceModifyOperation(fTarget.getProject()) {
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						fPatcher.applyAll(getTarget(), monitor, getShell(), PatchMessages.PatchWizard_title); 
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			getContainer().run(true, false, op);

		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e,
					PatchMessages.PatchWizard_title,	
					PatchMessages.PatchWizard_unexpectedException_message);	
		} catch (InterruptedException e) {
			// cannot happen
			// NeedWork: use assert!
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

