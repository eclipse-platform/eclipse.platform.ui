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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * This class wraps a CVSOperation in a workspace modify operation.
 * 
 */
public class CVSWorkspaceModifyOperation extends WorkspaceModifyOperation {

	private CVSOperation operation;
	
	public CVSWorkspaceModifyOperation(CVSOperation operation) {
		super();
		this.operation = operation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		try {
			operation.execute(monitor);
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}

}
