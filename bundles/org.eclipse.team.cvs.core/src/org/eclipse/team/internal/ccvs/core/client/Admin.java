package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class Admin extends AbstractMessageCommand {
	/*** Local options: specific to admin ***/

	protected Admin() { }
	protected String getRequestId() {
		return "admin";  //$NON-NLS-1$
	}
}

