/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * An operation which potentially makes changes to the workspace. All resource
 * modification should be performed using this operation. The primary
 * consequence of using this operation is that events which typically occur as a
 * result of workspace changes (such as the firing of resource deltas,
 * performance of autobuilds, etc.) are generally deferred until the outermost operation
 * has successfully completed.  The platform may still decide to broadcast
 * periodic resource change notifications during the scope of the operation
 * if the operation runs for a long time or another thread modifies the workspace
 * concurrently.
 * <p>
 * If a scheduling rule is provided, the operation will obtain that scheduling
 * rule for the duration of its <code>execute</code> method.  If no scheduling
 * rule is provided, the operation will obtain a scheduling rule that locks
 * the entire workspace for the duration of the operation.
 * </p>
 * <p>
 * Subclasses must implement <code>execute</code> to do the work of the
 * operation.
 * </p>
 * @see ISchedulingRule
 * @see org.eclipse.core.resources.IWorkspace#run(ICoreRunnable, IProgressMonitor)
 *  */
public abstract class WorkspaceModifyOperation implements IRunnableWithProgress, IThreadListener {
	private ISchedulingRule rule;

	/**
	 * Creates a new operation.
	 */
	protected WorkspaceModifyOperation() {
		this(IDEWorkbenchPlugin.getPluginWorkspace().getRoot());
	}

	/**
	 * Creates a new operation that will run using the provided
	 * scheduling rule.
	 * @param rule  The ISchedulingRule to use or <code>null</code>.
	 * @since 3.0
	 */
	protected WorkspaceModifyOperation(ISchedulingRule rule) {
		this.rule = rule;
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
	protected abstract void execute(IProgressMonitor monitor)
			throws CoreException, InvocationTargetException,
			InterruptedException;

	/**
	 * The <code>WorkspaceModifyOperation</code> implementation of this
	 * <code>IRunnableWithProgress</code> method initiates a batch of changes by
	 * invoking the <code>execute</code> method as a workspace runnable
	 * (<code>IWorkspaceRunnable</code>).
	 */
	@Override
	public synchronized final void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		AtomicReference<InvocationTargetException> rethrownInvocationTargetException = new AtomicReference<>();
		AtomicReference<InterruptedException> rethrownInterruptedException = new AtomicReference<>();
		try {
			IWorkspaceRunnable workspaceRunnable = pm -> {
				try {
					execute(pm);
				} catch (InvocationTargetException e1) {
					rethrownInvocationTargetException.set(e1);
				} catch (InterruptedException e2) {
					rethrownInterruptedException.set(e2);
				}
				// CoreException and unchecked exceptions (e.g. OperationCanceledException) are
				// propagated to the outer catch
			};
			// if we are in the UI thread, make sure we use progress monitor
			// that spins event loop to allow processing of pending asyncExecs
			if (monitor != null && PlatformUI.isWorkbenchRunning()
					&& !PlatformUI.getWorkbench().isStarting()) {
				Display display = PlatformUI.getWorkbench().getDisplay();
				if (!display.isDisposed()
						&& display.getThread() == Thread.currentThread()) {
					monitor = new EventLoopProgressMonitor(monitor);
				}
			}
			IDEWorkbenchPlugin.getPluginWorkspace().run(workspaceRunnable,
					rule, IResource.NONE, monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (OperationCanceledException e) {
			InterruptedException interruptedException = new InterruptedException(e.getMessage());
			interruptedException.initCause(e);
			throw interruptedException;
		}

		// Re-throw any exceptions caught while running the IWorkspaceRunnable
		if (rethrownInvocationTargetException.get() != null) {
			throw rethrownInvocationTargetException.get();
		}
		if (rethrownInterruptedException.get() != null) {
			throw rethrownInterruptedException.get();
		}
	}
	@Override
	public void threadChange(Thread thread) {
		//we must make sure we aren't transferring control away from a thread that
		//already owns a scheduling rule because this is deadlock prone (bug 105491)
		if (rule == null) {
			return;
		}
		Job currentJob = Job.getJobManager().currentJob();
		if (currentJob == null) {
			return;
		}
		ISchedulingRule currentRule = currentJob.getRule();
		if (currentRule == null) {
			return;
		}
		throw new IllegalStateException("Cannot fork a thread from a thread owning a rule"); //$NON-NLS-1$
	}

	/**
	 * The scheduling rule.  Should not be modified.
	 * @return the scheduling rule, or <code>null</code>.
	 * @since 3.4
	 */
	public ISchedulingRule getRule() {
		return rule;
	}
}
