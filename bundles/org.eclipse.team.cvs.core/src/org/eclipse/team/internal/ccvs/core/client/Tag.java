package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

public class Tag extends Command {
	/*** Local options: specific to tag ***/
	public static final LocalOption CREATE_BRANCH = new LocalOption("-b", null);	 //$NON-NLS-1$	

	protected Tag() { }
	protected String getCommandId() {
		return "tag"; //$NON-NLS-1$
	}

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
		if (arguments.length < 1) throw new IllegalArgumentException();
		String[] allButFirst = new String[arguments.length - 1];
		System.arraycopy(arguments, 1, allButFirst, 0, arguments.length - 1);
		return super.computeWorkResources(session, localOptions, allButFirst);
	}

	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		// Send all folders that are already managed to the server
		sendFileStructure(session, resources, false, false, monitor);
	}
}