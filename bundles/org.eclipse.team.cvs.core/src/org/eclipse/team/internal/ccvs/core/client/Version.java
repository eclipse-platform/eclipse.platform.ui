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

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
/**
 * Here are some of the output formats we know about:
 * 
 * Concurrent Versions System (CVS) 1.11.1p1 (client/server)
 * Concurrent Versions System (CVS) NT 1.11.1.1 (Build 27)
 * Concurrent Versions System (CVSNT) 1.11.1.3  (Build 57a) (client/server)
 */
public class Version extends AbstractMessageCommand {

	private static final String KNOWN_PREFIX = "Concurrent Versions System"; //$NON-NLS-1$
	/**
	 * @see Request#getRequestId()
	 */
	protected String getRequestId() {
		return "version"; //$NON-NLS-1$
	}

	public IStatus execute(Session session, IProgressMonitor monitor) throws CVSException {
		return execute(session, NO_GLOBAL_OPTIONS, NO_LOCAL_OPTIONS, NO_ARGUMENTS, new ICommandOutputListener() {
			public IStatus messageLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				if (line.startsWith(KNOWN_PREFIX)) {
					String rest = line.substring(KNOWN_PREFIX.length() + 1);
				}
				return OK;
			}
			public IStatus errorLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				return new CVSStatus(IStatus.ERROR, CVSStatus.ERROR_LINE, line);
			}
		}, monitor);
	}
}
