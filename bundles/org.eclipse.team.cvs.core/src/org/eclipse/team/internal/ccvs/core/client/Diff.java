package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;

/**
 * Runs the CVS diff command.
 */
public class Diff extends Command {
	/*** Local options: specific to diff ***/
	public static final LocalOption UNIFIED_FORMAT = new LocalOption("-u");
	public static final LocalOption CONTEXT_FORMAT = new LocalOption("-c");
	public static final LocalOption INCLUDE_NEWFILES = new LocalOption("-N");

	protected Diff() { }
	protected String getCommandId() {
		return "diff";
	}
	
	/**
	 * Overwritten to throw the CVSDiffException if the server returns an error, because it just does so when there is a 
	 * difference between  the checked files.	
	 */
	public IStatus execute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, String[] arguments, ICommandOutputListener listener,
		IProgressMonitor monitor)
		throws CVSException {
		try {
			return super.execute(session, globalOptions, localOptions, arguments, listener, monitor);
		} catch (CVSServerException e) {
			if (e.containsErrors()) throw e;
			return e.getStatus();
		}
	}

	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		checkResourcesManaged(resources);
		FileStructureVisitor fsVisitor = new DiffStructureVisitor(session, false, false,  monitor);
		for (int i = 0; i < resources.length; i++) {
			resources[i].accept(fsVisitor);
		}
	}
}