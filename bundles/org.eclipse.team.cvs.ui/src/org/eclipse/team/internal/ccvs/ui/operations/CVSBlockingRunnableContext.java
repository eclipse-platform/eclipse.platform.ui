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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.PlatformUI;

/**
 * This CVS runnable context blocks the UI and can therfore have a shell assigned to
 * it (since the shell won't be closed by the user before the runnable completes.
 */
public class CVSBlockingRunnableContext implements ICVSRunnableContext {

	private Shell shell;
	private IRunnableContext runnableContext;
	
	public CVSBlockingRunnableContext(Shell shell) {
		this.shell = shell;
	}

	/**
	 * Run the given runnable in the context of the receiver. By default, the
	 * progress is provided by the active workbench windows but subclasses may
	 * override this to provide progress in some other way (Progress Monitor or
	 * job).
	 */
	public void run(String title, ISchedulingRule schedulingRule, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		getRunnableContext().run(true /* fork */, true /* cancelable */, wrapRunnable(title, schedulingRule, runnable));
	}

	protected IRunnableContext getRunnableContext() {
		if (runnableContext == null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		return runnableContext;
	}

	/*
	 * Return an IRunnableWithProgress that sets the task name for the progress monitor
	 * and runs in a workspace modify operation if requested.
	 */
	private IRunnableWithProgress wrapRunnable(final String title, final ISchedulingRule schedulingRule, final IRunnableWithProgress runnable) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(title, 100);
				try {
					if (schedulingRule == null) {
						runnable.run(Policy.subMonitorFor(monitor, 100));
					} else {
						final Exception[] exception = new Exception[] { null };
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor pm) throws CoreException {
								try {
									runnable.run(pm);
								} catch (InvocationTargetException e) {
									exception[0] = e;
								} catch (InterruptedException e) {
									exception[0] = e;
								}
							}
						}, schedulingRule, Policy.subMonitorFor(monitor, 100));
						if (exception[0] != null) {
							if (exception[0] instanceof InvocationTargetException) {
								throw (InvocationTargetException)exception[0];
							} else if (exception[0] instanceof InterruptedException) {
								throw (InterruptedException)exception[0];	
							}
						}
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSRunnableContext#getShell()
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * Set the shell to be used by the owner of this context to prompt the user.
	 * @param shell
	 */
	public void setShell(Shell shell) {
		this.shell = shell;
	}

	/**
	 * @param runnableContext
	 */
	public void setRunnableContext(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
	}

}
