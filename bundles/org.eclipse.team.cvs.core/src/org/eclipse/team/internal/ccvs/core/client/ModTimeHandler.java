package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.ParseException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.util.CVSDateFormatter;

/**
 * Handles a "Mod-time" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Mod-time 18 Oct 2001 20:21:13 -0330\n
 *   [...]
 * </pre>
 * Then we parse and remember the date for use in subsequent
 * file transfer responses such as Updated.
 * </p>
 */
class ModTimeHandler extends ResponseHandler {
	public String getResponseID() {
		return "Mod-time"; //$NON-NLS-1$
	}

	public void handle(Session session, String timeStamp,
		IProgressMonitor monitor) throws CVSException {
		try {
			session.setModTime(CVSDateFormatter.serverStampToDate(timeStamp));
		} catch (ParseException e) {
			throw new CVSException(Policy.bind("ModTimeHandler.invalidFormat", timeStamp), e); //$NON-NLS-1$
		}
	}
}

