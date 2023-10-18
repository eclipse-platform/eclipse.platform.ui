/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;

public class ValidationCheckResultQuery implements IValidationCheckResultQuery  {
	private Shell fParent;
	private String fTitle;
	public ValidationCheckResultQuery(Shell parent, String title) {
		fParent= parent;
		fTitle= title;
	}
	@Override
	public boolean proceed(RefactoringStatus status) {
		final Dialog dialog= RefactoringUI.createRefactoringStatusDialog(status, fParent, fTitle, false);
		final int[] result= new int[1];
		Runnable r= () -> result[0]= dialog.open();
		fParent.getDisplay().syncExec(r);
		return result[0] == IDialogConstants.OK_ID;
	}
	@Override
	public void stopped(final RefactoringStatus status) {
		Runnable r= () -> {
			String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
			MessageDialog.openWarning(fParent, fTitle, getFullMessage(message));
		};
		fParent.getDisplay().syncExec(r);
	}
	private String getFullMessage(String errorMessage) {
		return Messages.format(
			RefactoringUIMessages.ValidationCheckResultQuery_error_message,
			errorMessage);
	}
}