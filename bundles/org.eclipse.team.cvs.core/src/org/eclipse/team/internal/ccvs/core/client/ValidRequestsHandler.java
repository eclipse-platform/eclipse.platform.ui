package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Session;

/**
 * Handles a "Valid-requests" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Valid-requests ci co update Root Directory Valid-responses Argument ...\n
 *   [...]
 * </pre>
 * Then we remember the set of valid requests for this session in
 * preparation for isValidRequests queries.
 * </p>
 */
class ValidRequestsHandler extends ResponseHandler {
	public String getResponseID() {
		return "Valid-requests";
	}

	public void handle(Session session, String validRequests,
		IProgressMonitor monitor) throws CVSException {
		// remember the set of valid requests for this session
		session.setValidRequests(validRequests);
	}

}
