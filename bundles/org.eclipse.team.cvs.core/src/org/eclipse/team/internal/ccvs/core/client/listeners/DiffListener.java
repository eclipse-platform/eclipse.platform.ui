/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client.listeners;


import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;

public class DiffListener extends CommandOutputListener {
	PrintStream patchStream;
	
	public DiffListener(PrintStream patchStream) {
		this.patchStream = patchStream;
	}
	
	public IStatus messageLine(
			String line, 
			ICVSRepositoryLocation location, 
			ICVSFolder commandRoot,
			IProgressMonitor monitor) {
		
		// Ensure that the line doesn't end with a CR.
		// This can happen if the remote file has CR/LF in it.
		if (line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
			line = line.substring(0, line.length() - 1);
		}
		patchStream.println(line);
		return OK;
	}

	public IStatus errorLine(
			String line, 
			ICVSRepositoryLocation location, 
			ICVSFolder commandRoot,
			IProgressMonitor monitor) {
		// ignore server messages for now - this is used only with the diff
		// request and the errors can be safely ignored.
		if (getServerMessage(line, location) != null) {
			return OK;
		}
		return super.errorLine(line, location, commandRoot, monitor);
	}
}
