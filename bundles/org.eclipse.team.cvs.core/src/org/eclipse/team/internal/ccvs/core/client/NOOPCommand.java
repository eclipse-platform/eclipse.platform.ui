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
package org.eclipse.team.internal.ccvs.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class NOOPCommand extends Command {

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#sendLocalResourceState(Session, GlobalOption[], LocalOption[], ICVSResource[], IProgressMonitor)
	 */
	protected ICVSResource[] sendLocalResourceState(
		Session session,
		GlobalOption[] globalOptions,
		LocalOption[] localOptions,
		ICVSResource[] resources,
		IProgressMonitor monitor)
		throws CVSException {
		
		// The noop visitor will send any pending notifications
		new NOOPVisitor(session).visit(session, resources, monitor);
		return resources;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Request#getRequestId()
	 */
	protected String getRequestId() {
		return "noop"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#sendArguments(Session, String[])
	 */
	protected void sendArguments(Session session, String[] arguments)throws CVSException {
		// don't send any arguments
	}

}
