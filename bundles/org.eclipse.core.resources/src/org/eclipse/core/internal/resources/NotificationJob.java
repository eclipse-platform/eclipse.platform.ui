/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job for scheduling resource change notifications during operations
 * if sufficient time has elapsed.
 */
class NotificationJob extends Job {
	private static final long NOTIFICATION_DELAY = 1000;
//	private Workspace workspace;
	NotificationJob(Workspace workspace) {
		super(ICoreConstants.MSG_RESOURCES_UPDATING);
//		this.workspace = workspace;
		setSystem(true);
	}
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
//		workspace.notificationRequested = true;
		return Status.OK_STATUS;
	}
	void notifyIfNeeded() {
		schedule(NOTIFICATION_DELAY);
	}
}