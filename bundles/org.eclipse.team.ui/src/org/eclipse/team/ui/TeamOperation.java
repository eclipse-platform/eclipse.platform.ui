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
package org.eclipse.team.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.actions.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * An operation that can be configured to run in the foreground using
 * the {@link org.eclipse.ui.progress.IProgressService} or the background
 * as a {@link org.eclipse.core.runtime.jobs.Job}. The execution context is determined
 * by what is returned by the {@link #canRunAsJob()} hint which may be overriden by subclasses. 
 * Subsclass must override the <code>run(IProgressMonitor)</code> method to perform 
 * the behavior of the operation in the desired execution context.
 * <p>
 * If this operation is run as a job, it is registered with the job as a 
 * {@link org.eclipse.core.runtime.jobs.IJobChangeListener} and is scheduled with
 * the part of this operation if it is not <code>null</code>. 
 * Subsclasses can override the methods of this interface to receive job change notifications.
 * </p>
 * @see org.eclipse.ui.progress.IProgressService
 * @see org.eclipse.core.runtime.jobs.Job
 * @see org.eclipse.core.runtime.ISchedulingRule
 * @see org.eclipse.core.runtime.jobs.IJobChangeListener
 * @since 3.0
 */
public abstract class TeamOperation extends JobChangeAdapter implements IRunnableWithProgress {
	
	private IWorkbenchPart part;
	private IRunnableContext context;
	
	/**
	 * Create an team operation associated with the given part.
	 * @param part the part the operation is associated with or <code>null</code> if the
	 * operation is to be run without a part.
	 */
	protected TeamOperation(IWorkbenchPart part) {
		this(part, null);
	}
	
	/**
	 * Create an team operation that will run in the given context.
	 * @param context a runnable context
	 */
	protected TeamOperation(IRunnableContext context) {
		this(null, context);
	}
	
	/**
	 * Create an team operation associated with the given part 
	 * that will run in the given context.
	 * @param part the part the operation is associated with or <code>null</code>
	 * @param context a runnable context
	 */
	protected TeamOperation(IWorkbenchPart part, IRunnableContext context) {
		this.part = part;
		this.context = context;
	}

	/**
	 * Return the part that is associated with this operation.
	 * 
	 * @return Returns the part or <code>null</code>
	 */
	public IWorkbenchPart getPart() {
		return part;
	}
	
	/**
	 * Run the operation in a context that is determined by the {@link #canRunAsJob()}
	 * hint. If this operation can run as a job then it will be run in a background thread.
	 * Otherwise it will run in the foreground and block the caller.
	 */
	public final void run() throws InvocationTargetException, InterruptedException {
		if (shouldRun()) {
			getRunnableContext().run(this);
		}
	}

	/**
	 * This method is invoked from the <code>run()</code> method before
	 * the operation is run in the operation's context. Subclasses may
	 * override in order to perform prechecks to determine if the operation
	 * should run. This may include prompting the user for information, etc.
	 * 
	 * @return whether the operation should be run.
	 */
	protected boolean shouldRun() {
		return true;
	}

	/**
	 * Returns the scheduling rule that is to be obtained before this
	 * operation is executed by it's context or <code>null</code> if
	 * no scheduling rule is to be obtained. If the operation is run 
	 * as a job, the schdulin rule is used as the schduling rule of the
	 * job. Otherwise, it is obtained before execution of the operation
	 * occurs.
	 * <p>
	 * By default, no scheduling
	 * rule is obtained. Sublcasses can override to in order ot obtain a
	 * scheduling rule or can obtain schduling rules withing their operation
	 * if finer grained schduling is desired.
	 * 
	 * @return the schduling rule to be obtained by this operation
	 * or <code>null</code>.
	 */
	protected ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	/**
	 * Return whether the auto-build should be postponed until after
	 * the operation is complete. The default is to postpone the auto-build.
	 * subclas can override.
	 * 
	 * @return whether to postpone the auto-build while the operation is executing.
	 */
	protected boolean isPostponeAutobuild() {
		return true;
	}
	
	/**
	 * If this operation can safely be run in the background, then subclasses can
	 * override this method and return <code>true</code>. This will make their
	 * action run in a {@link  org.eclipse.core.runtime.jobs.Job}. 
	 * Subsclass that override this method should 
	 * also override the <code>getJobName()</code> method.
	 * 
	 * @return <code>true</code> if this action can be run in the background and
	 * <code>false</code> otherwise.
	 */
	protected boolean canRunAsJob() {
		return false;
	}
	
	/**
	 * Return the job name to be used if the action can run as a job. (i.e.
	 * if <code>canRunAsJob()</code> returns <code>true</code>).
	 * 
	 * @return the string to be used as the job name
	 */
	protected String getJobName() {
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * This method is called to allow subclasses to configure an action that could be run to show
	 * the results of the action to the user. Default is to return null.
	 * 
	 * @return an action that could be run to see the results of this operation
	 */
	protected IAction getGotoAction() {
		return null;
	}
	
	/**
	 * This method is called to allow subclasses to configure an icon to show when running this
	 * operation.
	 * 
	 * @return an URL to an icon
	 */
	protected URL getOperationIcon() {
		return null;
	}
	
	/**
	 * This method is called to allow subclasses to have the operation remain in the progress
	 * indicator even after the job is done.
	 * 
	 * @return <code>true</code> to keep the operation and <code>false</code> otherwise.
	 */
	protected boolean getKeepOperation() {
		return false;
	}
	
	/**
	 * Return a shell that can be used by the operation to display dialogs, etc.
	 * 
	 * @return a shell
	 */
	protected Shell getShell() {
		final Shell[] shell = new Shell[] { null };
		if (canRunAsJob()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					shell[0] = Utils.getShell(getSite());
				}
			});
		} else {
			shell[0] = Utils.getShell(getSite());
		}
		return shell[0];
	}
	
	/*
	 * Uses the {@link #canRunAsJob()} hint to return a {@link ITeamRunnableContext}
	 * that is used to execute the <code>run(SyncInfoSet, IProgressMonitor)</code>
	 * method of this action. 
	 * 
	 * @param syncSet the sync info set containing the selected elements for which this
	 * action is enabled.
	 * @return the runnable context in which to run this action.
	 */
	private ITeamRunnableContext getRunnableContext() {
		if (context == null && canRunAsJob()) {
			JobRunnableContext context = new JobRunnableContext(getJobName(), getOperationIcon(), getGotoAction(), getKeepOperation(), this, getSite());
			context.setPostponeBuild(isPostponeAutobuild());
			context.setSchedulingRule(getSchedulingRule());
			return context;
		} else {
			ProgressDialogRunnableContext context = new ProgressDialogRunnableContext(getShell());
			context.setPostponeBuild(isPostponeAutobuild());
			context.setSchedulingRule(getSchedulingRule());
			if (this.context != null) {
				context.setRunnableContext(this.context);
			}
			return context;
		}
	}
	
	private IWorkbenchSite getSite() {
		IWorkbenchSite site = null;
		if(part != null) {
			site = part.getSite();
		}
		return site;
	}
}
