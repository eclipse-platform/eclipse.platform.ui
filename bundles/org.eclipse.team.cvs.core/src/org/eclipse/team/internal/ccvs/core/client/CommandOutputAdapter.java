/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
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
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;

public class CommandOutputAdapter implements ICommandOutputListener {
	public IStatus messageLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot, IProgressMonitor monitor) {
		return OK;
	}
	public IStatus errorLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot, IProgressMonitor monitor) {
		return new CVSStatus(CVSStatus.ERROR, CVSStatus.ERROR_LINE, commandRoot, line);
	}
}
