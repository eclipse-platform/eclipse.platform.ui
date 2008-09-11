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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Wizard page to give an overview a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryOverviewPage extends WizardPage {

	/** The page name */
	private final static String PAGE_NAME= "historyOverviewPage"; //$NON-NLS-1$

	/** The sort dialog setting */
	private static final String SETTING_SORT= "org.eclipse.ltk.ui.refactoring.sortRefactorings"; //$NON-NLS-1$

	/** The refactoring history control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The refactoring history control */
	private SortableRefactoringHistoryControl fHistoryControl= null;

	/** The refactoring history */
	private final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history overview page.
	 *
	 * @param history
	 *            the refactoring history to overview
	 * @param title
	 *            the title of the wizard page
	 * @param description
	 *            the description of the wizard page
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public RefactoringHistoryOverviewPage(final RefactoringHistory history, final String title, final String description, final RefactoringHistoryControlConfiguration configuration) {
		super(PAGE_NAME);
		Assert.isNotNull(history);
		Assert.isNotNull(configuration);
		Assert.isNotNull(title);
		Assert.isNotNull(description);
		fRefactoringHistory= history;
		fControlConfiguration= configuration;
		setTitle(title);
		setDescription(description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		return !fRefactoringHistory.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fHistoryControl= new SortableRefactoringHistoryControl(composite, fControlConfiguration) {

			protected void createBottomButtonBar(final Composite control) {
				// No button bar
			}
		};
		fHistoryControl.createControl();
		boolean sortProjects= false;
		final IDialogSettings settings= getWizard().getDialogSettings();
		if (settings != null)
			sortProjects= settings.getBoolean(SETTING_SORT);
		if (sortProjects)
			fHistoryControl.sortByProjects();
		else
			fHistoryControl.sortByDate();
		fHistoryControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
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

	/**
	 * Gets called if the wizard is finished.
	 */
	public void performFinish() {
		final IDialogSettings settings= getWizard().getDialogSettings();
		if (settings != null)
			settings.put(SETTING_SORT, fHistoryControl.isSortByProjects());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPageComplete(final boolean complete) {
		super.setPageComplete(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		fHistoryControl.setInput(fRefactoringHistory);
		super.setVisible(visible);
	}
}
