package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;

public class TagListener extends CommandOutputListener {

	/*
	 * @see ICommandOutputListener#messageLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus messageLine(
		String line,
		ICVSRepositoryLocation location,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		
		// Received a warning in the form:
		// W folder/file : v1 already exists on version 1.2 : NOT MOVING tag to version 1.3
		if( line.charAt(0) == 'W' ) {
			return new CVSStatus(CVSStatus.WARNING, CVSStatus.TAG_ALREADY_EXISTS, commandRoot, line.substring(2));
		}
			
		return OK;
	}

	/*
	 * @see ICommandOutputListener#errorLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus errorLine(
		String line,
		ICVSRepositoryLocation location,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
			
		// Ignore the lines: Tagging folder1/folder2
		String serverMessage = getServerMessage(line, location);
		if ((serverMessage != null) && serverMessage.startsWith("Tagging")) { //$NON-NLS-1$
			return OK;
		}
		String rtagMessage = getServerRTagMessage(line, location);
		if(rtagMessage != null && rtagMessage.startsWith("Tagging") ) { //$NON-NLS-1$
			return OK;
		}
			
		return super.errorLine(line, location, commandRoot, monitor);
	}
	
}
