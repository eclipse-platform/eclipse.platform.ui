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
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusDialog;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.history.SortableRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.IRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.ISortableRefactoringHistoryControl;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Central access point to access resources managed by the refactoring UI
 * plug-in.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringUI {

	/**
	 * Creates a light-weight dialog to present a {@link RefactoringStatus} to
	 * the user. The following values are returned from the dialogs open method:
	 * <ul>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#OK_ID IDialogConstants#OK_ID}:
	 * if the user has pressed the continue button.</li>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#CANCEL_ID IDialogConstants#CANCEL_ID}:
	 * if the user has pressed the cancel button.</li>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#BACK_ID IDialogConstants#BACK_ID}:
	 * if the user has pressed the back button.</li>
	 * </ul>
	 *
	 * @param status
	 *            the status to present
	 * @param parent
	 *            the parent shell of the dialog. May be <code>null</code> if
	 *            the dialog is a top-level dialog
	 * @param windowTitle
	 *            the dialog's window title
	 * @return a dialog to present a refactoring status.
	 *
	 * @since 3.2
	 */
	public static Dialog createLightWeightStatusDialog(RefactoringStatus status, Shell parent, String windowTitle) {
		return new RefactoringStatusDialog(status, parent, windowTitle, false, true);
	}

	/**
	 * Creates a control capable of presenting a refactoring history. Clients of
	 * this method can assume that the returned composite is an instance of
	 * {@link IRefactoringHistoryControl}.
	 *
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration
	 * @return the refactoring history control
	 *
	 * @since 3.2
	 */
	public static Composite createRefactoringHistoryControl(Composite parent, RefactoringHistoryControlConfiguration configuration) {
		return new RefactoringHistoryControl(parent, configuration);
	}

	/**
	 * Creates a dialog to present a {@link RefactoringStatus} to the user. The
	 * following values are returned from the dialogs open method:
	 * <ul>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#OK_ID IDialogConstants#OK_ID}:
	 * if the user has pressed the continue button.</li>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#CANCEL_ID IDialogConstants#CANCEL_ID}:
	 * if the user has pressed the cancel button.</li>
	 * <li>{@link org.eclipse.jface.dialogs.IDialogConstants#BACK_ID IDialogConstants#BACK_ID}:
	 * if the user has pressed the back button.</li>
	 * </ul>
	 *
	 * @param status
	 *            the status to present
	 * @param parent
	 *            the parent shell of the dialog. May be <code>null</code> if
	 *            the dialog is a top-level dialog
	 * @param windowTitle
	 *            the dialog's window title
	 * @param backButton
	 *            if <code>true</code> the dialog will contain a back button;
	 *            otherwise no back button will be present.
	 * @return a dialog to present a refactoring status.
	 */
	public static Dialog createRefactoringStatusDialog(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton) {
		return new RefactoringStatusDialog(status, parent, windowTitle, backButton);
	}

	/**
	 * Creates a dialog capable of presenting the given refactoring wizard.
	 * Clients of this method can assume that the returned dialog is an instance
	 * of {@link org.eclipse.jface.wizard.IWizardContainer IWizardContainer}.
	 * However the dialog is not necessarily an instance of
	 * {@link org.eclipse.jface.wizard.WizardDialog WizardDialog}.
	 *
	 * @param wizard
	 *            the refactoring wizard to create a dialog for
	 * @param parent
	 *            the parent of the created dialog or <code>null</code> to
	 *            create a top-level dialog
	 *
	 * @return the dialog
	 */
	/* package */static Dialog createRefactoringWizardDialog(RefactoringWizard wizard, Shell parent) {
		Dialog result;
		if (wizard.needsWizardBasedUserInterface())
			result= new RefactoringWizardDialog(parent, wizard);
		else
			result= new RefactoringWizardDialog2(parent, wizard);
		return result;
	}

	/**
	 * Creates a control capable of presenting a refactoring history. Clients of
	 * this method can assume that the returned composite is an instance of
	 * {@link ISortableRefactoringHistoryControl}.
	 *
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration
	 * @return the refactoring history control
	 *
	 * @since 3.3
	 */
	public static Composite createSortableRefactoringHistoryControl(Composite parent, RefactoringHistoryControlConfiguration configuration) {
		return new SortableRefactoringHistoryControl(parent, configuration);
	}

	/**
	 * Creates a special perform change operations that knows how to batch undo
	 * operations in open editors into one undo object. The operation batches
	 * the undo operations for those editors that implement the interface
	 * {@link org.eclipse.jface.text.IRewriteTarget}.
	 *
	 * @param change
	 *            the change to perform
	 *
	 * @return a special perform change operation that knows how to batch undo
	 *         operations for open editors if they implement
	 *         <code>IRewriteTarget
	 *  </code>
	 * @deprecated use {@link PerformChangeOperation#PerformChangeOperation(Change)}.
	 *             Since 3.1, undo batching is implemented in {@link TextChange}.
	 */
	public static PerformChangeOperation createUIAwareChangeOperation(Change change) {
		return new PerformChangeOperation(change);
	}

	private RefactoringUI() {
		// no instance
	}
}
