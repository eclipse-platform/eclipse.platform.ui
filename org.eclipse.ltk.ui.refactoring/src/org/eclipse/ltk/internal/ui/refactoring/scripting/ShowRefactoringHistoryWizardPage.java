/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEditHelper;
import org.eclipse.ltk.internal.ui.refactoring.history.ShowRefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEditHelper.IRefactoringHistoryProvider;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * First page of the show refactoring history wizard.
 * 
 * @since 3.2
 */
public final class ShowRefactoringHistoryWizardPage extends WizardPage {

	/** The show refactoring history wizard page name */
	private static final String PAGE_NAME= "ShowRefactoringHistoryWizardPage"; //$NON-NLS-1$

	/** The sort dialog setting */
	private static final String SETTING_SORT= "org.eclipse.ltk.ui.refactoring.sortRefactorings"; //$NON-NLS-1$

	/** The refactoring history control */
	private ShowRefactoringHistoryControl fHistoryControl= null;

	/** The show refactoring history wizard */
	private final ShowRefactoringHistoryWizard fWizard;

	/**
	 * Creates a new show refactoring history wizard page.
	 * 
	 * @param wizard
	 *            the associated wizard
	 */
	public ShowRefactoringHistoryWizardPage(final ShowRefactoringHistoryWizard wizard) {
		super(PAGE_NAME);
		Assert.isNotNull(wizard);
		fWizard= wizard;
		setTitle(ScriptingMessages.ShowRefactoringHistoryWizard_caption);
		setDescription(ScriptingMessages.ShowRefactoringHistoryWizard_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NULL);
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginLeft= 5;
		layout.marginRight= 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		final RefactoringHistoryControlConfiguration configuration= new RefactoringHistoryControlConfiguration(null, true, false) {

			public final String getWorkspaceCaption() {
				return ScriptingMessages.ShowRefactoringHistoryWizard_workspace_caption;
			}
		};
		fHistoryControl= new ShowRefactoringHistoryControl(composite, configuration) {

			protected void createBottomButtonBar(final Composite control) {
				Assert.isNotNull(control);
				final Composite container= new Composite(control, SWT.NONE);
				final GridLayout grid= new GridLayout(1, false);
				grid.marginWidth= 0;
				grid.marginHeight= 0;
				container.setLayout(grid);

				final GridData data= new GridData();
				data.grabExcessHorizontalSpace= true;
				data.grabExcessVerticalSpace= false;
				data.horizontalAlignment= SWT.FILL;
				data.verticalAlignment= SWT.TOP;
				container.setLayoutData(data);

				createDeleteButton(container, GridData.END);
			}

			protected void createDeleteAllButton(final Composite control) {
				// No delete all button
			}

			protected void createRightButtonBar(final Composite control) {
				final Composite container= new Composite(control, SWT.NONE);
				container.setLayout(new GridLayout(1, false));
			}

			protected int getContainerColumns() {
				return 1;
			}
		};
		fHistoryControl.createControl();
		boolean sortProjects= true;
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null)
			sortProjects= settings.getBoolean(SETTING_SORT);
		if (sortProjects)
			fHistoryControl.sortByProjects();
		else
			fHistoryControl.sortByDate();

		fHistoryControl.setInput(fWizard.getRefactoringHistory());

		final GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.heightHint= new PixelConverter(parent).convertHeightInCharsToPixels(24);
		fHistoryControl.setLayoutData(data);
		fHistoryControl.getDeleteButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] selection= fHistoryControl.getCheckedDescriptors();
				if (selection.length > 0) {
					final Shell shell= getShell();
					final IRunnableContext context= new ProgressMonitorDialog(shell);
					RefactoringHistoryEditHelper.promptRefactoringDelete(shell, context, fHistoryControl, new RefactoringDescriptorDeleteQuery(shell, null, selection.length), new IRefactoringHistoryProvider() {

						public RefactoringHistory getRefactoringHistory(final IProgressMonitor monitor) {
							return RefactoringHistoryService.getInstance().getWorkspaceHistory(monitor);
						}
					}, selection);
				}
			}
		});
		setPageComplete(false);
		setControl(composite);
		Dialog.applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_SHOW_HISTORY_PAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPageComplete() {
		return true;
	}

	/**
	 * Gets called if the wizard is finished.
	 */
	public void performFinish() {
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null)
			settings.put(SETTING_SORT, fHistoryControl.isSortByProjects());
	}
}