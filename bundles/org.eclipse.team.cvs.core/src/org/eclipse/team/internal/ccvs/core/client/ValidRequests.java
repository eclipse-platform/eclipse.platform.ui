package org.eclipse.team.internal.ccvs.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

class ValidRequests extends Command {
	protected ValidRequests() { }
	protected String getCommandId() {
		return "valid-requests"; //$NON-NLS-1$
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			
	}
	
	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
	}
	
	/**
	 * Returns the default global options for all commands. Subclasses can override but
	 * must call this method and return superclasses global options.
	 * 
	 * @param globalOptions are the options already specified by the user.
	 * @return the default global options that will be sent with every command.
	 */
	protected GlobalOption[] getDefaultGlobalOptions(Session session, GlobalOption[] globalOptions, LocalOption[] localOptions) {
		return Command.NO_GLOBAL_OPTIONS;		
	}
}
