package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class Admin extends AbstractMessageCommand {
	/*** Local options: specific to admin ***/

	protected Admin() { }
	protected String getCommandId() {
		return "admin";
	}
}

