/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPreferences;

/**
 * An abstract wizard page that can be used to implement user input pages for 
 * refactoring wizards. Usually user input pages are pages shown at the beginning 
 * of a wizard. As soon as the "last" user input page is left a corresponding 
 * precondition check is executed.
 */
public abstract class UserInputWizardPage extends RefactoringWizardPage {

	private final boolean fIsLastUserPage;
	
	/**
	 * Creates a new user input page.
	 * @param name the page's name.
	 * @param isLastUserPage <code>true</code> if this page is the wizard's last
	 *  user input page. Otherwise <code>false</code>.
	 */
	public UserInputWizardPage(String name, boolean isLastUserPage) {
		super(name);
		fIsLastUserPage= isLastUserPage;
	}
	
	/**
	 * Sets the page's complete status depending on the given <tt>
	 * ReactoringStatus</tt>.
	 * 
	 * @param status the <tt>RefactoringStatus</tt>
	 */
	public void setPageComplete(RefactoringStatus status) {
		getRefactoringWizard().setStatus(status);

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
	
	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	public void setVisible(boolean visible) {
		if (visible)
			getRefactoringWizard().setChange(null);
		super.setVisible(visible);
	}
	
	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public IWizardPage getNextPage() {
		if (fIsLastUserPage) 
			return getRefactoringWizard().computeUserInputSuccessorPage(this);
		else
			return super.getNextPage();
	}
	
	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		if (fIsLastUserPage) {
			// we can't call getNextPage to determine if flipping is allowed since computing
			// the next page is quite expensive (checking preconditions and creating a
			// change). So we say yes if the page is complete.
			return isPageComplete();
		} else {
			return super.canFlipToNextPage();
		}
	}
	
	/* (non-JavaDoc)
	 * Method defined in RefactoringWizardPage
	 */
	protected boolean performFinish() {
		RefactoringWizard wizard= getRefactoringWizard();
		int threshold= RefactoringPreferences.getCheckPassedSeverity();
		RefactoringStatus activationStatus= wizard.getActivationStatus();
		RefactoringStatus inputStatus= null;
		RefactoringStatus status= new RefactoringStatus();
		Refactoring refactoring= getRefactoring();
		boolean result= false;
		
		if (activationStatus != null && activationStatus.getSeverity() > threshold) {
			inputStatus= wizard.checkInput();
		} else {
			CreateChangeOperation create= new CreateChangeOperation(
				new CheckConditionsOperation(refactoring, CheckConditionsOperation.FINAL_CONDITIONS),
				threshold);
			PerformChangeOperation perform= new PerformChangeOperation(create);
			
			result= wizard.performFinish(perform);
			wizard.setChange(create.getChange());
			if (!result)
				return false;
			inputStatus= new RefactoringStatus();
			inputStatus.merge(create.getConditionCheckingStatus());
			inputStatus.merge(perform.getValidationStatus());
		}
		
		status.merge(activationStatus);
		status.merge(inputStatus);
		
		if (status.getSeverity() > threshold) {
			wizard.setStatus(status);
			IWizardPage nextPage= wizard.getPage(ErrorWizardPage.PAGE_NAME);
			wizard.getContainer().showPage(nextPage);
			return false;
		}
		
		return result;	
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
