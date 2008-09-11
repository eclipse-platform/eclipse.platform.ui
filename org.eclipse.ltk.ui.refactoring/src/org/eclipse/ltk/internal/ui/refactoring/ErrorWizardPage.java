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

package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
public class ErrorWizardPage extends RefactoringWizardPage implements IErrorWizardPage {

	protected RefactoringStatus fStatus;
	protected RefactoringStatusViewer fViewer;

	/**
	 * Creates a new error wizard page.
	 */
	public ErrorWizardPage() {
		super(PAGE_NAME);
	}

	/**
	 * Creates a new error wizard page.
	 *
	 * @param wizard
	 *            <code>true</code> if the page belongs to a conventional
	 *            wizard, <code>false</code> otherwise
	 */
	public ErrorWizardPage(boolean wizard) {
		super(PAGE_NAME, wizard);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setStatus(RefactoringStatus status) {
		fStatus= status;
		if (fStatus != null) {
			final int severity= fStatus.getSeverity();
			setPageComplete(severity < RefactoringStatus.FATAL);
			if (severity >= RefactoringStatus.FATAL) {
				setDescription(RefactoringUIMessages.ErrorWizardPage_cannot_proceed);
			} else if (severity >= RefactoringStatus.INFO) {
				setDescription(Messages.format(RefactoringUIMessages.ErrorWizardPage_confirm, new String[] {getLabelAsText(IDialogConstants.NEXT_LABEL), getLabelAsText(IDialogConstants.FINISH_LABEL)}));
			} else {
				setDescription(""); //$NON-NLS-1$
			}
		} else {
			setPageComplete(true);
			setDescription(""); //$NON-NLS-1$
		}
	}

	protected String getLabelAsText(String label) {
		Assert.isNotNull(label);
		return LegacyActionTools.removeMnemonics(label);
	}

	/**
	 * {@inheritDoc}
	 */
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
		return fStatus != null && fStatus.getSeverity() < RefactoringStatus.FATAL &&
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
				Messages.format(
					RefactoringUIMessages.RefactoringUI_cannot_execute,
					fValidationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
			return true;
		}
		return true;
	}
}
