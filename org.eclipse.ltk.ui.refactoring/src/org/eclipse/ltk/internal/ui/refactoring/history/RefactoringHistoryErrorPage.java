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

import org.eclipse.ltk.core.refactoring.Refactoring;
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

	/** Is the current refactoring the last one? */
	private boolean fLastRefactoring= false;

	/** The current refactoring, or <code>null</code> */
	private Refactoring fRefactoring;

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
		if (status != null && status.hasFatalError())
			return !fLastRefactoring;
		return true;
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
	 * Returns the current refactoring.
	 * 
	 * @return the current refactoring
	 */
	public Refactoring getRefactoring() {
		return fRefactoring;
	}

	/**
	 * Is the current refactoring the last one?
	 * 
	 * @return <code>true</code> if it is the last one, <code>false</code>
	 *         otherwise
	 */
	public boolean isLastRefactoring() {
		return fLastRefactoring;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean performFinish() {
		return true;
	}

	/**
	 * Determines whether the current refactoring is the last one.
	 * 
	 * @param last
	 *            <code>true</code> if it is the last one, <code>false</code>
	 *            otherwise
	 */
	public void setLastRefactoring(final boolean last) {
		fLastRefactoring= last;
	}

	/**
	 * Sets the current refactoring.
	 * 
	 * @param refactoring
	 *            the current refactoring, or <code>null</code>
	 */
	public void setRefactoring(final Refactoring refactoring) {
		fRefactoring= refactoring;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setStatus(final RefactoringStatus status) {
		super.setStatus(status);
		if (status != null) {
			final int severity= status.getSeverity();
			if (severity >= RefactoringStatus.FATAL)
				setDescription(RefactoringUIMessages.RefactoringHistoryErrorPage_fatal_error);
			else if (severity >= RefactoringStatus.INFO)
				setDescription(RefactoringUIMessages.RefactoringHistoryErrorPage_info_error);
		}
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