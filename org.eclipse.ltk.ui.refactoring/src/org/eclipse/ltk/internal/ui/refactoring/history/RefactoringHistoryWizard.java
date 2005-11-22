/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to execute the refactorings of a refactoring history sequentially.
 * 
 * @since 3.2
 */
public class RefactoringHistoryWizard extends Wizard {

	/** The default page title */
	private String fDefaultPageTitle;

	/** Are we currently in method <code>addPages</code>? */
	private boolean fInAddPages= false;

	/** The refactoring history to execute */
	private final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history wizard.
	 * 
	 * @param history
	 *            the refactoring history to execute
	 */
	public RefactoringHistoryWizard(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
		setNeedsProgressMonitor(true);
		setWindowTitle(RefactoringUIMessages.RefactoringWizard_title);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPage(final IWizardPage page) {
		Assert.isTrue(fInAddPages);
		super.addPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		Assert.isNotNull(fRefactoringHistory);
		try {
			fInAddPages= true;
			addPage(new ErrorWizardPage());
			addPage(new PreviewWizardPage());
			initializeDefaultPageTitles();
		} finally {
			fInAddPages= false;
		}
	}

	/**
	 * Returns the default page title used for pages that don't provide their
	 * own page title.
	 * 
	 * @return the default page title or <code>null</code> if non has been set
	 * 
	 * @see #setDefaultPageTitle(String)
	 */
	public final String getDefaultPageTitle() {
		return fDefaultPageTitle;
	}

	/**
	 * Returns the refactoring history.
	 * 
	 * @return the refactoring history
	 */
	public final RefactoringHistory getRefactoringHistory() {
		return fRefactoringHistory;
	}

	/**
	 * Initializes the default page titles.
	 */
	private void initializeDefaultPageTitles() {
		if (fDefaultPageTitle != null) {
			final IWizardPage[] pages= getPages();
			for (int index= 0; index < pages.length; index++) {
				if (pages[index].getTitle() == null)
					pages[index].setTitle(fDefaultPageTitle);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		return false;
	}

	/**
	 * Sets the default page title to the given value. This value is used as a
	 * page title for wizard pages which don't provide their own page title.
	 * Setting this value has only an effect as long as the user interface
	 * hasn't been created yet.
	 * 
	 * @param title
	 *            the default page title
	 * @see Wizard#setDefaultPageImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public final void setDefaultPageTitle(final String title) {
		Assert.isNotNull(title);
		fDefaultPageTitle= title;
	}
}
