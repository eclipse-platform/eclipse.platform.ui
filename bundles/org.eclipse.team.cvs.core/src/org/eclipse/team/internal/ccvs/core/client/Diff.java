package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;

/**
 * Runs the CVS diff command.
 */
public class Diff extends Command {
	/*** Local options: specific to diff ***/
	public static final LocalOption UNIFIED_FORMAT = new LocalOption("-u"); //$NON-NLS-1$
	public static final LocalOption CONTEXT_FORMAT = new LocalOption("-c"); //$NON-NLS-1$
	public static final LocalOption INCLUDE_NEWFILES = new LocalOption("-N"); //$NON-NLS-1$

	protected Diff() { }
	protected String getRequestId() {
		return "diff"; //$NON-NLS-1$
	}
	
	/**
	 * Overwritten to throw the CVSDiffException if the server returns an error, because it just does 
	 * so when there is a difference between the checked files.	
	 */
	protected IStatus doExecute(Session session, GlobalOption[] globalOptions, LocalOption[] localOptions,
								  String[] arguments, ICommandOutputListener listener, IProgressMonitor monitor) throws CVSException {
		try {
			return super.doExecute(session, globalOptions, localOptions, arguments, listener, monitor);
		} catch (CVSServerException e) {
			if (e.containsErrors()) throw e;
			return e.getStatus();
		}
	}

	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		checkResourcesManaged(resources);
		DiffStructureVisitor visitor = new DiffStructureVisitor(session, monitor);
		visitor.visit(session, resources);
	}
}