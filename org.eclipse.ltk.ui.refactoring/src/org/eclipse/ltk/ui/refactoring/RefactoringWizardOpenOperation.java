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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardContainer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * A helper class to open a refactoring wizard dialog. The class first checks
 * the initial conditions of the refactoring and depending on its outcome
 * the wizard dialog or an error dialog is shown.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0 
 * 
 * @noextend This class is not intended to be subclassed by clients.
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
	 * @return the outcome of the initial condition checking or <code>null</code>
	 *  if the condition checking hasn't been performed yet
	 */
	public RefactoringStatus getInitialConditionCheckingStatus() {
		return fInitialConditions;
	}
	
	/**
	 * Opens the refactoring dialog for the refactoring wizard passed to the constructor. 
	 * The method first checks the initial conditions of the refactoring. If the condition 
	 * checking returns a status with a severity of {@link RefactoringStatus#FATAL} then
	 * a message dialog is opened containing the corresponding status message. No wizard 
	 * dialog is opened in this situation. If the condition checking passes then the 
	 * refactoring dialog is opened. 
	 * <p>
	 * The methods ensures that the workspace lock is held while the condition checking, 
	 * change creation and change execution is performed. Clients can't make any assumption
	 * about the thread in which these steps are executed. However the framework ensures
	 * that the workspace lock is transfered to the thread in which the execution of the
	 * steps takes place. 
	 * </p>
	 * @param parent the parent shell for the dialog or <code>null</code> if the dialog
	 *  is a top level dialog
	 * @param dialogTitle the dialog title of the message box presenting the failed
	 *  condition check (if any)
	 *   
	 * @return {@link #INITIAL_CONDITION_CHECKING_FAILED} if the initial condition checking
	 *  failed and no wizard dialog was presented. Otherwise either {@link IDialogConstants#OK_ID}
	 *  or {@link IDialogConstants#CANCEL_ID} is returned depending on whether the user
	 *  has pressed the OK or cancel button on the wizard dialog.
	 * 
	 * @throws InterruptedException if the initial condition checking got canceled by
	 *  the user.
	 */
	public int run(final Shell parent, final String dialogTitle) throws InterruptedException {
		Assert.isNotNull(dialogTitle);
		final Refactoring refactoring= fWizard.getRefactoring();
		final IJobManager manager= Job.getJobManager();
		final int[] result= new int[1];
		final InterruptedException[] canceled= new InterruptedException[1];
		Runnable r= new Runnable() {
			public void run() {
				try {
					// we are getting the block dialog for free if we pass in null
					manager.beginRule(ResourcesPlugin.getWorkspace().getRoot(), null);
					
					refactoring.setValidationContext(parent);
					fInitialConditions= checkInitialConditions(refactoring, parent, dialogTitle);
					if (fInitialConditions.hasFatalError()) {
						String message= fInitialConditions.getMessageMatchingSeverity(RefactoringStatus.FATAL);
						MessageDialog.openInformation(parent, dialogTitle, message);
						result[0]= INITIAL_CONDITION_CHECKING_FAILED;
					} else {
						fWizard.setInitialConditionCheckingStatus(fInitialConditions);
						Dialog dialog= RefactoringUI.createRefactoringWizardDialog(fWizard, parent);
						dialog.create();
						IWizardContainer wizardContainer= (IWizardContainer) dialog;
						if (wizardContainer.getCurrentPage() == null)
							/*
							 * Don't show the dialog at all if there are no user
							 * input pages and change creation was cancelled.
							 */
							result[0]= Window.CANCEL;
						else
							result[0]= dialog.open();
					} 
				} catch (InterruptedException e) {
					canceled[0]= e;
				} catch (OperationCanceledException e) {
					canceled[0]= new InterruptedException(e.getMessage());
				} finally {
					manager.endRule(ResourcesPlugin.getWorkspace().getRoot());
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
				RefactoringUIMessages.RefactoringUI_open_unexpected_exception);
			return RefactoringStatus.createFatalErrorStatus(
				RefactoringUIMessages.RefactoringUI_open_unexpected_exception);
		}
	}
}
