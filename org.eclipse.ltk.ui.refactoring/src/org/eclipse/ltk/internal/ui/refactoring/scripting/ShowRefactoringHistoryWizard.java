/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to show the global refactoring history
 * 
 * @since 3.2
 */
public final class ShowRefactoringHistoryWizard extends Wizard {

	/** The dialog settings key */
	private static String DIALOG_SETTINGS_KEY= "ShowRefactoringHistoryWizard"; //$NON-NLS-1$

	/** Has the wizard new dialog settings? */
	private boolean fNewSettings;

	/** The refactoring history */
	private RefactoringHistory fRefactoringHistory;

	/** The show refactoring history wizard page */
	private final ShowRefactoringHistoryWizardPage fWizardPage;

	/**
	 * Creates a new show refactoring history wizard.
	 */
	public ShowRefactoringHistoryWizard() {
		setNeedsProgressMonitor(false);
		setWindowTitle(ScriptingMessages.ShowRefactoringHistoryWizard_title);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_SHOW_HISTORY);
		final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
		final IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null)
			fNewSettings= true;
		else {
			fNewSettings= false;
			setDialogSettings(section);
		}
		fWizardPage= new ShowRefactoringHistoryWizardPage(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		super.addPages();
		addPage(fWizardPage);
	}

	/**
	 * Returns the refactoring history to create a script from.
	 * 
	 * @return the refactoring history.
	 */
	public RefactoringHistory getRefactoringHistory() {
		return fRefactoringHistory;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		if (fNewSettings) {
			final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
			section= settings.addNewSection(DIALOG_SETTINGS_KEY);
			setDialogSettings(section);
		}
		fWizardPage.performFinish();
		return true;
	}

	/**
	 * Sets the refactoring history to use.
	 * 
	 * @param history
	 *            the refactoring history to use
	 */
	public void setRefactoringHistory(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}

}