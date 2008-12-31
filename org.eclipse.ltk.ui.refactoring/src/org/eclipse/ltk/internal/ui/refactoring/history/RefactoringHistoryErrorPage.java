/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusEntryFilter;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Error page for refactoring history wizards.
 *
 * @since 3.2
 */
public final class RefactoringHistoryErrorPage extends ErrorWizardPage {

	/** The status entry filter */
	private RefactoringStatusEntryFilter fFilter= new RefactoringStatusEntryFilter();

	/** Is the next wizard page disabled? */
	private boolean fNextPageDisabled= false;

	/** The current refactoring, or <code>null</code> */
	private Refactoring fRefactoring;

	/**
	 * Creates a new refactoring history error page.
	 */
	public RefactoringHistoryErrorPage() {
		super(true);
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(Messages.format(RefactoringUIMessages.RefactoringHistoryErrorPage_description, new String[] { getLabelAsText(IDialogConstants.NEXT_LABEL), getLabelAsText(IDialogConstants.FINISH_LABEL) }));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		return !fNextPageDisabled;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		super.createControl(parent);
		fViewer.setFilter(fFilter);
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
	 * Returns the refactoring history wizard.
	 *
	 * @return the refactoring history wizard
	 */
	protected RefactoringHistoryWizard getRefactoringHistoryWizard() {
		final IWizard result= getWizard();
		if (result instanceof RefactoringHistoryWizard)
			return (RefactoringHistoryWizard) result;
		return null;
	}

	/**
	 * Is the next wizard page disabled?
	 *
	 * @return <code>true</code> if disabled, <code>false</code> otherwise
	 */
	public boolean isNextPageDisabled() {
		return fNextPageDisabled;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean performFinish() {
		return true;
	}

	/**
	 * Sets the status entry filter.
	 *
	 * @param filter
	 *            the status entry filter to set
	 */
	public void setFilter(final RefactoringStatusEntryFilter filter) {
		Assert.isNotNull(filter);
		fFilter= filter;
	}

	/**
	 * Determines whether the next wizard page is disabled.
	 *
	 * @param disable
	 *            <code>true</code> to disable, <code>false</code> otherwise
	 */
	public void setNextPageDisabled(final boolean disable) {
		fNextPageDisabled= disable;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPageComplete(final boolean complete) {
		super.setPageComplete(true);
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
				setDescription(Messages.format(RefactoringUIMessages.RefactoringHistoryErrorPage_info_error, new String[] { getLabelAsText(IDialogConstants.NEXT_LABEL), getLabelAsText(IDialogConstants.FINISH_LABEL) }));
		}
		if (fViewer != null)
			fViewer.setStatus(status);
	}

	/**
	 * Sets the title of the page according to the refactoring.
	 *
	 * @param descriptor
	 *            the refactoring descriptor, or <code>null</code>
	 * @param current
	 *            the non-zero based index of the current refactoring
	 * @param total
	 *            the total number of refactorings
	 */
	public void setTitle(final RefactoringDescriptorProxy descriptor, final int current, final int total) {
		final String message;
		if (descriptor != null)
			message= descriptor.getDescription();
		else
			message= RefactoringUIMessages.RefactoringHistoryOverviewPage_title;
		if (total > 1)
			setTitle(Messages.format(RefactoringUIMessages.RefactoringHistoryPreviewPage_refactoring_pattern, new String[] { message, String.valueOf(current + 1), String.valueOf(total) }));
		else
			setTitle(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(final boolean visible) {
		if (visible) {
			if (fViewer != null && fViewer.getStatus() != fStatus)
				fViewer.setStatus(fStatus);
		} else
			setPageComplete(!fNextPageDisabled);
		getControl().setVisible(visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setWizard(final IWizard newWizard) {
		Assert.isTrue(newWizard instanceof RefactoringHistoryWizard);
		super.setWizard(newWizard);
	}
}
