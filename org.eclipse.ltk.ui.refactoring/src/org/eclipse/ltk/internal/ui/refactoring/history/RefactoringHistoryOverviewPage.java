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
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.ltk.ui.refactoring.history.IRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Wizard page to give an overview a refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryOverviewPage extends WizardPage {

	/** The page name */
	private final static String PAGE_NAME= "historyOverviewPage"; //$NON-NLS-1$

	/** The refactoring history control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The refactoring history control */
	private IRefactoringHistoryControl fHistoryControl= null;

	/** The refactoring history */
	private final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history overview page.
	 * 
	 * @param history
	 *            the refactoring history to overview
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public RefactoringHistoryOverviewPage(final RefactoringHistory history, final RefactoringHistoryControlConfiguration configuration) {
		super(PAGE_NAME);
		Assert.isNotNull(history);
		Assert.isNotNull(configuration);
		fRefactoringHistory= history;
		fControlConfiguration= configuration;
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(RefactoringUIMessages.RefactoringHistoryOverviewPage_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		fHistoryControl= (IRefactoringHistoryControl) RefactoringUI.createRefactoringHistoryControl(composite, fControlConfiguration);
		fHistoryControl.createControl();
		fHistoryControl.setRefactoringHistory(fRefactoringHistory);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_HISTORY_WIZARD_PAGE);
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
}