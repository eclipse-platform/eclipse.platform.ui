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
package org.eclipse.team.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This runnable context executes it's operation in the context of a background job.
 */
public class JobRunnableContext implements ITeamRunnableContext {

	private IJobChangeListener listener;
	private IWorkbenchSite site;

	public JobRunnableContext() {
		this(null, null);
	}
	
	public JobRunnableContext(IJobChangeListener listener, IWorkbenchSite site) {
		this.listener = listener;
		this.site = site;
	}

	protected IStatus run(IRunnableWithProgress runnable, IProgressMonitor monitor) {
		try {
			runnable.run(monitor);
		} catch (InvocationTargetException e) {
			return TeamException.asTeamException(e).getStatus();
		} catch (InterruptedException e) {
			return Status.OK_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	protected Job getBasicJob(String title, final IRunnableWithProgress runnable) {
		return new Job(title) {
			public IStatus run(IProgressMonitor monitor) {
				return JobRunnableContext.this.run(runnable, monitor);
			}
		};
	}
	
	protected Job getWorkspaceJob(String title, final IRunnableWithProgress runnable) {
		return new WorkspaceJob(title) {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				return JobRunnableContext.this.run(runnable, monitor);
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#run(java.lang.String, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(String title, ISchedulingRule schedulingRule, boolean postponeBuild, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		Job job;
		if (schedulingRule == null && !postponeBuild) {
			job = getBasicJob(title, runnable);
		} else {
			job = getWorkspaceJob(title, runnable);
			if (schedulingRule != null) {
				job.setRule(schedulingRule);
			}
		}
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		Utils.schedule(job, site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#getShell()
	 */
	public Shell getShell() {
		final Shell[] newShell = new Shell[] { null };
		Display.getDefault().syncExec(
			new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						Display display = Display.getDefault();
						newShell[0] = new Shell(display);
					} else {
						newShell[0] = window.getShell();
					}
				}
			});
		return newShell[0];
	}
}
