package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class NullCopyHandler extends CopyHandler {
	/*
	 * @see ResponseHandler#handle(Session, String, IProgressMonitor)
	 */
	public void handle(Session session, String argument, IProgressMonitor monitor)
		throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();
		String newFile = session.readLine();
	}
}
