/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardPage;

/**
 * Presents the list of failed preconditions to the user
 */
public class ErrorWizardPage extends RefactoringWizardPage {
		
	public static final String PAGE_NAME= "ErrorPage"; //$NON-NLS-1$
	
	private RefactoringStatus fStatus;
	private RefactoringStatusViewer fViewer;
	
	public ErrorWizardPage() {
		super(PAGE_NAME);
	}
	
	/**
	 * Sets the page's refactoring status to the given value.
	 * @param status the refactoring status.
	 */
	public void setStatus(RefactoringStatus status) {
		fStatus= status;
		if (fStatus != null) {
			setPageComplete(isRefactoringPossible());
			int severity= fStatus.getSeverity();
			if (severity >= RefactoringStatus.FATAL) {
				setDescription(RefactoringUIMessages.getString("ErrorWizardPage.cannot_proceed")); //$NON-NLS-1$
			} else if (severity >= RefactoringStatus.INFO) {
				setDescription(RefactoringUIMessages.getString("ErrorWizardPage.confirm")); //$NON-NLS-1$
			} else {
				setDescription(""); //$NON-NLS-1$
			}
		} else {
			setPageComplete(true);
			setDescription(""); //$NON-NLS-1$
		}	
	}
	
	public RefactoringStatus getStatus() {
		return fStatus;
	}
	
	//---- UI creation ----------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		setControl(fViewer= new RefactoringStatusViewer(parent, SWT.NONE));
		Dialog.applyDialogFont(fViewer);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IRefactoringHelpContextIds.REFACTORING_ERROR_WIZARD_PAGE);			
	}
	
	//---- Reimplementation of WizardPage methods ------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IDialog.
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			fViewer.setStatus(fStatus);
		} else {
			// the page was not complete if we show a fatal error. In this
			// case we can finish anyway. To enable the OK and Preview button
			// on the user input page we have to mark the page as complete again.
			if (!isPageComplete() && fStatus.hasFatalError())
				setPageComplete(true);
		}
		super.setVisible(visible);
	}
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		// We have to call super.getNextPage since computing the next
		// page is expensive. So we avoid it as long as possible.
		return fStatus != null && isRefactoringPossible() &&
			   isPageComplete() && super.getNextPage() != null;
	}
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public IWizardPage getNextPage() {
		RefactoringWizard wizard= getRefactoringWizard();
		Change change= wizard.getChange();
		if (change == null) {
			change= wizard.internalCreateChange(InternalAPI.INSTANCE, new CreateChangeOperation(getRefactoring()), false);
			wizard.internalSetChange(InternalAPI.INSTANCE, change);
		}
		if (change == null)
			return this;
			
		return super.getNextPage();
	}
	
	/* (non-JavaDoc)
	 * Method defined in RefactoringWizardPage
	 */
	protected boolean performFinish() {
		RefactoringWizard wizard= getRefactoringWizard();
		Change change= wizard.getChange();
		PerformChangeOperation operation= null;
		if (change != null) {
			operation= new UIPerformChangeOperation(getShell().getDisplay(), change, getContainer());
		} else {
			CreateChangeOperation ccop= new CreateChangeOperation(getRefactoring());
			operation= new UIPerformChangeOperation(getShell().getDisplay(), ccop, getContainer());
		}
		FinishResult result= wizard.internalPerformFinish(InternalAPI.INSTANCE, operation);
		if (result.isException())
			return true;
		if (result.isInterrupted())
			return false;
		RefactoringStatus fValidationStatus= operation.getValidationStatus();
		if (fValidationStatus != null && fValidationStatus.hasFatalError()) {
			MessageDialog.openError(wizard.getShell(), wizard.getWindowTitle(), 
				RefactoringUIMessages.getFormattedString(
					"RefactoringUI.cannot_execute", //$NON-NLS-1$
					fValidationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
			return true;
		}
		return true;
	} 
	
	//---- Helpers ----------------------------------------------------------------------------------------
	
	private boolean isRefactoringPossible() {
		return fStatus.getSeverity() < RefactoringStatus.FATAL;
	}	
}
