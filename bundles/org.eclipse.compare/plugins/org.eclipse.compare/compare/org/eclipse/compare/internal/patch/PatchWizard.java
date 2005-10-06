/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ExceptionHandler;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/* package */class PatchWizard extends Wizard {

	// dialog store id constants
	private final static String DIALOG_SETTINGS_KEY= "PatchWizard"; //$NON-NLS-1$

	private boolean fHasNewDialogSettings;
	
	private InputPatchPage fPatchWizardPage;

	private WorkspacePatcher fPatcher;

	/*
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	/* package */PatchWizard(ISelection selection) {

		setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.gif")); //$NON-NLS-1$
		setWindowTitle(PatchMessages.PatchWizard_title);

		fPatcher= new WorkspacePatcher();
		setTarget(Utilities.getResource(selection));

		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		if (section==null)
			fHasNewDialogSettings= true;
		else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}
	}

	WorkspacePatcher getPatcher() {
		return fPatcher;
	}
	
	IResource getTarget() {
		return fPatcher.getTarget();
	}

	void setTarget(IResource target) {
		fPatcher.setTarget(target);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		
		addPage(fPatchWizardPage= new InputPatchPage(this));
		addPage(new PatchTargetPage(this));
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
			//Create scheduling rule based on the type of patch - single or workspace
			ISchedulingRule scheduleRule= null;
			if (fPatcher.isWorkspacePatch()) {
				//workspace patch
				scheduleRule= new MultiRule(fPatcher.getTargetProjects());
			} else {
				//single patch
				scheduleRule= getTarget();
			}

			WorkspaceModifyOperation op= new WorkspaceModifyOperation(scheduleRule) {
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						fPatcher.applyAll(monitor, getShell(), PatchMessages.PatchWizard_title);
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

