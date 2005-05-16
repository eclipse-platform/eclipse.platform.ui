/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.actions;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This class is not really sample code.  This action is used to gather debugging
 * information about the internal state of the background job scheduling 
 * mechanism.
 */
public class DebugJobManagerAction implements IWorkbenchWindowActionDelegate {
	public DebugJobManagerAction() {
		super();
	}
	public void dispose() {
		//
	}

	public void init(IWorkbenchWindow window) {
		//
	}

	public void run(IAction action) {
		System.out.println("**** BEGIN DUMP JOB MANAGER INFORMATION ****"); //$NON-NLS-1$
		Job[] jobs = Platform.getJobManager().find(null);
		for (int i = 0; i < jobs.length; i++) {
			System.out.println("" + jobs[i].getClass().getName() + " state: " + JobManager.printState(jobs[i].getState())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println("**** END DUMP JOB MANAGER INFORMATION ****"); //$NON-NLS-1$
	}

	public void selectionChanged(IAction action, ISelection selection) {
		//
	}
}
