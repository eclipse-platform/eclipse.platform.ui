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
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import java.lang.reflect.InvocationTargetException;

/**
 * An operation which potentially makes changes to the workspace. All resource 
 * modification should be performed using this operation. The primary 
 * consequence of using this operation is that events which typically occur as a
 * result of workspace changes (such as the firing of resource deltas, 
 * performance of autobuilds, etc.) are deferred until the outermost operation
 * has successfully completed.
 * <p>
 * Subclasses must implement <code>execute</code> to do the work of the
 * operation.
 * </p>
 */
public abstract class WorkspaceModifyOperation
	implements IRunnableWithProgress
{
/**
 * Creates a new operation.
 */
protected WorkspaceModifyOperation() {
}
/**
 * Performs the steps that are to be treated as a single logical workspace
 * change.
 * <p>
 * Subclasses must implement this method.
 * </p>
 *
 * @param monitor the progress monitor to use to display progress and field
 *   user requests to cancel
 * @exception CoreException if the operation fails due to a CoreException
 * @exception InvocationTargetException if the operation fails due to an exception other than CoreException
 * @exception InterruptedException if the operation detects a request to cancel, 
 *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
 *  <code>InterruptedException</code>.  It is also possible to throw 
 *  <code>OperationCanceledException</code>, which gets mapped to <code>InterruptedException</code>
 *  by the <code>run</code> method.
 */
protected abstract void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException;
/**
 * The <code>WorkspaceModifyOperation</code> implementation of this 
 * <code>IRunnableWithProgress</code> method initiates a batch of changes by 
 * invoking the <code>execute</code> method as a workspace runnable 
 * (<code>IWorkspaceRunnable</code>).
 */
public synchronized final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException { 
	final InvocationTargetException[] iteHolder = new InvocationTargetException[1];
	try {
		IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) throws CoreException {
				try {
					execute(pm);
				}
				catch (InvocationTargetException e) {
					// Pass it outside the workspace runnable
					iteHolder[0] = e;
				}
				catch (InterruptedException e) {
					// Re-throw as OperationCanceledException, which will be
					// caught and re-thrown as InterruptedException below.
					throw new OperationCanceledException(e.getMessage());
				}
				// CoreException and OperationCanceledException are propagated
			}
		};
		WorkbenchPlugin.getPluginWorkspace().run(workspaceRunnable, monitor);
	} catch (CoreException e) {
		throw new InvocationTargetException(e);
	} catch (OperationCanceledException e) {
		throw new InterruptedException(e.getMessage());
	}
	// Re-throw the InvocationTargetException, if any occurred
	if (iteHolder[0] != null) {
		throw iteHolder[0];
	}
}
}
