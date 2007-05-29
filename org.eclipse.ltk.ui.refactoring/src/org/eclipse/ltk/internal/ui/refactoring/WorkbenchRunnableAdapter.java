/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;

/**
 * An <code>IRunnableWithProgress</code> that adapts an <code>IWorkspaceRunnable</code>
 * so that is can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code> 
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress, IThreadListener {
	
	private IWorkspaceRunnable fWorkspaceRunnable;
	private ISchedulingRule fRule;
	private boolean fTransfer;
	
	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> 
	 * to run with no lock at all.
	 * 
	 * @param runnable the workspace runnable
	 * @param rule the scheduling rule
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
		this(runnable, rule, true);
	}
	
	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> 
	 * to run with no lock at all.
	 * 
	 * @param runnable the workspace runnable
	 * @param rule the scheduling rule
	 * @param transfer <code>true</code> if the rule is to be transfered 
	 *  to the model context thread. Otherwise <code>false</code>
	 *  
	 *  @since 3.1
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule, boolean transfer) {
		Assert.isNotNull(runnable);
		Assert.isNotNull(rule);
		fWorkspaceRunnable= runnable;
		fRule= rule;
		fTransfer= transfer;
	}
	
	public ISchedulingRule getSchedulingRule() {
		return fRule;
	}

	/**
	 * {@inheritDoc}
	 */
	public void threadChange(Thread thread) {
		if (fTransfer)
			Job.getJobManager().transferRule(fRule, thread);
	}

	/*
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			ResourcesPlugin.getWorkspace().run(fWorkspaceRunnable, fRule, IWorkspace.AVOID_UPDATE, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
}

