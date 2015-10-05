/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Performs periodic saving (snapshot) of the workspace.
 */
public class DelayedSnapshotJob extends Job {

	private static final String MSG_SNAPSHOT = Messages.resources_snapshot;
	private SaveManager saveManager;

	public DelayedSnapshotJob(SaveManager manager) {
		super(MSG_SNAPSHOT);
		this.saveManager = manager;
		setRule(ResourcesPlugin.getWorkspace().getRoot());
		setSystem(true);
	}

	/*
	 * @see Job#run()
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;

		try {
			ResourcesPlugin.getWorkspace();
		} catch (IllegalStateException e) {
			// workspace is null, log it as warning only and return OK_STATUS
			Policy.log(IStatus.WARNING, null, e);
			return Status.OK_STATUS;
		}

		try {
			return saveManager.save(ISaveContext.SNAPSHOT, null, Policy.monitorFor(null));
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			saveManager.operationCount = 0;
			saveManager.snapshotRequested = false;
		}
	}
}
