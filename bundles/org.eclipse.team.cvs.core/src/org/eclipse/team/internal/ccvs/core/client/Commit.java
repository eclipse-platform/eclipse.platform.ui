package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

public class Commit extends Command {
	/*** Local options: specific to commit ***/
	// Forces a file to be committed even if it has not been modified; implies -l.
	// NOTE: This option is not fully supported -- a file will not be sent
	//       unless it is dirty.  The primary use is to resend a file that may
	//       or may not be changed (e.g. could depend on CR/LF translations, etc...)
	//       and force the server to create a new revision and reply Checked-in.
	public static final LocalOption FORCE = new LocalOption("-f"); //$NON-NLS-1$

	protected Commit() { }
	protected String getRequestId() {
		return "ci"; //$NON-NLS-1$
	}

	/**
	 * Send all files under the workingFolder as changed files to 
	 * the server.
	 */		
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		// Get the folders we want to work on
		checkResourcesManaged(resources);
		
		// Send all changed files to the server	
		ModifiedFileSender visitor = new ModifiedFileSender(session, monitor);
		visitor.visit(session, resources);
		
		// Send the changed files as arguments
		// XXX Is this the way the command line client works?
		ICVSFile[] changedFiles = visitor.getSentFiles();
		for (int i = 0; i < changedFiles.length; i++) {
			session.sendArgument(changedFiles[i].getRelativePath(session.getLocalRoot()));
		}
	}
	
	/**
	 * We do not want to send the arguments here, because we send
	 * them in sendRequestsToServer (special handling).
	 */
	protected void sendArguments(Session session, String[] arguments) throws CVSException {
	}
}	