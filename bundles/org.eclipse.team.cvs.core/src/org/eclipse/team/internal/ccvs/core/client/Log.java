package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class Log extends AbstractMessageCommand {
	/*** Local options: specific to log ***/

	protected Log() { }
	protected String getRequestId() {
		return "log"; //$NON-NLS-1$
	}
}

