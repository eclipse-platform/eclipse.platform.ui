package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSDiffException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

class Diff extends AbstractMessageCommand {

	/**
	 * Constructor for Diff.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Diff(ResponseDispatcher responseDispathcer, RequestSender requestSender) {
		super(responseDispathcer, requestSender);
	}
	
	/**
	 * Overwritten to throw the CVSDiffException if the server returns
	 * an error, because it just does so when there is a difference between 
	 * the cecked files.	
	 */
	public void execute (
		String[] globalOptions, 
		String[] localOptions, 
		String[] arguments, 
		ICVSFolder mRoot,
		IProgressMonitor monitor, 
		PrintStream messageOut) 
			throws CVSException {

		try {	
			super.execute(globalOptions,localOptions,arguments,mRoot,monitor,messageOut);
		} catch (CVSServerException e) {
			throw new CVSDiffException();
		}
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.DIFF;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.DIFF;
	}

}

