package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

public class Admin extends AbstractMessageCommand {

	/**
	 * Constructor for Admin.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Admin(
		ResponseDispatcher responseContainer,
		RequestSender requestSender) {
		super(responseContainer, requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.ADMIN;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.ADMIN;
	}

}

