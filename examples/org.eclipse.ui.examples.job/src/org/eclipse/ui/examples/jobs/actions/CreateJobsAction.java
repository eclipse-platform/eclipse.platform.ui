/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.actions;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.examples.jobs.TestJob;

/**
 * Test action that creates a number of fake jobs, and then waits until they complete.
 */
public class CreateJobsAction implements IWorkbenchWindowActionDelegate {
	static final long DELAY = 100;

	private IWorkbenchWindow window;

	private long askForDuration() {
		InputDialog dialog = new InputDialog(window.getShell(), "How long?", "Enter the number of milliseconds per job", "1000", new IInputValidator() { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					@Override
					public String isValid(String newText) {
						try {
							Long.parseLong(newText);
						} catch (NumberFormatException e) {
							return "Not a number"; //$NON-NLS-1$
						}
						return null;
					}
				});
		if (dialog.open() == Window.CANCEL)
			throw new OperationCanceledException();
		return Long.parseLong(dialog.getValue());
	}

	private boolean askForExclusive() {
		MessageDialog dialog = new MessageDialog(window.getShell(), "Likes to be left alone?", //$NON-NLS-1$
				null, "Press yes if the jobs should be run one at a time, and no otherwise", //$NON-NLS-1$
				MessageDialog.QUESTION, 1, IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL);
		return dialog.open() == 0;
	}

	private boolean askForFailure() {
		MessageDialog dialog = new MessageDialog(window.getShell(), "Born to fail?", //$NON-NLS-1$
				null, "Should the jobs return an error status?", //$NON-NLS-1$
				MessageDialog.QUESTION, 1, IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL// no is the default
		);
		return dialog.open() == 0;
	}

	private int askForJobCount() {
		InputDialog dialog = new InputDialog(window.getShell(), "How much work?", "Enter the number of jobs to run", "100", new IInputValidator() { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					@Override
					public String isValid(String newText) {
						try {
							Integer.parseInt(newText);
						} catch (NumberFormatException e) {
							return "Not a number"; //$NON-NLS-1$
						}
						return null;
					}
				});
		if (dialog.open() == Window.CANCEL)
			throw new OperationCanceledException();
		return Integer.parseInt(dialog.getValue());
	}

	@Override
	public void dispose() {
		//do nothing
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		int jobCount = askForJobCount();
		long duration = askForDuration();
		boolean exclusive = askForExclusive();
		boolean failure = askForFailure();
		for (int i = 0; i < jobCount; i++) {
			new TestJob(duration, exclusive, failure, false, false, 0).schedule(DELAY);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}
}