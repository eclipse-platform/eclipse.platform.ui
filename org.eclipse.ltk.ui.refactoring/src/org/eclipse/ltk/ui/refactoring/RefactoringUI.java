/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusDialog;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.ltk.internal.ui.refactoring.UIPerformChangeOperation;

/**
 * Central access point to access resources managed by the refactoring
 * ui plug-in.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public class RefactoringUI {
	
	private RefactoringUI() {
		// no instance
	}
	
	/**
	 * Creates a dialog to present a {@link RefactoringStatus} to the user. Depending
	 * on the parameter <code>backButton</code> the following values are returned
	 * from the dialogs open method: {@link org.eclipse.jface.dialogs.IDialogConstants#OK_ID
	 * IDialogConstants#OK_ID} if the user has pressed the continue button, 
	 * {@link org.eclipse.jface.dialogs.IDialogConstants#CANCEL_ID IDialogConstants#CANCEL_ID}
	 * if the user has pressed the cancel button or 
	 * {@link org.eclipse.jface.dialogs.IDialogConstants#BACK_ID IDialogConstants#BACK_ID} if
	 * the user has pressed the back button.
	 * 
	 * @param status the status to present
	 * @param parent the parent shell of the dialog. May be <code>null</code>
	 *  if the dialog is unparented
	 * @param windowTitle the dialog's window title
	 * @param backButton if <code>true</code> the dialog will contain a back button;
	 *  otherwise no back button will be present.
	 * @return a dialog to present a refactoring status.
	 */
	public static Dialog createRefactoringStatusDialog(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton) {
		return new RefactoringStatusDialog(status, parent, windowTitle, backButton);
	}
	
	/**
	 * Creates a dialog capable to present the given refactoring wizard. Clients of
	 * this method can assume that the returned dialog is an instance of 
	 * {@link org.eclipse.jface.wizard.IWizardContainer IWizardContainer}. However the 
	 * dialog is not necessarily an instance of {@link org.eclipse.jface.wizard.WizardDialog
	 * WizardDialog}.
	 * 
	 * @param wizard the refactoring wizard to create a dialog for
	 * @param parent the parent of the created dialog or <code>null</code> if the dialog
	 *  is unparanted
	 * 
	 * @return the dialog 
	 */
	/* package */ static Dialog createRefactoringWizardDialog(RefactoringWizard wizard, Shell parent) {
		Dialog result;
		if (wizard.needsWizardBasedUserInterface())
			result= new RefactoringWizardDialog(parent, wizard);
		else 
			result= new RefactoringWizardDialog2(parent, wizard);
		return result;
	}
	
	/**
	 * Creates a special perform change operations that knows how to batch
	 * undo operations in open editors into one undo object. The operation
	 * batches the undo operations for those editors that implement the
	 * interface {@link org.eclipse.jface.text.IRewriteTarget}.
	 * 
	 * @param change the change to perform
	 * 
	 * @return a special perform change operation that knows how to batch
	 *  undo operations for open editors
	 */
	public static PerformChangeOperation createUIAwareChangeOperation(Change change) {
		return new UIPerformChangeOperation(change);
	}	
}
