package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class Status extends AbstractMessageCommand {
	/*** Local options: specific to status ***/

	protected Status() { }
	protected String getRequestId() {
		return "status"; //$NON-NLS-1$
	}
}
