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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;

public abstract class SingleCommandOperation extends RepositoryProviderOperation {
	
	LocalOption[] options = Command.NO_LOCAL_OPTIONS;
	
	public SingleCommandOperation(Shell shell, IResource[] resources, LocalOption[] options) {
		super(shell, resources);
		if (options != null) {
			this.options = options;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#execute(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException {
		monitor.beginTask(null, 100);
		Session session = new Session(getRemoteLocation(provider), getLocalRoot(provider), true /* output to console */);
		session.open(Policy.subMonitorFor(monitor, 10), isServerModificationOperation());
		try {
			IStatus status = executeCommand(session, provider, resources, Policy.subMonitorFor(monitor, 90));
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				addError(status);
			}
		} finally {
			session.close();
		}
	}

	/**
	 * Indicate whether the operation requires write access to the server (i.e.
	 * the operation changes state on the server whether it be to commit, tag, admin, etc).
	 * @return
	 */
	protected boolean isServerModificationOperation() {
		return false;
	}

	/**
	 * Method overridden by subclasses to issue the command to the CVS repository using the given session.
	 */
	protected abstract IStatus executeCommand(Session session, CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException;

	protected LocalOption[] getLocalOptions() {
		return options;
	}

	protected void setLocalOptions(LocalOption[] options) {
		this.options = options;
	}

}
