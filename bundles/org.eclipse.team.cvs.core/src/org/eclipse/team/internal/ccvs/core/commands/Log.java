package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

class Log extends AbstractMessageCommand {

	/**
	 * Constructor for Log.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Log(ResponseDispatcher responseContainer, RequestSender requestSender) {
		super(responseContainer, requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.LOG;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.LOG;
	}

}

