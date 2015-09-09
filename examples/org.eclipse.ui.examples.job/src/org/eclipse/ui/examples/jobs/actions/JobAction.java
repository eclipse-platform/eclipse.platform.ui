/*******************************************************************************
 * Copyright (c) 2004,2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.actions;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class JobAction implements IWorkbenchWindowActionDelegate {
	@Override
	public void run(IAction action) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		Job job = new WorkspaceJob("Background job") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("Doing something in background", 100); //$NON-NLS-1$
				for (int i = 0; i < 100; i++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(workspace.getRoot());
		job.schedule();
	}
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}
	@Override
	public void dispose() {
		//do nothing
	}
	@Override
	public void init(IWorkbenchWindow window) {
		//do nothing
	}
}
