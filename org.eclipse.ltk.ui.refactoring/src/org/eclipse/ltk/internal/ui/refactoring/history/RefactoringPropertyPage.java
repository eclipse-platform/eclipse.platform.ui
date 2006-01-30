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

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;

import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;

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

	/** The refactoring descriptor delete query */
	private class RefactoringDescriptorDeleteQuery implements IRefactoringDescriptorDeleteQuery {

		/**
		 * {@inheritDoc}
		 */
		public RefactoringStatus proceed(final RefactoringDescriptorProxy proxy) {
			Assert.isNotNull(proxy);
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			MessageDialogWithToggle dialog= null;
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE)) {
				final String project= proxy.getProject();
				dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_caption, (project == null || "".equals(project)) ? RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_workspace_pattern : RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_project_pattern, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null); //$NON-NLS-1$
				store.setValue(PREFERENCE_DO_NOT_WARN_DELETE, dialog.getToggleState());
			}
			if (dialog == null || dialog.getReturnCode() == IDialogConstants.YES_ID)
				return new RefactoringStatus();
			return RefactoringStatus.createErrorStatus(IDialogConstants.NO_LABEL);
		}
	}

	/** Preference key for the warn delete preference */
	private static final String PREFERENCE_DO_NOT_WARN_DELETE= RefactoringUIPlugin.getPluginId() + ".do.not.warn.delete.descriptor"; //$NON-NLS-1$;

	/** Preference key for the warn delete all preference */
	private static final String PREFERENCE_DO_NOT_WARN_DELETE_ALL= RefactoringUIPlugin.getPluginId() + ".do.not.warn.delete.history"; //$NON-NLS-1$;

	/** The refactoring preference */
	private boolean fHasProjectHistory= false;

	/** The preferences working copy manager, or <code>null</code> */
	private IWorkingCopyManager fManager= null;

	/** The share history button, or <code>null</code> */
	private Button fShareHistory= null;

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
		layout.marginHeight= 0;
		layout.marginRight= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);

		final Label label= new Label(composite, SWT.WRAP);
		label.setText(RefactoringUIMessages.RefactoringPropertyPage_label_message);

		final ManageRefactoringHistoryControl control= new ManageRefactoringHistoryControl(composite, new RefactoringHistoryControlConfiguration(getCurrentProject(), true, true));
		control.createControl();
		control.getDeleteAllButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final IProject project= getCurrentProject();
				final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
				MessageDialogWithToggle dialog= null;
				if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE_ALL) && !control.getInput().isEmpty()) {
					dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_all_caption, Messages.format(RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_all_pattern, project.getName()), RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
					store.setValue(PREFERENCE_DO_NOT_WARN_DELETE_ALL, dialog.getToggleState());
				}
				if (dialog == null || dialog.getReturnCode() == IDialogConstants.YES_ID) {
					RefactoringHistoryService service= RefactoringHistoryService.getInstance();
					try {
						service.connect();
						try {
							service.deleteRefactoringHistory(project, null);
						} catch (CoreException exception) {
							final Throwable throwable= exception.getStatus().getException();
							if (throwable instanceof IOException)
								MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
							else
								RefactoringUIPlugin.log(exception);
						}
						control.setInput(service.getProjectHistory(project, null));
					} finally {
						service.disconnect();
					}
				}
			}
		});
		control.getDeleteButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] selection= control.getCheckedDescriptors();
				if (selection.length > 0) {
					RefactoringHistoryService service= RefactoringHistoryService.getInstance();
					try {
						service.connect();
						try {
							service.deleteRefactoringDescriptors(selection, new RefactoringDescriptorDeleteQuery(), null);
						} catch (CoreException exception) {
							final Throwable throwable= exception.getStatus().getException();
							if (throwable instanceof IOException)
								MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
							else
								RefactoringUIPlugin.log(exception);
						}
						control.setInput(service.getProjectHistory(getCurrentProject(), null));
					} finally {
						service.disconnect();
					}
				}
			}
		});
		control.getEditButton().addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				String comment= ""; //$NON-NLS-1$
				final RefactoringDescriptorProxy[] selection= control.getSelectedDescriptors();
				if (selection.length > 0) {
					RefactoringHistoryService service= RefactoringHistoryService.getInstance();
					try {
						service.connect();
						final RefactoringDescriptor descriptor= selection[0].requestDescriptor(null);
						if (descriptor != null) {
							final String current= descriptor.getComment();
							if (current != null)
								comment= current;
						}
						final InputDialog dialog= new InputDialog(getShell(), RefactoringUIMessages.RefactoringPropertyPage_edit_caption, RefactoringUIMessages.RefactoringPropertyPage_edit_message, comment, null);
						if (dialog.open() == 0) {
							service.setRefactoringComment(selection[0], dialog.getValue(), null);
							control.setSelectedDescriptors(new RefactoringDescriptorProxy[0]);
							control.setSelectedDescriptors(new RefactoringDescriptorProxy[] { selection[0]});
						}
					} catch (CoreException exception) {
						RefactoringUIPlugin.log(exception);
					} finally {
						service.disconnect();
					}
				}
			}
		});
		fShareHistory= new Button(composite, SWT.CHECK);
		fShareHistory.setText(RefactoringUIMessages.RefactoringPropertyPage_share_message);
		fShareHistory.setData(RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY);

		fShareHistory.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fShareHistory.setSelection(hasSharedRefactoringHistory());

		new Label(composite, SWT.NONE);

		final IProject project= getCurrentProject();
		if (project != null) {
			final IRefactoringHistoryService service= RefactoringCore.getRefactoringHistoryService();
			try {
				service.connect();
				control.setInput(service.getProjectHistory(project, null));
			} finally {
				service.disconnect();
			}
		}
		applyDialogFont(composite);

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
	 * Returns whether a project has an shared refactoring history.
	 * 
	 * @return <code>true</code> if the project contains an shared project
	 *         history, <code>false</code> otherwise
	 */
	private boolean hasSharedRefactoringHistory() {
		final IProject project= getCurrentProject();
		if (project != null)
			return RefactoringHistoryService.getInstance().hasSharedRefactoringHistory(project);
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
		final IProject project= getCurrentProject();
		if (project != null)
			setPreference(fManager, new ProjectScope(project), RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, Boolean.valueOf(fShareHistory.getSelection()).toString());
		if (fManager != null)
			try {
				fManager.applyChanges();
				final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
				final boolean history= service.hasSharedRefactoringHistory(project);
				if (history != fHasProjectHistory && project != null)
					service.setSharedRefactoringHistory(project, history, null);
			} catch (BackingStoreException exception) {
				RefactoringUIPlugin.log(exception);
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
		return super.performOk();
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