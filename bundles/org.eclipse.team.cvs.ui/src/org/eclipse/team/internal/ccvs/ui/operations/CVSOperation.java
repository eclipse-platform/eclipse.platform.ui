/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.PlatformUI;


/**
 * This class is the abstract superclass for CVS operations. It provides
 * error handling, prompting and other UI.
 */
public abstract class CVSOperation implements IRunnableWithProgress {

	private boolean involvesMultipleResources = false;

	private List errors = new ArrayList(); // of IStatus

	protected static final IStatus OK = new CVSStatus(IStatus.OK, Policy.bind("ok")); // $NON-NLS-1$
	
	private IRunnableContext runnableContext;
	private Shell shell;
	private boolean interruptable = true;
	private boolean modifiesWorkspace = true;
	
	// instance variable used to indicate behavior while prompting for overwrite
	private boolean confirmOverwrite = true;
	
	public static void run(Shell shell, CVSOperation operation) throws CVSException, InterruptedException {
		operation.setShell(shell);
		operation.setRunnableContext(new ProgressMonitorDialog(shell));
		operation.execute();
	}
	
	/**
	 * @param shell
	 */
	public CVSOperation(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * Execute the operation in the given runnable context. If null is passed, 
	 * the runnable context assigned to the operation is used.
	 * 
	 * @throws InterruptedException
	 * @throws CVSException
	 */
	public void execute(IRunnableContext aRunnableContext) throws InterruptedException, CVSException {
		if (aRunnableContext == null) {
			aRunnableContext = getRunnableContext();
		}
		try {
			aRunnableContext.run(isInterruptable(), isInterruptable(), this);
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		}
	}
	
	public void executeWithProgress() throws CVSException, InterruptedException {
		execute(new ProgressMonitorDialog(getShell()));
	}
	
	/**
	 * Execute the operation in the runnable context that has been assigned to the operation.
	 * If a context has not been assigned, the workbench window is used.
	 * 
	 * @throws InterruptedException
	 * @throws CVSException
	 */
	public void execute() throws InterruptedException, CVSException {
		execute(getRunnableContext());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		startOperation();
		try {
			if (isModifiesWorkspace()) {
				new CVSWorkspaceModifyOperation(this).execute(monitor);
			} else {
				execute(monitor);
			}
			endOperation();
		} catch (CVSException e) {
			// TODO: errors may not be empty
			throw new InvocationTargetException(e);
		} catch (CoreException e) {
			// TODO: errors may not be empty
			throw new InvocationTargetException(e);
		}
	}

	protected void startOperation() {
		resetErrors();
		confirmOverwrite = true;
	}
	
	protected void endOperation() throws CVSException {
		handleErrors((IStatus[]) errors.toArray(new IStatus[errors.size()]));
	}

	/**
	 * Subclasses must override to perform the operation
	 * @param monitor
	 * @throws CVSException
	 * @throws InterruptedException
	 */
	public abstract void execute(IProgressMonitor monitor) throws CVSException, InterruptedException;

	/**
	 * @return
	 */
	private IRunnableContext getRunnableContext() {
		if (runnableContext == null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		return runnableContext;
	}

	/**
	 * @param context
	 */
	public void setRunnableContext(IRunnableContext context) {
		this.runnableContext = context;
	}

	/**
	 * @return
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * @param shell
	 */
	public void setShell(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * @return
	 */
	public boolean isInterruptable() {
		return interruptable;
	}
	
	/**
	 * @param b
	 */
	public void setInterruptable(boolean b) {
		interruptable = b;
	}

	/**
	 * @return
	 */
	public boolean isModifiesWorkspace() {
		return modifiesWorkspace;
	}

	/**
	 * @param b
	 */
	public void setModifiesWorkspace(boolean b) {
		modifiesWorkspace = b;
	}

	/**
	 * @param status
	 */
	protected void addError(IStatus status) {
		errors.add(status);
	}
	
	/**
	 * 
	 */
	protected void resetErrors() {
		errors.clear();
	}
	
	/**
	 * @param statuses
	 */
	protected void handleErrors(IStatus[] status) throws CVSException {
		if (status.length == 0) return;
		MultiStatus result = new MultiStatus(CVSUIPlugin.ID, 0, getErrorTitle(), null);
		for (int i = 0; i < status.length; i++) {
			IStatus s = status[i];
			if (s.isMultiStatus()) {
				result.add(new CVSStatus(s.getSeverity(), s.getMessage(), s.getException()));
				result.addAll(s);
			} else {
				result.add(s);
			}
		}
	}

	/**
	 * Provide the message used in the error status if an error occurs.
	 * Should be overriden by subclasses.
	 */
	protected String getErrorTitle() {
		return "Errors occured during this operation";
	}

	/**
	 * This method prompts the user to overwrite an existing resource. It uses the
	 * <code>involvesMultipleResources</code> to determine what buttons to show.
	 * @param project
	 * @return
	 */
	protected boolean promptToOverwrite(String title, String msg) {
		if (!confirmOverwrite) {
			return true;
		}
		String buttons[];
		if (involvesMultipleResources()) {
			buttons = new String[] {
				IDialogConstants.YES_LABEL, 
				IDialogConstants.YES_TO_ALL_LABEL, 
				IDialogConstants.NO_LABEL, 
				IDialogConstants.CANCEL_LABEL};
		} else {
			buttons = new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL};
		}	
		Shell displayShell = getShell();
		final MessageDialog dialog = 
			new MessageDialog(displayShell, title, null, msg, MessageDialog.QUESTION, buttons, 0);

		// run in syncExec because callback is from an operation,
		// which is probably not running in the UI thread.
		displayShell.getDisplay().syncExec(
			new Runnable() {
				public void run() {
					dialog.open();
				}
			});
		if (involvesMultipleResources()) {
			switch (dialog.getReturnCode()) {
				case 0://Yes
					return true;
				case 1://Yes to all
					confirmOverwrite = false; 
					return true;
				case 2://No
					return false;
				case 3://Cancel
				default:
					throw new OperationCanceledException();
			}
		} else {
			return dialog.getReturnCode() == 0;
		}
	}

	/**
	 * This method is used by <code>promptToOverwrite</code> to determine which 
	 * buttons to show in the prompter.
	 * 
	 * @return
	 */
	protected boolean involvesMultipleResources() {
		return involvesMultipleResources;
	}

	/**
	 * @param b
	 */
	public void setInvolvesMultipleResources(boolean b) {
		involvesMultipleResources = b;
	}
}
