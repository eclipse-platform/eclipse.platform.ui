/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * The progress service is the primary interface to the workbench progress 
 * support. It can be obtained from the workbench and then used to show 
 * progress for both background operations and operations that run in the UI thread.  It can
 * also be acquired from your service locator: e.g. getSite().getService(IProgressService.class)
 * <p>
 * All methods on the progress service must be called from the UI thread.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <strong>NOTE</strong> The progress service must be referenced at least once 
 * before it will become the progress provider for the {@link IJobManager} in the
 * workbench. This connection is done lazily so that RCP applications will not
 * have to use the {@link IProgressService} as the {@link ProgressProvider} to the
 * jobs framework if they do not wish to reference it.
 * 
 * @see org.eclipse.ui.IWorkbench#getProgressService() 
 * @see IJobManager#setProgressProvider(org.eclipse.core.runtime.jobs.ProgressProvider)
 * 
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
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
     * Runs the given operation in the UI thread using the given runnable context.  
     * The given scheduling rule, if any, will be acquired for the duration of the operation. 
     * If the rule is not available when this method is called, a progress dialog will be 
     * displayed that gives users control over the background processes that may 
     * be blocking the runnable from proceeding.
     * <p>
     * This method can act as a wrapper for uses of <tt>IRunnableContext</tt>
     * where the <tt>fork</tt> parameter was <tt>false</tt>. 
     * <p>
     * Note: Running long operations in the UI thread is generally not 
     * recommended. This can result in the UI becoming unresponsive for
     * the duration of the operation. Where possible, <tt>busyCursorWhile</tt>
     * should be used instead.  
     * </p>
     * <p>
     * Modal dialogs should also be avoided in the runnable as there will already
     * be a modal progress dialog open when this operation runs.
     * </p>
     * @see org.eclipse.jface.dialogs.Dialog
     * @see org.eclipse.swt.SWT#APPLICATION_MODAL
     * 
     * @param context The runnable context to run the operation in
     * @param runnable The operation to run
     * @param rule A scheduling rule, or <code>null</code>
     * @throws InvocationTargetException wraps any exception or error which occurs 
     *  while running the runnable
     * @throws InterruptedException propagated by the context if the runnable 
     *  acknowledges cancelation by throwing this exception.
     *  
     */
    public void runInUI(IRunnableContext context,
            IRunnableWithProgress runnable, ISchedulingRule rule)
            throws InvocationTargetException, InterruptedException;

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
     * Set the cursor to busy and run the runnable in a non-UI Thread.
     * The calling thread will be blocked for the duration of the execution
     * of the supplied runnable.
     * 
     * After the cursor has been running for 
     * <code>getLongOperationTime()</code> replace it with
     * a ProgressMonitorDialog so that the user may cancel.
     * Do not open the ProgressMonitorDialog if there is already a modal
     * dialog open.
     * 
     * @param runnable The runnable to execute and show the progress for.
     * @see IProgressService#getLongOperationTime
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public void busyCursorWhile(IRunnableWithProgress runnable)
            throws InvocationTargetException, InterruptedException;

    /**
     * This specialization of IRunnableContext#run(boolean, boolean,
     * IRunnableWithProgress) might run the runnable asynchronously
     * if <code>fork</code> is <code>true</code>.
     * 
     * @since 3.2
     */
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;
    
    /**
     * Open a dialog on job when it starts to run and close it 
     * when the job is finished. Wait for ProgressManagerUtil#SHORT_OPERATION_TIME
     * before opening the dialog. Do not open if it is already done or
     * if the user has set a preference to always run in the background.
     * 
     * Parent the dialog from the shell.
     * 
     * @param shell The Shell to parent the dialog from or 
     * <code>null</code> if the active shell is to be used.
     * @param job The Job that will be reported in the dialog. job
     * must not be <code>null</code>.
     */
    public void showInDialog(Shell shell, Job job);

}
