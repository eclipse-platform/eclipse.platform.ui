package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;

public class TagListener implements ICommandOutputListener {

	/*
	 * @see ICommandOutputListener#messageLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus messageLine(
		String line,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		
		// Received a warning in the form:
		// W folder/file : v1 already exists on version 1.2 : NOT MOVING tag to version 1.3
		if( line.charAt(0) == 'W' ) {
			return new CVSStatus(CVSStatus.WARNING, CVSStatus.TAG_ALREADY_EXISTS, line.substring(2));
		}
			
		return OK;
	}

	/*
	 * @see ICommandOutputListener#errorLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus errorLine(
		String line,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
			
		// Ignore the lines: Tagging folder1/folder2
		if( line.startsWith("cvs server: Tagging") ) { //$NON-NLS-1$
			return OK;
		} else if( line.startsWith("cvs rtag: Tagging") ) { //$NON-NLS-1$
			return OK;
		}
			
		return new CVSStatus(CVSStatus.ERROR, CVSStatus.ERROR_LINE, line);
	}

}
