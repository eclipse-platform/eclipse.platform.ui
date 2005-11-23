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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Error page for refactoring history wizards.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryErrorPage extends ErrorWizardPage {

	/**
	 * Creates a new refactoring history error page.
	 */
	public RefactoringHistoryErrorPage() {
		super(true);
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(RefactoringUIMessages.RefactoringHistoryErrorPage_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		final RefactoringStatus status= getStatus();
		return status != null && status.getSeverity() < RefactoringStatus.FATAL;
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage() {
		return getWizard().getNextPage(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getPreviousPage() {
		return getWizard().getPreviousPage(this);
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean performFinish() {
		return true;
	}

	/**
	 * Sets the title of the page according to the refactoring.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor, or <code>null</code>
	 */
	public void setTitle(final RefactoringDescriptorProxy descriptor) {
		if (descriptor != null)
			setTitle(descriptor.getDescription());
		else
			setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
	}
}