package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

/**
 * Superclass for commands that do not change the structure on 
 * the local working copy (it can change the content of the files).<br>
 * Most of the subclasses are asking the server for response in 
 * message format (log, status)
 */
abstract class AbstractMessageCommand extends Command {

	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			
		
		// Send all folders that are already managed to the server
		new FileStructureVisitor(session, false, false, monitor).visit(session, resources);
	}

}

