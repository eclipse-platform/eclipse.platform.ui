package org.eclipse.team.internal.ccvs.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class NOOPCommand extends Command {

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#sendLocalResourceState(Session, GlobalOption[], LocalOption[], ICVSResource[], IProgressMonitor)
	 */
	protected void sendLocalResourceState(
		Session session,
		GlobalOption[] globalOptions,
		LocalOption[] localOptions,
		ICVSResource[] resources,
		IProgressMonitor monitor)
		throws CVSException {
		
		// The noop visitor will send any pending notifications
		new NOOPVisitor(session, monitor).visit(session, resources);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Request#getRequestId()
	 */
	protected String getRequestId() {
		return "noop"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#sendArguments(Session, String[])
	 */
	protected void sendArguments(Session session, String[] arguments)throws CVSException {
		// don't send any arguments
	}

}
