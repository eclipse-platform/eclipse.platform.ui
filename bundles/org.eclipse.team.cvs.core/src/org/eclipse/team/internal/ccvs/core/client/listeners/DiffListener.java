package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;

public class DiffListener implements ICommandOutputListener {
	PrintStream patchStream;
	
	public DiffListener(PrintStream patchStream) {
		this.patchStream = patchStream;
	}
	
	public IStatus messageLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		if (! line.startsWith("cvs server:")) { //$NON-NLS-1$
			// Ensure that the line doesn't end with a CR.
			// This can happen if the remote file has CR/LF in it.
			if (line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
				line = line.substring(0, line.length() - 1);
			}
			patchStream.println(line);
		}
		return OK;
	}

	public IStatus errorLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		// ignore these errors for now - this is used only with the diff
		// request and the errors can be safely ignored.
		if(! line.startsWith("cvs server:")) {//$NON-NLS-1$
			return new CVSStatus(CVSStatus.ERROR, CVSStatus.ERROR_LINE, commandRoot, line);
		}
		return OK;
	}
}
