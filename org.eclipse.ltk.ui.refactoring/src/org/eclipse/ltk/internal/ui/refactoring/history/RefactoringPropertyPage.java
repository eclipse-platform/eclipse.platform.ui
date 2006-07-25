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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEditHelper.IRefactoringHistoryProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Property page for a project's refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringPropertyPage extends PropertyPage {

	/** The dialog settings key */
	private static String DIALOG_SETTINGS_KEY= "RefactoringPropertyPage"; //$NON-NLS-1$

	/** The empty descriptors constant */
	static final RefactoringDescriptorProxy[] EMPTY_DESCRIPTORS= new RefactoringDescriptorProxy[0];

	/** Preference key for the warn delete all preference */
	private static final String PREFERENCE_DO_NOT_WARN_DELETE_ALL= RefactoringUIPlugin.getPluginId() + ".do.not.warn.delete.history"; //$NON-NLS-1$;

	/** The sort dialog setting */
	private static final String SETTING_SORT= "org.eclipse.ltk.ui.refactoring.sortRefactorings"; //$NON-NLS-1$

	/** The refactoring preference */
	private boolean fHasProjectHistory= false;

	/** The refactoring history control */
	private ShowRefactoringHistoryControl fHistoryControl;

	/** The preferences working copy manager, or <code>null</code> */
	private IWorkingCopyManager fManager= null;

	/** Has the property page new dialog settings? */
	private boolean fNewSettings;

	/** The dialog settings, or <code>null</code> */
	private IDialogSettings fSettings= null;

	/** The share history button, or <code>null</code> */
	private Button fShareHistoryButton= null;

	/**
	 * Creates a new refactoring property page.
	 */
	public RefactoringPropertyPage() {
		noDefaultAndApplyButton();
		final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
		final IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null)
			fNewSettings= true;
		else {
			fNewSettings= false;
			fSettings= section;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected Control createContents(final Composite parent) {
		initializeDialogUnits(parent);

		final IPreferencePageContainer container= getContainer();
		if (container instanceof IWorkbenchPreferenceContainer)
			fManager= ((IWorkbenchPreferenceContainer) container).getWorkingCopyManager();

		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		composite.setLayout(layout);

		fHistoryControl= new ShowRefactoringHistoryControl(composite, new RefactoringHistoryControlConfiguration(getCurrentProject(), true, false) {

			public String getProjectPattern() {
				return RefactoringUIMessages.RefactoringPropertyPage_project_pattern;
			}
		}) {

			protected void createEditButton(final Composite control) {
				// No edit button so far
			}
		};
		fHistoryControl.createControl();
		boolean sortProjects= true;
		final IDialogSettings settings= fSettings;
		if (settings != null)
			sortProjects= settings.getBoolean(SETTING_SORT);
		if (sortProjects)
			fHistoryControl.sortByProjects();
		else
			fHistoryControl.sortByDate();

		fHistoryControl.getDeleteAllButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final IProject project= getCurrentProject();
				if (project != null) {
					final IRunnableContext context= new ProgressMonitorDialog(getShell());
					final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
					MessageDialogWithToggle dialog= null;
					if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE_ALL) && !fHistoryControl.getInput().isEmpty()) {
						dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_all_caption, Messages.format(RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_all_pattern, project.getName()), RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
						store.setValue(PREFERENCE_DO_NOT_WARN_DELETE_ALL, dialog.getToggleState());
					}
					if (dialog == null || dialog.getReturnCode() == IDialogConstants.YES_ID)
						promptDeleteHistory(context, project);
				}
			}
		});
		fHistoryControl.getDeleteButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] selection= fHistoryControl.getCheckedDescriptors();
				if (selection.length > 0) {
					final IRunnableContext context= new ProgressMonitorDialog(getShell());
					final IProject project= getCurrentProject();
					if (project != null) {
						final Shell shell= getShell();
						RefactoringHistoryEditHelper.promptRefactoringDelete(shell, context, fHistoryControl, new RefactoringDescriptorDeleteQuery(shell, getCurrentProject(), selection.length), new IRefactoringHistoryProvider() {

							public RefactoringHistory getRefactoringHistory(final IProgressMonitor monitor) {
								return RefactoringHistoryService.getInstance().getProjectHistory(project, monitor);
							}
						}, selection);
					}
				}
			}
		});
		final Button button= fHistoryControl.getEditButton();
		if (button != null) {
			button.addSelectionListener(new SelectionAdapter() {

				public final void widgetSelected(final SelectionEvent event) {
					final RefactoringDescriptorProxy[] selection= fHistoryControl.getSelectedDescriptors();
					if (selection.length > 0) {
						final IRunnableContext context= new ProgressMonitorDialog(getShell());
						RefactoringHistoryEditHelper.promptRefactoringDetails(context, fHistoryControl, selection[0]);
					}
				}
			});
		}
		fShareHistoryButton= new Button(composite, SWT.CHECK);
		fShareHistoryButton.setText(RefactoringUIMessages.RefactoringPropertyPage_share_message);
		fShareHistoryButton.setData(RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY);

		final GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.verticalIndent= convertHeightInCharsToPixels(1) / 2;
		fShareHistoryButton.setLayoutData(data);
		fShareHistoryButton.setSelection(hasSharedRefactoringHistory());

		new Label(composite, SWT.NONE);

		final IProject project= getCurrentProject();
		if (project != null) {
			IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (context == null)
				context= PlatformUI.getWorkbench().getProgressService();
			handleInputEvent(context, project);
		}
		applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IRefactoringHelpContextIds.REFACTORING_PROPERTY_PAGE);

		return composite;
	}

	/**
	 * Returns the project currently associated with this property page.
	 * 
	 * @return the currently associated project, or <code>null</code>
	 */
	private IProject getCurrentProject() {
		return (IProject) getElement().getAdapter(IProject.class);
	}

	/**
	 * Returns the preferences for the specified context.
	 * 
	 * @param manager
	 *            the working copy manager
	 * @param context
	 *            the scope context
	 * @return the preferences
	 */
	private IEclipsePreferences getPreferences(final IWorkingCopyManager manager, final IScopeContext context) {
		final IEclipsePreferences preferences= context.getNode(RefactoringCore.ID_PLUGIN);
		if (manager != null)
			return manager.getWorkingCopy(preferences);
		return preferences;
	}

	/**
	 * Handles the input event.
	 * 
	 * @param context
	 *            the runnable context to use
	 * @param project
	 *            the project
	 */
	private void handleInputEvent(final IRunnableContext context, final IProject project) {
		Assert.isNotNull(context);
		Assert.isNotNull(project);
		try {
			context.run(false, false, new IRunnableWithProgress() {

				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IRefactoringHistoryService service= RefactoringCore.getHistoryService();
					try {
						service.connect();
						fHistoryControl.setInput(service.getProjectHistory(project, monitor));
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
	 * Returns whether a project has an shared refactoring history.
	 * 
	 * @return <code>true</code> if the project contains an shared project
	 *         history, <code>false</code> otherwise
	 */
	private boolean hasSharedRefactoringHistory() {
		final IProject project= getCurrentProject();
		if (project != null)
			return RefactoringHistoryService.hasSharedRefactoringHistory(project);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void performDefaults() {
		super.performDefaults();
		final IProject project= getCurrentProject();
		if (project != null)
			setPreference(fManager, new ProjectScope(project), RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performOk() {
		if (fNewSettings) {
			final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
			section= settings.addNewSection(DIALOG_SETTINGS_KEY);
			fSettings= section;
		}
		fSettings.put(SETTING_SORT, fHistoryControl.isSortByProjects());
		final IProject project= getCurrentProject();
		if (project != null)
			setPreference(fManager, new ProjectScope(project), RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, Boolean.valueOf(fShareHistoryButton.getSelection()).toString());
		if (fManager != null)
			try {
				fManager.applyChanges();
				final boolean history= RefactoringHistoryService.hasSharedRefactoringHistory(project);
				if (history != fHasProjectHistory && project != null) {
					final Job job= new Job(history ? RefactoringUIMessages.RefactoringPropertyPage_sharing_refactoring_history : RefactoringUIMessages.RefactoringPropertyPage_unsharing_refactoring_history) {

						public final IStatus run(final IProgressMonitor monitor) {
							try {
								RefactoringHistoryService.setSharedRefactoringHistory(project, history, monitor);
							} catch (CoreException exception) {
								RefactoringUIPlugin.log(exception);
								return exception.getStatus();
							}
							return Status.OK_STATUS;
						}
					};
					job.setRule(project);
					job.setPriority(Job.SHORT);
					job.schedule();
				}
			} catch (BackingStoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
		return super.performOk();
	}

	/**
	 * Prompts the user to delete the project refactoring history.
	 * 
	 * @param context
	 *            the runnable context to use
	 * @param project
	 *            the project to delete its refactoring history
	 */
	protected void promptDeleteHistory(final IRunnableContext context, final IProject project) {
		Assert.isNotNull(context);
		Assert.isNotNull(project);
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.connect();
			try {
				final Shell shell= getShell();
				context.run(false, true, new WorkbenchRunnableAdapter(new IWorkspaceRunnable() {

					public void run(final IProgressMonitor monitor) throws CoreException {
						try {
							monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, 100);
							try {
								service.deleteRefactoringHistory(project, new SubProgressMonitor(monitor, 50, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							} catch (CoreException exception) {
								final Throwable throwable= exception.getStatus().getException();
								if (throwable instanceof IOException) {
									shell.getDisplay().syncExec(new Runnable() {

										public void run() {
											MessageDialog.openError(shell, RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
										}
									});
								} else
									throw exception;
							}
							final RefactoringHistory history= service.getProjectHistory(project, new SubProgressMonitor(monitor, 50, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							shell.getDisplay().syncExec(new Runnable() {

								public void run() {
									fHistoryControl.setInput(history);
									fHistoryControl.setCheckedDescriptors(EMPTY_DESCRIPTORS);
								}
							});
						} finally {
							monitor.done();
						}
					}
				}, project));
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
			} catch (InterruptedException exception) {
				// Do nothing
			}
		} finally {
			service.disconnect();
		}
	}

	/**
	 * Sets the preference for a certain context.
	 * 
	 * @param manager
	 *            the working copy manager
	 * @param context
	 *            the scope context
	 * @param key
	 *            the key of the preference
	 * @param value
	 *            the value of the preference
	 */
	private void setPreference(final IWorkingCopyManager manager, final IScopeContext context, final String key, final String value) {
		final IEclipsePreferences preferences= getPreferences(manager, context);
		if (value != null)
			preferences.put(key, value);
		else
			preferences.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(final boolean visible) {
		fHasProjectHistory= hasSharedRefactoringHistory();
		super.setVisible(visible);
	}
}
