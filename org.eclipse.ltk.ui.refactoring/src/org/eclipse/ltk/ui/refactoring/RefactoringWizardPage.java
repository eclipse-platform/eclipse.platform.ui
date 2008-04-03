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

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;

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
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class RefactoringWizardPage extends WizardPage {

	public static final String REFACTORING_SETTINGS= "org.eclipse.ltk.ui.refactoring.settings"; //$NON-NLS-1$
	
	/** Does the page belong to a conventional wizard? */
	private final boolean fConventionalWizard;
	
	/**
	 * Creates a new refactoring wizard page.
	 * <p>
	 * Note: this constructor is not intended to be used outside the refactoring
	 * framework.
	 * </p>
	 * 
	 * @param name
	 *            the page's name.
	 * @param wizard
	 *            <code>true</code> if the page belongs to a conventional wizard, <code>false</code> otherwise
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 * 
	 * @since 3.2
	 */
	protected RefactoringWizardPage(String name, boolean wizard) {
		super(name);
		fConventionalWizard= wizard;
	}
	
	/**
	 * Creates a new refactoring wizard page.
	 * 
	 * @param name the page's name.
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	protected RefactoringWizardPage(String name) {
		super(name);
		fConventionalWizard= false;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This method asserts that the wizard passed as a parameter is of 
	 * type <code>RefactoringWizard</code>.
	 */
	public void setWizard(IWizard newWizard) {
		Assert.isTrue(fConventionalWizard || newWizard instanceof RefactoringWizard);
		super.setWizard(newWizard);
	}

	/**
	 * Returns the refactoring associated with this wizard page. Returns
	 * <code>null</code> if the page isn't been added to any refactoring
	 * wizard yet.
	 * 
	 * @return the refactoring associated with this refactoring wizard page
	 *  or <code>null</code>
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
	 * @return the page's refactoring wizard or <code>null</code> if the
	 *         wizard hasn't been set yet
	 */
	protected RefactoringWizard getRefactoringWizard() {
		IWizard wizard= getWizard();
		if (wizard instanceof RefactoringWizard)
			return (RefactoringWizard) wizard;
		return null;
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
	 * @return the refactoring wizard's dialog settings or <code>null</code>
	 *  if no settings are associated with the refactoring wizard dialog
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
