package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;


public class Import extends Command {
	/*** Local options: specific to import ***/
	public static LocalOption makeBinaryWrapperOption(String pattern) {
		return new LocalOption("-W", pattern + " -k 'b'"); //$NON-NLS-1$  //$NON-NLS-2$
	}

	protected Import() { }
	protected String getRequestId() {
		return "import"; //$NON-NLS-1$
	}

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
		if (arguments.length < 3) throw new IllegalArgumentException();
		return new ICVSResource[0];
	}
	
	protected IStatus doExecute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, String[] arguments, ICommandOutputListener listener,
		IProgressMonitor monitor) throws CVSException {
			
		// If the branch option is not provided, a default value of 1.1.1 is used.
	 	// This is done to maintain reference client compatibility
	 	if (findOption(localOptions, "-b") == null) { //$NON-NLS-1$
	 		LocalOption[] newLocalOptions = new LocalOption[localOptions.length + 1];
			newLocalOptions[0] = new LocalOption("-b", "1.1.1");  //$NON-NLS-1$ //$NON-NLS-2$
			System.arraycopy(localOptions, 0, newLocalOptions, 1, localOptions.length);
			localOptions = newLocalOptions;
	 	}
		return super.doExecute(session, globalOptions, localOptions, arguments, listener, monitor);
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {
	
		ICVSResourceVisitor visitor = new ImportStructureVisitor(session,
			collectOptionArguments(localOptions, "-W"), monitor);		 //$NON-NLS-1$
		session.getLocalRoot().accept(visitor);
	}

	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
		session.sendConstructedRootDirectory();
	}

}

