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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;


/**
 * This class is the abstract superclass for CVS operations. It provides
 * error handling, prompting and other UI.
 */
public abstract class CVSOperation implements IRunnableWithProgress {

	private int statusCount;

	private boolean involvesMultipleResources = false;

	private List errors = new ArrayList(); // of IStatus

	protected static final IStatus OK = Status.OK_STATUS; //$NON-NLS-1$
	
	// shell to be used if the runnabl context is a blocking context
	private Shell shell;
	private boolean modifiesWorkspace = true;
	
	// instance variable used to indicate behavior while prompting for overwrite
	private boolean confirmOverwrite = true;
	
	ICVSRunnableContext cvsRunnableContext;
	
	public CVSOperation(Shell shell) {
		this.shell = shell;
	}

	/**
	 * Run the operation. Progress feedback will be provided by one of the following mechanisms
	 * (in priotiry order):
	 * <ol>
	 * <li>the runnable context assigned to the operation 
	 * <li>a background job (if supported by the operation and enabled through the preferences)
	 * <li>the workbench active page
	 * </ol>
	 * @throws CVSException
	 * @throws InterruptedException
	 */
	public synchronized void run() throws CVSException, InterruptedException {
		ICVSRunnableContext context = getCVSRunnableContext();
		try {
			getCVSRunnableContext().run(getTaskName(), getSchedulingRule(), getPostponeBuild(), this);
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		}
	}

	protected boolean areJobsEnabled() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.BACKGROUND_OPERATIONS);
	}

	/**
	 * Returns true if the operation can be run as a background job.
	 * The default is to support running as a job. Subclass should override
	 * to prevent background execution of the operation.
	 * @return whether operation can be run as a job
	 */
	public boolean canRunAsJob() {
		return true;
	}
	
	/**
	 * Return the scheduling rule that defines the scope of the whole operation.
	 * This method must either return <code>null</code> (in which case, code executed
	 * by the operation can attempt to obtain any scheduling rules they like but may be
	 * blocked by other jobs at that point) or a rule (e.g. IResource) that encompasses
	 * all scheduling rules used by code nested in the operation (in which case the
	 * operation wil not start until the encompassing rule is free but once the
	 * operation starts, nested rules will not block on any subsequent contained 
	 * scheduling rules). By default, <code>null</code> is returned.
	 * @return
	 */
	protected ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	/**
	 * Return whether the auto-build should be postponed whil ethe operation is running.
	 * The default is to postone a build.
	 * @return
	 */
	protected boolean getPostponeBuild() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		startOperation();
		try {
			execute(monitor);
			endOperation();
		} catch (CVSException e) {
			// TODO: errors may not be empty (i.e. endOperation has not been executed)
			throw new InvocationTargetException(e);
		}
	}
	
	protected void startOperation() {
		statusCount = 0;
		resetErrors();
		confirmOverwrite = true;
	}
	
	protected void endOperation() throws CVSException {
		handleErrors((IStatus[]) errors.toArray(new IStatus[errors.size()]));
	}

	/**
	 * Subclasses must override this method to perform the operation.
	 * Clients should never call this method directly.
	 * 
	 * @param monitor
	 * @throws CVSException
	 * @throws InterruptedException
	 */
	protected abstract void execute(IProgressMonitor monitor) throws CVSException, InterruptedException;

	/*
	 * Return the ICVSRunnableContext which will be used to run the operation.
	 */
	private ICVSRunnableContext getCVSRunnableContext() {
		if (cvsRunnableContext == null) {
			if (canRunAsJob() && areJobsEnabled()) {
				return new CVSNonblockingRunnableContext();
			} else {
				return new CVSBlockingRunnableContext(shell);
			}
		}
		return cvsRunnableContext;
	}
	
	/**
	 * Set the CVS runnable context to be used by the operation.
	 * Although this method can be used by clients, it's main
	 * purpose is to support the running of headless operations
	 * for testing purposes.
	 * @param cvsRunnableContext
	 */
	public void setCVSRunnableContext(ICVSRunnableContext cvsRunnableContext) {
		this.cvsRunnableContext = cvsRunnableContext;
	}
	
	public Shell getShell() {
		return getCVSRunnableContext().getShell();
	}

	public boolean isModifiesWorkspace() {
		return modifiesWorkspace;
	}

	public void setModifiesWorkspace(boolean b) {
		modifiesWorkspace = b;
	}

	protected void addError(IStatus status) {
		if (status.isOK()) return;
		if (isLastError(status)) return;
		errors.add(status);
	}

	protected void collectStatus(IStatus status)  {
		if (isLastError(status)) return;
		statusCount++;
		if (!status.isOK()) addError(status);
	}
	
	protected void resetErrors() {
		errors.clear();
		statusCount = 0;
	}
	
	/**
	 * Get the last error taht occured. This can be useful when a method
	 * has a return type but wants to signal an error. The method in question
	 * can add the error using <code>addError(IStatus)</code> and return null.
	 * The caller can then query the error using this method. Also, <code>addError(IStatus)</code>
	 * will not add the error if it is already on the end of the list (using identity comparison)
	 * which allows the caller to still perform a <code>collectStatus(IStatus)</code>
	 * to get a valid operation count.
	 * @return
	 */
	protected IStatus getLastError() {
		Assert.isTrue(errors.size() > 0);
		IStatus status = (IStatus)errors.get(errors.size() - 1);
		return status;
	}
	
	private boolean isLastError(IStatus status) {
		return (errors.size() > 0 && getLastError() == status);
	}
	
	protected void handleErrors(IStatus[] errors) throws CVSException {
		if (errors.length == 0) return;
		if (errors.length == 1 && statusCount == 1)  {
			throw new CVSException(errors[0]);
		}
		MultiStatus result = new MultiStatus(CVSUIPlugin.ID, 0, getErrorMessage(errors, statusCount), null);
		for (int i = 0; i < errors.length; i++) {
			IStatus s = errors[i];
			if (s.isMultiStatus()) {
				result.add(new CVSStatus(s.getSeverity(), s.getMessage(), s.getException()));
				result.addAll(s);
			} else {
				result.add(s);
			}
		}
		throw new CVSException(result);
	}

	protected String getErrorMessage(IStatus[] failures, int totalOperations) {
		return Policy.bind("CVSOperation.0", String.valueOf(failures.length),  String.valueOf(totalOperations)); //$NON-NLS-1$
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

	public void setInvolvesMultipleResources(boolean b) {
		involvesMultipleResources = b;
	}

	/**
	 * Return the string that is to be used as the task name for the operation
	 * 
	 * @param remoteFolders
	 * @return
	 */
	protected abstract String getTaskName();

}
