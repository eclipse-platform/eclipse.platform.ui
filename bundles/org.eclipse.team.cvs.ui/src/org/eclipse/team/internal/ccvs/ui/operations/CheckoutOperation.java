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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;

public abstract class CheckoutOperation extends RemoteOperation {

	public CheckoutOperation(Shell shell, ICVSRemoteFolder[] remoteFolders) {
		super(shell, remoteFolders);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		String taskName = getTaskName();
		monitor.beginTask(taskName, 100);
		checkout(getRemoteFolders(), Policy.subMonitorFor(monitor, 100));
	}

	protected ICVSRemoteFolder[] getRemoteFolders() {
		return (ICVSRemoteFolder[])getRemoteResources();
	}
	
	/**
	 * Checkout the selected remote folders in a form appropriate for the operation subclass.
	 * @param folders
	 * @param monitor
	 */
	protected abstract void checkout(ICVSRemoteFolder[] folders, IProgressMonitor monitor)  throws CVSException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#canRunAsJob()
	 */
	public boolean canRunAsJob() {
		return true;
	}
}
