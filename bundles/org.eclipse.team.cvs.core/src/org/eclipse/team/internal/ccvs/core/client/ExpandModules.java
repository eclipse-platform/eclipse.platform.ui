/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class ExpandModules extends Request {
	protected ExpandModules() { }
	protected String getRequestId() {
		return "expand-modules"; //$NON-NLS-1$
	}

	public IStatus execute(Session session, String[] modules, IProgressMonitor monitor) throws CVSException {
		// Reset the module expansions before the responses arrive
		session.resetModuleExpansion();
		return executeRequest(session, null, monitor);
	}
}
