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

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ScriptingMessages;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Default implementation of a refactoring descriptor delete query.
 * 
 * @since 3.3
 */
public final class RefactoringDescriptorDeleteQuery implements IRefactoringDescriptorDeleteQuery {

	/** Preference key for the warn delete preference */
	private static final String PREFERENCE_DO_NOT_WARN_DELETE= RefactoringUIPlugin.getPluginId() + ".do.not.warn.delete.descriptor"; //$NON-NLS-1$;

	/** The number of descriptors to delete */
	private final int fCount;

	/** The project to use */
	private final IProject fProject;

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
	 * @param project
	 *            the project to use, or <code>null</code>
	 * @param count
	 *            the number of descriptors to delete
	 */
	public RefactoringDescriptorDeleteQuery(final Shell shell, final IProject project, final int count) {
		Assert.isNotNull(shell);
		Assert.isTrue(count >= 0);
		fShell= shell;
		fProject= project;
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
		if (!fWarned) {
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE)) {
				fShell.getDisplay().syncExec(new Runnable() {

					public final void run() {
						if (!fShell.isDisposed()) {
							final String count= new Integer(fCount).toString();
							String message= null;
							if (fProject != null)
								message= Messages.format(RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_pattern, new String[] { count, fProject.getName()});
							else
								message= Messages.format(ScriptingMessages.ShowRefactoringHistoryWizard_confirm_deletion, count);
							final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(fShell, RefactoringUIMessages.RefactoringPropertyPage_confirm_delete_caption, message, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, store.getBoolean(PREFERENCE_DO_NOT_WARN_DELETE), null, null);
							store.setValue(PREFERENCE_DO_NOT_WARN_DELETE, dialog.getToggleState());
							fReturnCode= dialog.getReturnCode();
						}
					}
				});
			} else
				fReturnCode= IDialogConstants.YES_ID;
		}
		fWarned= true;
		if (fReturnCode == IDialogConstants.YES_ID)
			return new RefactoringStatus();
		return RefactoringStatus.createErrorStatus(IDialogConstants.NO_LABEL);
	}
}