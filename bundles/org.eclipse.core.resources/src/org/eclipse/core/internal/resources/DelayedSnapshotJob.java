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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class DelayedSnapshotJob extends Job {

	private static final String MSG_SNAPSHOT = Policy.bind("resources.snapshot"); //$NON-NLS-1$
	private SaveManager saveManager;

	public DelayedSnapshotJob(SaveManager manager) {
		super(MSG_SNAPSHOT);
		this.saveManager = manager;
	}
	/*
	 * @see Job#run()
	 */
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!monitor.isCanceled())
					saveManager.requestSnapshot();
			}
		};
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			//do null check in case workspace shuts down concurrently
			if (workspace != null)
				workspace.run(runnable, monitor);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return Status.OK_STATUS;
	}
}