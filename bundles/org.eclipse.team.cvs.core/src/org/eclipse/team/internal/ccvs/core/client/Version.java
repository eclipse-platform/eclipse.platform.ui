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
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

/**
 * Here are some of the output formats we know about:
 * 
 * Concurrent Versions System (CVS) 1.11.1p1 (client/server)
 * Concurrent Versions System (CVS) NT 1.11.1.1 (Build 27)
 * Concurrent Versions System (CVSNT) 1.11.1.3  (Build 57a) (client/server)
 */
public class Version extends AbstractMessageCommand {

	private static final String CVS_NT_PREFIX_1 = "Concurrent Versions System (CVS) NT "; //$NON-NLS-1$
	private static final String CVS_NT_PREFIX_2 = "Concurrent Versions System (CVSNT) "; //$NON-NLS-1$
	private static final String CVS_PREFIX = "Concurrent Versions System (CVS) "; //$NON-NLS-1$
	/**
	 * @see Request#getRequestId()
	 */
	protected String getRequestId() {
		return "version"; //$NON-NLS-1$
	}

	public IStatus execute(Session session, final ICVSRepositoryLocation location, IProgressMonitor monitor) throws CVSException {
		
		ICommandOutputListener listener = new ICommandOutputListener() {
			public IStatus messageLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				String knownPrefix = null;
				boolean isCVSNT = false;
				if (line.startsWith(CVS_NT_PREFIX_1)) {
					isCVSNT = true;
					knownPrefix = CVS_NT_PREFIX_1;
				} else if (line.startsWith(CVS_NT_PREFIX_2)) {
					isCVSNT = true;
					knownPrefix = CVS_NT_PREFIX_2;
				} else if (line.startsWith(CVS_PREFIX)) {
					knownPrefix = CVS_PREFIX;
				}
				IStatus status = OK;
				if (knownPrefix != null) {
					String versionNumber = line.substring(knownPrefix.length(), line.indexOf(' ', knownPrefix.length() + 1));
					if (versionNumber.startsWith("1.10") || versionNumber.equals("1.11") || versionNumber.equals("1.11.1")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						status = new CVSStatus(IStatus.ERROR, CVSStatus.UNSUPPORTED_SERVER_VERSION, Policy.bind("Version.unsupportedVersion", location.getHost(), versionNumber));//$NON-NLS-1$
					} else if (isCVSNT) {
						status = new CVSStatus(IStatus.WARNING, CVSStatus.SERVER_IS_CVSNT, Policy.bind("Version.unsupportedCVSNT", location.getHost(), versionNumber));//$NON-NLS-1$
					}
				} else {
					status = new CVSStatus(IStatus.INFO, CVSStatus.SERVER_IS_UNKNOWN, Policy.bind("Version.unknownVersionFormat", location.getHost(), line));//$NON-NLS-1$
				}
				((CVSRepositoryLocation)location).setServerPlaform(status);
				return status;
			}
			public IStatus errorLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				return new CVSStatus(IStatus.ERROR, CVSStatus.ERROR_LINE, line);
			}
		};
		
		if (session == null) {
			return execute(NO_GLOBAL_OPTIONS, NO_LOCAL_OPTIONS, new ICVSResource[] {}, listener, monitor);
		} else {
			return execute(session, NO_GLOBAL_OPTIONS, NO_LOCAL_OPTIONS, new String[] {}, listener, monitor);
		}
	}
}
