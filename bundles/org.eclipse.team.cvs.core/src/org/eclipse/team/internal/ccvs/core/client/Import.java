package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;

public class Import extends Command {
	/*** Local options: specific to import ***/
	public static LocalOption makeBinaryWrapperOption(String pattern) {
		return new LocalOption("-W", pattern + " -k 'b'");
	}

	protected Import() { }
	protected String getCommandId() {
		return "import";
	}

	protected ICVSResource[] computeWorkResources(Session session, String[] arguments, LocalOption[] localOptions)
		throws CVSException {
		if (arguments.length < 3) throw new IllegalArgumentException();
		return new ICVSResource[0];
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {
					
	 	// If the branch option is not provided, a default value of 1.1.1 is used.
	 	// This is done to maintain reference cliet compatibility
	 	if (findOption(localOptions, "-b") == null) {
	 		session.sendArgument("-b");
	 		session.sendArgument("1.1.1");
	 	}
	
		ICVSResourceVisitor visitor = new ImportStructureVisitor(session,
			collectOptionArguments(localOptions, "-W"), monitor);		
		session.getLocalRoot().accept(visitor);
	}

	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
		session.sendDefaultRootDirectory();
	}

}

