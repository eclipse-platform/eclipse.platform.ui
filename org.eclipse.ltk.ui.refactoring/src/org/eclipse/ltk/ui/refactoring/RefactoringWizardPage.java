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

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * An abstract base implementation of a refactoring wizard page. The class
 * provides access to the refactoring wizard and to the refactoring itself.
 * Refactoring wizard pages can only be added to a
 * {@link org.eclipse.ltk.ui.refactoring.RefactoringWizard RefactoringWizard}.
 * Adding them to a normal {@linkplain org.eclipse.jface.wizard.Wizard wizard}
 * result in an exception.
 * <p> 
 * Note: this class is not intended to be subclassed by clients. Clients should
 * extend {@link org.eclipse.ltk.ui.refactoring.UserInputWizardPage}.
 * </p>
 * 
 * @see RefactoringWizard
 * @see org.eclipse.ltk.core.refactoring.Refactoring
 */
public abstract class RefactoringWizardPage extends WizardPage {

	public static final String REFACTORING_SETTINGS= "org.eclipse.ltk.ui.refactoring.settings"; //$NON-NLS-1$

	/**
	 * Creates a new refactoring wizard page.
	 * 
	 * @param name the page's name.
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	protected RefactoringWizardPage(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public void setWizard(IWizard newWizard) {
		Assert.isTrue(newWizard instanceof RefactoringWizard);
		super.setWizard(newWizard);
	}

	/**
	 * Returns the refactoring associated with this wizard page. Returns
	 * <code>null</code> if the page isn't been added to any refactoring
	 * wizard yet.
	 * 
	 * @return the refactoring associated with this refactoring wizard page
	 */
	protected Refactoring getRefactoring() {
		RefactoringWizard wizard= getRefactoringWizard();
		if (wizard == null)
			return null;
		return wizard.getRefactoring();
	}
	
	/**
	 * Returns the page's refactoring wizard.
	 * 
	 * @return the page's refactoring wizard
	 */
	protected RefactoringWizard getRefactoringWizard() {
		return (RefactoringWizard)getWizard();
	}
	
	/**
	 * Performs any actions appropriate in response to the user having pressed
	 * the Finish button, or refuse if finishing now is not permitted. This
	 * method is called by the refactoring wizard on the currently active
	 * refactoring wizard page.
	 * 
	 * @return <code>true</code> to indicate the finish request was accepted,
	 *         and <code>false</code> to indicate that the finish request was
	 *         refused
	 */
	protected boolean performFinish() {
		return true;
	}
	
	/**
	 * Returns the refactoring wizard's dialog settings.
	 * 
	 * @return the refactoring wizard's dialog settings.
	 */
	protected IDialogSettings getRefactoringSettings() {
		IDialogSettings settings= getDialogSettings();
		if (settings == null)
			return null;
		IDialogSettings result= settings.getSection(REFACTORING_SETTINGS);
		if (result == null) {
			result= new DialogSettings(REFACTORING_SETTINGS);
			settings.addSection(result); 
		}
		return result;
	}
}
