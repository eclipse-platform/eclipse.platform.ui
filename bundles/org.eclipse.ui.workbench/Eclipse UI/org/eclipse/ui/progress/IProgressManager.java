/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * The IProgressManager is an interface to the progress manager provided by the
 * workbench. <b>NOTE: This is experimental API and subject to change at any
 * time</b>.
 * 
 *  @since 3.0
 */
public interface IProgressManager {
	
	/**
	 * The time at which the busy cursor will be replaced with a progress monitor.
	 */
	public int LONG_OPERATION_MILLISECONDS = 5000;

	/**
	 * Block the current thread until UIJob is served. The message is used to
	 * announce to the user a pending UI Job.
	 * 
	 * <b>Note: This is experimental API and subject to change at any time </b>.	 * 
	 * 
	 * @param job
	 * @param message The message that informs the user of the waiting UI job.
	 * @return IStatus
	 * @throws IllegalThreadStateException if this is called from the UIThread
	 * as we do not want to block the UIThread to make a request in the UIThread.
	 * @since 3.0
	 */
	public IStatus requestInUI(UIJob job, String message);

	/**
	 * Set the cursor to busy and run runnable within the UI Thread.
	 * After the cursor has been running for LONG_OPERATION_MILLISECONDS
	 * replace it with a ProgressMonitorDialog so that the user may cancel.
	 * 
	 * @param runnable
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;

}
