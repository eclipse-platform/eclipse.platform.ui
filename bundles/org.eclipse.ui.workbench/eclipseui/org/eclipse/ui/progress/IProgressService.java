/*******************************************************************************
 * Copyright (c) 2003, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * support. It can be obtained from the workbench and then used to show progress
 * for both background operations and operations that run in the UI thread.
 * <p>
 * All methods on the progress service must be called from the UI thread, except
 * {@link #run(boolean, boolean, IRunnableWithProgress)} and
 * {@link #runInUI(IRunnableContext, IRunnableWithProgress, ISchedulingRule)},
 * which can be called from any thread.
 * </p>
 * <p>
 * This service can be acquired from your service locator:
 * </p>
 *
 * <pre>
 * IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
 * </pre>
 * <ul>
 * <li>This service is available globally.</li>
 * </ul>
 * <p>
 * <strong>NOTE</strong> The progress service must be referenced at least once
 * before it will become the progress provider for the {@link IJobManager} in
 * the workbench. This connection is done lazily so that RCP applications will
 * not have to use the {@link IProgressService} as the {@link ProgressProvider}
 * to the jobs framework if they do not wish to reference it.
 * </p>
 *
 * @see org.eclipse.ui.IWorkbench#getProgressService()
 * @see IJobManager#setProgressProvider(org.eclipse.core.runtime.jobs.ProgressProvider)
 * @see org.eclipse.ui.services.IServiceLocator#getService(Class)
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProgressService extends IRunnableContext {

	/**
	 * The time at which an operation becomes considered a long operation. Used to
	 * determine when the busy cursor will be replaced with a progress monitor.
	 *
	 * @return int
	 * @see IProgressService#busyCursorWhile(IRunnableWithProgress)
	 */
	int getLongOperationTime();

	/**
	 * Register the ImageDescriptor to be the icon used for all jobs that belong to
	 * family within the workbench.
	 *
	 * @param icon   ImageDescriptor that will be used when the job is being
	 *               displayed
	 * @param family The family to associate with
	 * @see Job#belongsTo(Object)
	 */
	void registerIconForFamily(ImageDescriptor icon, Object family);

	/**
	 * Runs the given operation in the UI thread using the given runnable context.
	 * The given scheduling rule, if any, will be acquired for the duration of the
	 * operation. If the rule is not available when this method is called, a
	 * progress dialog will be displayed that gives users control over the
	 * background processes that may be blocking the runnable from proceeding.
	 * <p>
	 * This method can act as a wrapper for uses of <code>IRunnableContext</code>
	 * where the <code>fork</code> parameter was <code>false</code>.
	 * <p>
	 * Note: Running long operations in the UI thread is generally not recommended.
	 * This can result in the UI becoming unresponsive for the duration of the
	 * operation. Where possible, <code>busyCursorWhile</code> should be used
	 * instead.
	 * </p>
	 * <p>
	 * Modal dialogs should also be avoided in the runnable as there will already be
	 * a modal progress dialog open when this operation runs.
	 * </p>
	 * <p>
	 * When called from a non-UI thread, the calling thread blocks until the UI
	 * thread has run the operation, so do not call it from a thread the UI thread
	 * is waiting for or while owning a scheduling rule that conflicts with
	 * <code>rule</code>.
	 * </p>
	 *
	 * @see org.eclipse.jface.dialogs.Dialog
	 * @see org.eclipse.swt.SWT#APPLICATION_MODAL
	 *
	 * @param context  The runnable context to run the operation in
	 * @param runnable The operation to run
	 * @param rule     A scheduling rule, or <code>null</code>
	 * @throws InvocationTargetException wraps any exception or error which occurs
	 *                                   while running the runnable
	 * @throws InterruptedException      propagated by the context if the runnable
	 *                                   acknowledges cancelation by throwing this
	 *                                   exception.
	 */
	void runInUI(IRunnableContext context, IRunnableWithProgress runnable, ISchedulingRule rule)
			throws InvocationTargetException, InterruptedException;

	/**
	 * Get the icon that has been registered for a Job by checking if the job
	 * belongs to any of the registered families.
	 *
	 * @param job job to get icon for
	 * @return Icon or <code>null</code> if there isn't one.
	 * @see IProgressService#registerIconForFamily(ImageDescriptor,Object)
	 */
	Image getIconFor(Job job);

	/**
	 * Set the cursor to busy and run the runnable in a non-UI Thread. The calling
	 * thread will be blocked for the duration of the execution of the supplied
	 * runnable.
	 *
	 * After the cursor has been running for <code>getLongOperationTime()</code>
	 * replace it with a ProgressMonitorDialog so that the user may cancel. Do not
	 * open the ProgressMonitorDialog if there is already a modal dialog open.
	 *
	 * @param runnable The runnable to execute and show the progress for.
	 * @see IProgressService#getLongOperationTime
	 * @throws InvocationTargetException if executed operation failed
	 * @throws InterruptedException if operation is interrupted
	 */
	void busyCursorWhile(IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;

	/**
	 * Runs the given runnable like
	 * {@link IRunnableContext#run(boolean, boolean, IRunnableWithProgress)}. The
	 * calling thread always blocks until the runnable has finished.
	 * <ul>
	 * <li>From the UI thread, a progress dialog may open once the runnable runs
	 * longer than {@link #getLongOperationTime()}, provided the UI thread can
	 * dispatch events (e.g. with <code>fork</code> <code>true</code>), unless the
	 * user selected "Always run in background".</li>
	 * <li>From a thread that is not an SWT user-interface thread (i.e.
	 * {@link org.eclipse.swt.widgets.Display#getCurrent()} returns
	 * <code>null</code>), no progress dialog opens and the user cannot cancel the
	 * runnable. With <code>fork</code> <code>false</code> the runnable runs in the
	 * calling thread, with <code>fork</code> <code>true</code> in a separate
	 * thread. Scheduling rules and locks are bound to a thread, so the forked
	 * thread does not own those of the caller. A rule acquired with
	 * {@link org.eclipse.core.runtime.jobs.IJobManager#beginRule} can be moved with
	 * {@link org.eclipse.core.runtime.jobs.IJobManager#transferRule(org.eclipse.core.runtime.jobs.ISchedulingRule, Thread)}
	 * in {@link org.eclipse.jface.operation.IThreadListener#threadChange(Thread)}:
	 * to the forked thread in the first notification and back to the calling thread
	 * in the second one. The rule of the running Job itself cannot be moved, so
	 * such callers must pass <code>fork</code> <code>false</code>. The runnable is
	 * not forked again when the calling thread is a modal context thread (see
	 * {@link org.eclipse.jface.operation.ModalContext}) or a thread forked by a
	 * progress service.</li>
	 * </ul>
	 *
	 * @since 3.2
	 */
	@Override
	void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException;

	/**
	 * Open a dialog on job when it starts to run and close it when the job is
	 * finished. Wait for ProgressManagerUtil#SHORT_OPERATION_TIME before opening
	 * the dialog. Do not open if it is already done or if the user has set a
	 * preference to always run in the background.
	 *
	 * Parent the dialog from the shell.
	 *
	 * @param shell The Shell to parent the dialog from or <code>null</code> if the
	 *              active shell is to be used.
	 * @param job   The Job that will be reported in the dialog. job must not be
	 *              <code>null</code>.
	 */
	void showInDialog(Shell shell, Job job);

}
