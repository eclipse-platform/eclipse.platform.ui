/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardContainer;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;

public class UIPerformChangeOperation extends PerformChangeOperation {

	private Display fDisplay;
	private IWizardContainer fWizardContainer;

	public UIPerformChangeOperation(Display display, Change change, IWizardContainer container) {
		super(change);
		fDisplay= display;
		fWizardContainer= container;
	}

	public UIPerformChangeOperation(Display display, CreateChangeOperation op, IWizardContainer container) {
		super(op);
		fDisplay= display;
		fWizardContainer= container;
	}

	protected void executeChange(final IProgressMonitor pm) throws CoreException {
		if (fDisplay != null && !fDisplay.isDisposed()) {
			final Throwable[] exception= new Throwable[1];
			/** Cancel button to re-enable, or <code>null</code> to do nothing. */
			final Button[] cancelToEnable= new Button[1];
			
			final ISafeRunnable safeRunnable= new ISafeRunnable() {
				public void run() {
					Button cancel= getCancelButton();
					if (cancel != null && !cancel.isDisposed() && cancel.isEnabled()) {
						cancelToEnable[0]= cancel;
						cancel.setEnabled(false);
					}
				}
				public void handleException(Throwable e) {
					exception[0]= e;
				}
			};
			Runnable r= new Runnable() {
				public void run() {
					SafeRunner.run(safeRunnable);
				}
			};
			try {
				fDisplay.syncExec(r);
				if (exception[0] != null) {
					if (exception[0] instanceof CoreException) {
						IStatus status= ((CoreException)exception[0]).getStatus();
						// it is more important to get the original cause of the
						// exception. Therefore create a new status and take
						// over the exception trace from the UI thread.
						throw new CoreException(new MultiStatus(
								RefactoringUIPlugin.getPluginId(), IStatus.ERROR,
								new IStatus[] {status}, status.getMessage(), exception[0]));
					} else {
						String message= exception[0].getMessage();
						throw new CoreException(new Status(
							IStatus.ERROR, RefactoringUIPlugin.getPluginId(),IStatus.ERROR,
							message == null
								? RefactoringUIMessages.ChangeExceptionHandler_no_details
								: message,
							exception[0]));
					}
				}
				super.executeChange(pm);
			} finally {
				if (cancelToEnable[0] != null) {
					fDisplay.syncExec(new Runnable() {
						public void run() {
							if (!cancelToEnable[0].isDisposed()) {
								cancelToEnable[0].setEnabled(true);
							}
						}
					});
				}
			}
		}
	}

	private Button getCancelButton() {
		if (fWizardContainer instanceof RefactoringWizardDialog2) {
			return ((RefactoringWizardDialog2)fWizardContainer).getButton(IDialogConstants.CANCEL_ID);
		} else if (fWizardContainer instanceof RefactoringWizardDialog) {
			return ((RefactoringWizardDialog)fWizardContainer).getButton(IDialogConstants.CANCEL_ID);
		}
		return null;
	}
}
