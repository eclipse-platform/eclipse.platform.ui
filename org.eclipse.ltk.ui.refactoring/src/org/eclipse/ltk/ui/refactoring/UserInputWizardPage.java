/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.FinishResult;
import org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.InternalAPI;
import org.eclipse.ltk.internal.ui.refactoring.UIPerformChangeOperation;

/**
 * An abstract wizard page that is to be used to implement user input pages presented
 * inside a {@link org.eclipse.ltk.ui.refactoring.RefactoringWizard refactoring wizard}.
 * User input pages are shown at the beginning of a wizard. As soon as the last input
 * page is left the refactoring's condition checking is performed. Depending on the
 * outcome an error page or the preview page is shown.
 * <p>
 * Clients may extend this class.
 * </p>
 * @since 3.0
 */
public abstract class UserInputWizardPage extends RefactoringWizardPage {

	private boolean fIsLastUserInputPage;

	/**
	 * Creates a new user input page.
	 * @param name the page's name.
	 */
	public UserInputWizardPage(String name) {
		super(name);
	}

	/**
	 * Returns <code>true</code> if this is the last user input page in the stack
	 * of input pages; <code>false</code> otherwise. The last user input page is not
	 * necessarily the page after which the refactoring's precondition has to be
	 * triggered. For wizards implementing a dynamic work flow, this may happen for
	 * other pages as well.
	 *
	 * @return whether this is the last user input page or not.
	 */
	public boolean isLastUserInputPage() {
		return fIsLastUserInputPage;
	}

	/**
	 * Triggers the refactoring's condition checking and returns either the
	 * error wizard page or a preview page, depending on the outcome of the
	 * precondition checking.
	 *
	 * @return either the error or the preview page, depending on the refactoring's
	 *  precondition checking
	 */
	protected final IWizardPage computeSuccessorPage() {
		return getRefactoringWizard().computeUserInputSuccessorPage(this, getContainer());
	}

	/**
	 * Sets the page's complete status depending on the given <tt>
	 * ReactoringStatus</tt>.
	 *
	 * @param status the <tt>RefactoringStatus</tt>
	 */
	public void setPageComplete(RefactoringStatus status) {
		getRefactoringWizard().setConditionCheckingStatus(status);

		int severity= status.getSeverity();
		if (severity == RefactoringStatus.FATAL){
			setPageComplete(false);
			setErrorMessage(status.getMessageMatchingSeverity(severity));
		} else {
			setPageComplete(true);
			setErrorMessage(null);
			if (severity == RefactoringStatus.OK)
				setMessage(null, NONE);
			else
				setMessage(status.getMessageMatchingSeverity(severity), getCorrespondingIStatusSeverity(severity));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		if (visible)
			getRefactoringWizard().internalSetChange(InternalAPI.INSTANCE, null);
		super.setVisible(visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage() {
		if (fIsLastUserInputPage)
			return computeSuccessorPage();
		else
			return super.getNextPage();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		if (fIsLastUserInputPage) {
			// we can't call getNextPage to determine if flipping is allowed since computing
			// the next page is quite expensive (checking preconditions and creating a
			// change). So we say yes if the page is complete.
			return isPageComplete();
		} else {
			return super.canFlipToNextPage();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean performFinish() {
		RefactoringWizard wizard= getRefactoringWizard();
		int threshold= RefactoringCore.getConditionCheckingFailedSeverity();
		RefactoringStatus activationStatus= wizard.getInitialConditionCheckingStatus();
		RefactoringStatus inputStatus= null;
		RefactoringStatus status= new RefactoringStatus();
		Refactoring refactoring= getRefactoring();

		if (activationStatus != null && activationStatus.getSeverity() >= threshold) {
			if (!activationStatus.hasFatalError())
				inputStatus= wizard.checkFinalConditions();
		} else {
			CreateChangeOperation create= new CreateChangeOperation(
				new CheckConditionsOperation(refactoring, CheckConditionsOperation.FINAL_CONDITIONS),
				threshold);
			PerformChangeOperation perform= new UIPerformChangeOperation(getShell().getDisplay(), create, getContainer());

			FinishResult result= wizard.internalPerformFinish(InternalAPI.INSTANCE, perform);
			wizard.internalSetChange(InternalAPI.INSTANCE, create.getChange());
			if (result.isException())
				return true;
			if (result.isInterrupted())
				return false;
			inputStatus= new RefactoringStatus();
			inputStatus.merge(create.getConditionCheckingStatus());
			RefactoringStatus validationStatus= perform.getValidationStatus();
			// only merge this in if we have a fatal error. In all other cases
			// the change got executed
			if (validationStatus != null && validationStatus.hasFatalError())
				inputStatus.merge(perform.getValidationStatus());
		}

		status.merge(activationStatus);
		status.merge(inputStatus);

		if (status.getSeverity() >= threshold) {
			wizard.setConditionCheckingStatus(status);
			IWizardPage nextPage= wizard.getPage(IErrorWizardPage.PAGE_NAME);
			wizard.getContainer().showPage(nextPage);
			return false;
		}

		return true;
	}

	/* package */ void markAsLastUserInputPage() {
		fIsLastUserInputPage= true;
	}

	private static int getCorrespondingIStatusSeverity(int severity) {
		if (severity == RefactoringStatus.FATAL)
			return IStatus.ERROR;
		if (severity == RefactoringStatus.ERROR)
			return IStatus.WARNING;
		if (severity == RefactoringStatus.WARNING)
			return IStatus.WARNING;
		if (severity == RefactoringStatus.INFO)
			return IStatus.INFO;
		return IStatus.OK;
	}
}
