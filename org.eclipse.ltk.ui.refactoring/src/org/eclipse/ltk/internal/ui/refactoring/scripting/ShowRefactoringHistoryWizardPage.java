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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;

import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.history.EditRefactoringDetailsDialog;
import org.eclipse.ltk.internal.ui.refactoring.history.ShowRefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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

		/** Has the user already been warned once? */
		private boolean fWarned= false;

		/**
		 * Creates a new refactoring descriptor delete query.
		 * 
		 * @param count
		 *            the number of descriptors to delete
		 */
		public RefactoringDescriptorDeleteQuery(final int count) {
			Assert.isTrue(count >= 0);
			fCount= count;
		}

		/**
		 * Returns whether any deletions have been performed successfully.
		 * 
		 * @return <code>true</code> if any deletions have been performed,
		 *         <code>false</code> otherwise
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
				final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_caption, Messages.format(ScriptingMessages.ShowRefactoringHistoryWizard_confirm_deletion, new Integer(fCount).toString()), RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE), null, null);
				store.setValue(PREFERENCE_DO_NOT_WARN_DELETE, dialog.getToggleState());
				fReturnCode= dialog.getReturnCode();
			}
			fWarned= true;
			if (fReturnCode == IDialogConstants.YES_ID)
				return new RefactoringStatus();
			return RefactoringStatus.createErrorStatus(IDialogConstants.NO_LABEL);
		}
	}

	/** The empty descriptors constant */
	private static final RefactoringDescriptorProxy[] EMPTY_DESCRIPTORS= new RefactoringDescriptorProxy[0];

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
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		final RefactoringHistoryControlConfiguration configuration= new RefactoringHistoryControlConfiguration(null, true, false) {

			public final String getWorkspaceCaption() {
				return ScriptingMessages.ShowRefactoringHistoryWizard_workspace_caption;
			}
		};
		fHistoryControl= new ShowRefactoringHistoryControl(composite, configuration) {

			protected void createDeleteAllButton(final Composite control) {
				// No delete all button
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
					final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
					try {
						service.connect();
						final RefactoringDescriptorDeleteQuery query= new RefactoringDescriptorDeleteQuery(selection.length);
						try {
							service.deleteRefactoringDescriptors(selection, query, null);
						} catch (CoreException exception) {
							final Throwable throwable= exception.getStatus().getException();
							if (throwable instanceof IOException)
								MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
							else
								RefactoringUIPlugin.log(exception);
						}
						if (query.hasDeletions()) {
							try {
								IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								if (context == null)
									context= PlatformUI.getWorkbench().getProgressService();
								context.run(false, false, new IRunnableWithProgress() {

									public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
										fHistoryControl.setInput(service.getWorkspaceHistory(monitor));
									}
								});
							} catch (InvocationTargetException exception) {
								RefactoringUIPlugin.log(exception);
							} catch (InterruptedException exception) {
								// Do nothing
							}
							fHistoryControl.setCheckedDescriptors(EMPTY_DESCRIPTORS);
						}
					} finally {
						service.disconnect();
					}
				}
			}
		});
		fHistoryControl.getEditButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				String details= ""; //$NON-NLS-1$
				final RefactoringDescriptorProxy[] selection= fHistoryControl.getSelectedDescriptors();
				if (selection.length > 0) {
					RefactoringHistoryService service= RefactoringHistoryService.getInstance();
					try {
						service.connect();
						final RefactoringDescriptor descriptor= selection[0].requestDescriptor(null);
						if (descriptor != null) {
							final String current= descriptor.getComment();
							if (current != null)
								details= current;
						}
						final EditRefactoringDetailsDialog dialog= new EditRefactoringDetailsDialog(getShell(), RefactoringUIMessages.RefactoringPropertyPage_edit_caption, Messages.format(RefactoringUIMessages.RefactoringPropertyPage_edit_message, selection[0].getDescription()), details);
						if (dialog.open() == 0) {
							service.setRefactoringComment(selection[0], dialog.getDetails(), null);
							fHistoryControl.setSelectedDescriptors(EMPTY_DESCRIPTORS);
							fHistoryControl.setSelectedDescriptors(new RefactoringDescriptorProxy[] { selection[0]});
						}
					} catch (CoreException exception) {
						RefactoringUIPlugin.log(exception);
					} finally {
						service.disconnect();
					}
				}
			}
		});
		try {
			IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (context == null)
				context= PlatformUI.getWorkbench().getProgressService();
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