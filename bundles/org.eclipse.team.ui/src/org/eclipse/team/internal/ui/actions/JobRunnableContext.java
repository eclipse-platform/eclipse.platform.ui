/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.net.URL;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This runnable context executes it's operation in the context of a background job.
 */
public final class JobRunnableContext implements ITeamRunnableContext {

	private IJobChangeListener listener;
	private IWorkbenchSite site;
	private String jobName;
	private ISchedulingRule schedulingRule;
	private boolean postponeBuild;
	private boolean isUser;
	private URL icon;
	private boolean keep;
	private IAction gotoAction;

	public JobRunnableContext(String jobName) {
		this(jobName, null, null, false, null, null);
	}
	
	public JobRunnableContext(String jobName, URL icon, IAction action, boolean keep, IJobChangeListener listener, IWorkbenchSite site) {
		this.jobName = jobName;
		this.listener = listener;
		this.site = site;
		// By default team actions are user initiated. 
		this.isUser = true;
		this.gotoAction = action;
		this.icon = icon;
		this.keep = keep;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#run(java.lang.String, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(IRunnableWithProgress runnable) {
		Job job;
		if (schedulingRule == null && !postponeBuild) {
			job = getBasicJob(runnable);
		} else {
			job = getWorkspaceJob(runnable);
			if (schedulingRule != null) {
				job.setRule(schedulingRule);
			}
		}
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		job.setUser(isUser());
		configureJob(job);
		Utils.schedule(job, site);
	}
	
	private void configureJob(Job job) {
		if(keep)
			job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE); 
		if(gotoAction != null)
			job.setProperty(IProgressConstants.ACTION_PROPERTY, gotoAction);
		if(icon != null)
			job.setProperty(IProgressConstants.ICON_PROPERTY, icon);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#getShell()
	 */
	public Shell getShell() {
		final Shell[] newShell = new Shell[] { null };
		Display.getDefault().syncExec(
			new Runnable() {
				public void run() {
					newShell[0] = Utils.getShell(site);
				}
			});
		return newShell[0];
	}
	
	/**
	 * Returns whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @return whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 */
	public boolean isPostponeBuild() {
		return postponeBuild;
	}
	
	/**
	 * Set whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @param postponeBuild whether to postpone the auto-build.
	 */
	public void setPostponeBuild(boolean postponeBuild) {
		this.postponeBuild = postponeBuild;
	}
	
	/**
	 * Return the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be onbtained.
	 * @return the schedulingRule to be obtained or <code>null</code>.
	 */
	public ISchedulingRule getSchedulingRule() {
		return schedulingRule;
	}
	
	/**
	 * Return whether this job context is user initiated.
	 * @param boolean <code>true</code> if the job is a result of a user initiated actions
	 * and <code>false</code> otherwise.
	 */
	public boolean isUser() {
		return isUser;
	}
	
	/**
	 * Set wheter the job is user initiated. By default the job created by this runnable context
	 * is a user job.
	 * @param isUser <code>true</code> if the job is a result of a user initiated actions
	 * and <code>false</code> otherwise.
	 */
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	
	/**
	 * Set the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be onbtained.
	 * @param schedulingRule The schedulingRule to be obtained or <code>null</code>.
	 */
	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}
	
	/* private */ IStatus run(IRunnableWithProgress runnable, IProgressMonitor monitor) {
		try {
			runnable.run(monitor);
		} catch (InvocationTargetException e) {
			return TeamException.asTeamException(e).getStatus();
		} catch (InterruptedException e) {
			return Status.OK_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	private Job getBasicJob(final IRunnableWithProgress runnable) {
		return new Job(jobName) {
			public IStatus run(IProgressMonitor monitor) {
				return JobRunnableContext.this.run(runnable, monitor);
			}
		};
	}
	
	private Job getWorkspaceJob(final IRunnableWithProgress runnable) {
		return new WorkspaceJob(jobName) {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				return JobRunnableContext.this.run(runnable, monitor);
			}
		};
	}
}
