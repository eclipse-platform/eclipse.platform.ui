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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * The IProgressManager is an interface to the progress manager provided by the
 * workbench.
 * 
 * @since 3.0
 */
public interface IProgressService extends IRunnableContext {

	/**
	 * The time at which an operation becomes considered a long
	 * operation. Used to determine when the busy cursor will 
	 * be replaced with a progress monitor.
	 * @return int 
	 * @see IProgressService#busyCursorWhile(IRunnableWithProgress)
	 */
	public int getLongOperationTime();
	
	/**
	 * Register the ImageDescriptor to be the icon used for
	 * all jobs that belong to family within the workbench.
	 * @param icon ImageDescriptor that will be used when the job is being displayed
	 * @param family The family to associate with
	 * @see Job#belongsTo(Object)
	 */
	public void registerIconForFamily(ImageDescriptor icon, Object family);
	
	/**
	 * Get the icon that has been registered for a Job by
	 * checking if the job belongs to any of the registered 
	 * families.
	 * @param job
	 * @return Icon or <code>null</code> if there isn't one.
	 * @see IProgressService#registerIconForFamily(ImageDescriptor,Object)
	 */
	public Image getIconFor(Job job);

	/**
	 * Set the cursor to busy and run runnable within the UI Thread. After the
	 * cursor has been running for <code>getLongOperationTime()<code> replace it with
	 * a ProgressMonitorDialog so that the user may cancel.
	 * Do not open the ProgressMonitorDialog if there is already a modal
	 * dialog open.
	 * 
	 * @param runnable
	 * @see IProgressService#getLongOperationTime
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable)
		throws InvocationTargetException, InterruptedException;
	
	/**
	 * Open a dialog on job when it starts to run and close it 
	 * when the job is finished. Wait for LONG_OPERATION_MILLISECONDS
	 * before opening the dialog. Do not open if it is already done.
	 * 
	 * Parent the dialog from the shell.
	 * 
	 * @param job The Job that will be reported in the dialog.
	 * @param shell The Shell to parent the dialog from or 
	 * <code>null</code> if the active shell is to be used.
	 */
	public void showInDialog(Shell shell, Job job);

}
