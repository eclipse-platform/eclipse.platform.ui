/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.examples.jobs.actions;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This class is not really sample code. This action is used to gather debugging
 * information about the internal state of the background job scheduling
 * mechanism.
 */
public class DebugJobManagerAction implements IWorkbenchWindowActionDelegate {
	/**
	 * Create a new DebugJobManagerAction.
	 */
	public DebugJobManagerAction() {
		super();
	}
	@Override
	public void dispose() {
		//
	}

	@Override
	public void init(IWorkbenchWindow window) {
		//
	}

	@Override
	public void run(IAction action) {
		System.out.println("**** BEGIN DUMP JOB MANAGER INFORMATION ****"); //$NON-NLS-1$
		Job[] jobs = Job.getJobManager().find(null);
		for (Job job : jobs) {
			System.out.println("" + job.getClass().getName() + " state: " + JobManager.printState(job.getState())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println("**** END DUMP JOB MANAGER INFORMATION ****"); //$NON-NLS-1$
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//
	}
}
