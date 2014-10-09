/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Batches the activity of a job as a single operation, without obtaining the workspace
 * lock.
 */
public abstract class InternalWorkspaceJob extends Job {
	private Workspace workspace;

	public InternalWorkspaceJob(String name) {
		super(name);
		this.workspace = (Workspace) ResourcesPlugin.getWorkspace();
	}

	@Override
	public final IStatus run(IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			int depth = -1;
			try {
				workspace.prepareOperation(null, monitor);
				workspace.beginOperation(true);
				depth = workspace.getWorkManager().beginUnprotected();
				return runInWorkspace(monitor);
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				return Status.CANCEL_STATUS;
			} finally {
				if (depth >= 0)
					workspace.getWorkManager().endUnprotected(depth);
				workspace.endOperation(null, false, monitor);
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	protected abstract IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException;
}
