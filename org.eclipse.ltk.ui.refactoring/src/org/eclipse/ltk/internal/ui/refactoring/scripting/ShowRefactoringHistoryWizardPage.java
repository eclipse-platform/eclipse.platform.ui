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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEditHelper;
import org.eclipse.ltk.internal.ui.refactoring.history.ShowRefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEditHelper.IRefactoringHistoryProvider;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * First page of the show refactoring history wizard.
 * 
 * @since 3.2
 */
public final class ShowRefactoringHistoryWizardPage extends WizardPage {

	/** The refactoring descriptor delete query */
	private final class RefactoringDescriptorDeleteQuery implements IRefactoringDescriptorDeleteQuery {

		/** The number of descriptors to delete */
		private final int fCount;

		/** The return code */
		private int fReturnCode= -1;

		/** The shell to use */
		private final Shell fShell;

		/** Has the user already been warned once? */
		private boolean fWarned= false;

		/**
		 * Creates a new refactoring descriptor delete query.
		 * 
		 * @param shell
		 *            the shell to use
		 * @param count
		 *            the number of descriptors to delete
		 */
		public RefactoringDescriptorDeleteQuery(final Shell shell, final int count) {
			Assert.isNotNull(shell);
			Assert.isTrue(count >= 0);
			fShell= shell;
			fCount= count;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasDeletions() {
			return fReturnCode == IDialogConstants.YES_ID;
		}

		/**
		 * {@inheritDoc}
		 */
		public RefactoringStatus proceed(final RefactoringDescriptorProxy proxy) {
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			if (!fWarned && !store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE)) {
				fShell.getDisplay().syncExec(new Runnable() {

					public final void run() {
						if (!fShell.isDisposed()) {
							final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(fShell, RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_caption, Messages.format(ScriptingMessages.ShowRefactoringHistoryWizard_confirm_deletion, new Integer(fCount).toString()), RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE), null, null);
							store.setValue(PREFERENCE_DO_NOT_WARN_DELETE, dialog.getToggleState());
							fReturnCode= dialog.getReturnCode();
						}
					}
				});
			}
			fWarned= true;
			if (fReturnCode == IDialogConstants.YES_ID)
				return new RefactoringStatus();
			return RefactoringStatus.createErrorStatus(IDialogConstants.NO_LABEL);
		}
	}

	/** The show refactoring history wizard page name */
	private static final String PAGE_NAME= "ShowRefactoringHistoryWizardPage"; //$NON-NLS-1$

	/** Preference key for the warn delete preference */
	private static final String PREFERENCE_DO_NOT_WARN_DELETE= RefactoringUIPlugin.getPluginId() + ".do.not.warn.delete.descriptor"; //$NON-NLS-1$;

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

			protected void createEditButton(final Composite control) {
				// No edit button so far
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

		final GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.heightHint= new PixelConverter(parent).convertHeightInCharsToPixels(24);
		fHistoryControl.setLayoutData(data);
		fHistoryControl.getDeleteButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] selection= fHistoryControl.getCheckedDescriptors();
				if (selection.length > 0) {
					final Shell shell= getShell();
					final IRunnableContext context= new ProgressMonitorDialog(shell);
					RefactoringHistoryEditHelper.promptRefactoringDelete(shell, context, fHistoryControl, new RefactoringDescriptorDeleteQuery(shell, selection.length), new IRefactoringHistoryProvider() {

						public RefactoringHistory getRefactoringHistory(final IProgressMonitor monitor) {
							return RefactoringHistoryService.getInstance().getWorkspaceHistory(monitor);
						}
					}, selection);
				}
			}
		});
		final Button button= fHistoryControl.getEditButton();
		if (button != null) {
			button.addSelectionListener(new SelectionAdapter() {

				public final void widgetSelected(final SelectionEvent event) {
					final RefactoringDescriptorProxy[] selection= fHistoryControl.getSelectedDescriptors();
					if (selection.length > 0)
						RefactoringHistoryEditHelper.promptRefactoringDetails(getContainer(), fHistoryControl, selection[0]);
				}
			});
		}
		IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (context == null)
			context= PlatformUI.getWorkbench().getProgressService();
		handleInputEvent(context);
		setPageComplete(false);
		setControl(composite);
		Dialog.applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_SHOW_HISTORY_PAGE);
	}

	/**
	 * Handles the input event.
	 * 
	 * @param context
	 *            the runnable context to use
	 */
	private void handleInputEvent(final IRunnableContext context) {
		Assert.isNotNull(context);
		try {
			context.run(false, false, new IRunnableWithProgress() {

				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IRefactoringHistoryService service= RefactoringCore.getHistoryService();
					try {
						service.connect();
						fHistoryControl.setInput(service.getWorkspaceHistory(monitor));
					} finally {
						service.disconnect();
					}
				}
			});
		} catch (InvocationTargetException exception) {
			RefactoringUIPlugin.log(exception);
		} catch (InterruptedException exception) {
			// Do nothing
		}
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