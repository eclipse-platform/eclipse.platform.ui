/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryErrorPage;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Preview page for refactoring history wizards.
 *
 * @since 3.2
 */
public final class RefactoringHistoryPreviewPage extends PreviewWizardPage {

	/** The preview change filter */
	private RefactoringPreviewChangeFilter fFilter= new RefactoringPreviewChangeFilter();

	/** Is the next wizard page disabled? */
	private boolean fNextPageDisabled= false;

	/** The current refactoring, or <code>null</code> */
	private Refactoring fRefactoring;

	/** The refactoring status */
	private RefactoringStatus fStatus= new RefactoringStatus();

	/**
	 * Creates a new refactoring history preview page.
	 */
	public RefactoringHistoryPreviewPage() {
		super(true);
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_description);
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
	public IWizardPage getNextPage() {
		if (fChange != null && fRefactoring != null && !fStatus.hasFatalError()) {
			final RefactoringHistoryWizard result= getRefactoringHistoryWizard();
			if (result != null) {
				final RefactoringStatus status= result.performPreviewChange(fChange, fRefactoring);
				if (!status.isOK()) {
					final RefactoringStatusEntry entry= status.getEntryWithHighestSeverity();
					if (entry != null) {
						if (entry.getSeverity() == RefactoringStatus.INFO && entry.getCode() == RefactoringHistoryWizard.STATUS_CODE_INTERRUPTED)
							return this;
						final IErrorWizardPage page= result.getErrorPage();
						if (page instanceof RefactoringHistoryErrorPage) {
							final RefactoringHistoryErrorPage extended= (RefactoringHistoryErrorPage) page;
							extended.setStatus(status);
							extended.setNextPageDisabled(fNextPageDisabled);
							extended.setTitle(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error_title);
							extended.setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error);
							return extended;
						}
					}
				}
			}
		}
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
	 * Sets the preview change filter.
	 *
	 * @param filter
	 *            the preview change filter to set
	 */
	public void setFilter(final RefactoringPreviewChangeFilter filter) {
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
		super.setPageComplete(!fNextPageDisabled);
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
	 * Sets the status of the change generation.
	 *
	 * @param status
	 *            the status
	 */
	public void setStatus(final RefactoringStatus status) {
		Assert.isNotNull(status);
		fStatus= status;
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
			setTitle(Messages.format(RefactoringUIMessages.RefactoringHistoryPreviewPage_refactoring_pattern, new String[] { message, String.valueOf(current + 1), String.valueOf(total)}));
		else
			setTitle(message);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setTreeViewerInput() {
		if (fTreeViewer == null)
			return;
		PreviewNode input= null;
		if (fTreeViewerInputChange != null) {
			input= AbstractChangeNode.createNode(null, fFilter, fTreeViewerInputChange);
		}
		if (input instanceof CompositeChangeNode) {
			final CompositeChangeNode node= (CompositeChangeNode) input;
			final PreviewNode[] nodes= node.getChildren();
			if (nodes == null || nodes.length == 0) {
				fTreeViewerPane.setText(RefactoringUIMessages.RefactoringHistoryPreviewPage_no_changes);
				fNextAction.setEnabled(false);
				fPreviousAction.setEnabled(false);
				fFilterDropDownAction.setEnabled(false);
				fTreeViewer.setInput(null);
				return;
			}
		}
		fTreeViewerPane.setText(RefactoringUIMessages.PreviewWizardPage_changes);
		fNextAction.setEnabled(true);
		fPreviousAction.setEnabled(true);
		fFilterDropDownAction.setEnabled(true);
		fTreeViewer.setInput(input);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (fTreeViewer.getInput() == null)
			fFilterDropDownAction.setEnabled(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setWizard(final IWizard newWizard) {
		Assert.isTrue(newWizard instanceof RefactoringHistoryWizard);
		super.setWizard(newWizard);
	}
}
