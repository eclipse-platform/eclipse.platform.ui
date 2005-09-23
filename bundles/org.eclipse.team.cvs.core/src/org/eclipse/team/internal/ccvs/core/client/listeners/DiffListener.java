/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.team.internal.ccvs.core.client.Session;

public class DiffListener extends CommandOutputListener {
	PrintStream patchStream;
	boolean wroteToStream;
	
	//Error Messages returned by CVS
	static final String ERR_NOSUCHDIRECTORY = "cvs [diff aborted]: no such directory"; //$NON-NLS-1$
	
	public DiffListener(PrintStream patchStream) {
		this.patchStream = patchStream;
		wroteToStream=false;
	}
	
	public IStatus messageLine(
			String line, 
			ICVSRepositoryLocation location, 
			ICVSFolder commandRoot,
			IProgressMonitor monitor) {
		
		// Special handling to avoid getting duplicate CRs when generating a patch on windows.  
		// If the remote file has CR/LF in it, then the line will have a CR at the end.
        // We need to remove it so we don't end up with two CRs (since the printStream will also add one).
        // On *nix, we want to include the CR since it will not be added by the printStream (see bug 92162).
		if (Session.IS_CRLF_PLATFORM && line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
			line = line.substring(0, line.length() - 1);
		}
		patchStream.println(line);
		wroteToStream=true;
		
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
		
		//Check to see if this is a no such directory message
		if (line.indexOf(ERR_NOSUCHDIRECTORY) != -1){
			return OK;
		}
		return super.errorLine(line, location, commandRoot, monitor);
	}

	public boolean wroteToStream() {
		return wroteToStream;
	}
}
