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
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;

public class ExpandModules extends Command {

	/*
	 * @see Command#getCommandId()
	 */
	protected String getCommandId() {
		return "expand-modules"; //$NON-NLS-1$
	}

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
			
		return new ICVSResource[0];
	}
	
	/*
	 * @see Command#sendLocalResourceState(Session, GlobalOption[], LocalOption[], ICVSResource[], IProgressMonitor)
	 */
	protected void sendLocalResourceState(
		Session session,
		GlobalOption[] globalOptions,
		LocalOption[] localOptions,
		ICVSResource[] resources,
		IProgressMonitor monitor)
		throws CVSException {
		
		// Reset the module expansions before the responses arrive
		session.resetModuleExpansion();
	}
	
	/**
	 * Convenient execute method
	 */
	public IStatus execute(Session session, String[] modules, IProgressMonitor monitor) throws CVSException {
		return execute(session, NO_GLOBAL_OPTIONS, NO_LOCAL_OPTIONS, modules, null, monitor);
	}

}
