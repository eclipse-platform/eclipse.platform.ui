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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;

/**
 * A helper class to open a refactoring wizard dialog. The class first checks
 * the initial conditions of the refactoring and depending on its outcome
 * the wizard dialog or an error dialog is shown.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0 
 */ 
public class RefactoringWizardOpenOperation {

	private RefactoringWizard fWizard;
	private RefactoringStatus fInitialConditions;
	
	/**
	 * Constant (value 1025) indicating that the precondition check failed
	 * when opening a refactoring wizard dialog.
	 * 
	 * @see #run(Shell, String)
	 */
	public static final int INITIAL_CONDITION_CHECKING_FAILED= IDialogConstants.CLIENT_ID + 1;
	
	/**
	 * Creates a new refactoring wizard starter for the given wizard.
	 * 
	 * @param wizard the wizard to open a dialog for
	 */
	public RefactoringWizardOpenOperation(RefactoringWizard wizard) {
		Assert.isNotNull(wizard);
		fWizard= wizard;
	}
	
	/**
	 * Returns the outcome of the initial condition checking.
	 * 
	 * @return the outcome of the initial condition checking
	 */
	public RefactoringStatus getInitialConditionCheckingStatus() {
		return fInitialConditions;
	}
	
	/**
	 * Opens the refactoring dialog for the refactoring wizard passed in the constructor. 
	 * The method first checks the initial conditions of the refactoring. If the condition 
	 * checking returns a status with a severity of <code>RefactoringStatus#Fatal</code> then
	 * a message dialog is posted containing the corresponding status message. No wizard 
	 * dialog is opened in this situation. If the condition checking passes then the 
	 * refactoring dialog is opened. 
	 * 
	 * @param parent the parent shell for the dialog or <code>null</code> if the dialog
	 *  is unparanted
	 * @param dialogTitle the dialog title of the message box presenting the failed
	 *  condition check (if any)
	 *   
	 * @return {@link #INITIAL_CONDITION_CHECKING_FAILED} if the initial condition checking
	 *  failed and no wizard dialog was presented. Otherwise either {@link IDialogConstants#OK_ID}
	 *  or {@link IDialogConstants#CANCEL_ID} is returned depending on whether the user
	 *  has pressed the OK or cancel button on the wizard dialog.
	 * 
	 * @throws InterruptedException if the initial condition checking got cancelled by
	 *  the user.
	 */
	public int run(final Shell parent, final String dialogTitle) throws InterruptedException {
		Assert.isNotNull(dialogTitle);
		final Refactoring refactoring= fWizard.getRefactoring();
		final IJobManager manager= Platform.getJobManager();
		final int[] result= new int[1];
		final InterruptedException[] canceled= new InterruptedException[1];
		Runnable r= new Runnable() {
			public void run() {
				try {
					// we are getting the block dialog for free if we pass in null
					manager.suspend(ResourcesPlugin.getWorkspace().getRoot(), null);
					
					refactoring.setValidationContext(parent);
					fInitialConditions= checkInitialConditions(refactoring, parent, dialogTitle);
					if (fInitialConditions.hasFatalError()) {
						String message= fInitialConditions.getMessageMatchingSeverity(RefactoringStatus.FATAL);
						MessageDialog.openInformation(parent, dialogTitle, message);
						result[0]= INITIAL_CONDITION_CHECKING_FAILED;
						return;
					} else {
						fWizard.setInitialConditionCheckingStatus(fInitialConditions);
						Dialog dialog= RefactoringUI.createRefactoringWizardDialog(fWizard, parent);
						result[0]= dialog.open();
						return;
					} 
				} catch (InterruptedException e) {
					canceled[0]= e;
				} catch (OperationCanceledException e) {
					canceled[0]= new InterruptedException(e.getMessage());
				} finally {
					manager.resume(ResourcesPlugin.getWorkspace().getRoot());
					refactoring.setValidationContext(null);
				}		
			}
		};
		BusyIndicator.showWhile(parent.getDisplay(), r);
		if (canceled[0] != null)
			throw canceled[0];
		return result[0];
	}
	
	//---- private helper methods -----------------------------------------------------------------
	
	private RefactoringStatus checkInitialConditions(Refactoring refactoring, Shell parent, String title) throws InterruptedException {		
		try {
			CheckConditionsOperation cco= new CheckConditionsOperation(refactoring, CheckConditionsOperation.INITIAL_CONDITONS);
			IProgressService service= PlatformUI.getWorkbench().getProgressService();
			service.busyCursorWhile(new WorkbenchRunnableAdapter(cco, ResourcesPlugin.getWorkspace().getRoot()));
			return cco.getStatus();
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, parent, title, 
				RefactoringUIMessages.getString("RefactoringUI.open.unexpected_exception"));//$NON-NLS-1$
			return RefactoringStatus.createFatalErrorStatus(
				RefactoringUIMessages.getString("RefactoringUI.open.unexpected_exception"));//$NON-NLS-1$
		}
	}
}
