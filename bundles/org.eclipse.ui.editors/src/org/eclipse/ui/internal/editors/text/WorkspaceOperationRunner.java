/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

package org.eclipse.ui.internal.editors.text;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import org.eclipse.ui.texteditor.ISchedulingRuleProvider;

/**
 * @since 3.0
 */
public class WorkspaceOperationRunner implements IRunnableContext {

	private IProgressMonitor fProgressMonitor;

	public WorkspaceOperationRunner() {
	}

	/**
	 * Sets the progress monitor.
	 *
	 * @param progressMonitor the progress monitor to set
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
	}

	/**
	 * Returns the progress monitor. It there is no progress monitor the monitor\
	 * is set to the <code>NullProgressMonitor</code>.
	 *
	 * @return the progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		if (fProgressMonitor == null) {
			fProgressMonitor= new NullProgressMonitor();
		}
		return fProgressMonitor;
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (runnable instanceof ISchedulingRuleProvider) {
			run(runnable, ((ISchedulingRuleProvider)runnable).getSchedulingRule());
		} else {
			run(runnable, ResourcesPlugin.getWorkspace().getRoot());
		}
	}

	/*
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	private void run(IRunnableWithProgress runnable, ISchedulingRule schedulingRule) throws InvocationTargetException, InterruptedException {
		WorkspaceModifyDelegatingOperation operation= new WorkspaceModifyDelegatingOperation(runnable, schedulingRule);
		operation.run(getProgressMonitor());
	}
}
