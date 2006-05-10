/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
		for (int i = 0; i < modules.length; ++i) {
			session.sendArgument(modules[i]);
		}
		return executeRequest(session, Command.DEFAULT_OUTPUT_LISTENER, monitor);
	}
}
