package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;

public interface ICommandOutputListener {
	public static final IStatus OK = new Status(IStatus.OK,
		CVSProviderPlugin.ID, 0, Policy.bind("ok"), null);
	
	/**
	 * Invoked when a message line is received from the server.
	 * <p>
	 * The status severity must indicate whether this is a warning,
	 * error, or informational text.  The status code must not be
	 * CVSException.SERVER_ERROR.
	 * </p>
	 * 
	 * @param line the line of message text sent by the server
	 * @param commandRoot the root directory of the command
	 * @param monitor the progress monitor
	 * @return a status indicating success or failure based on the text
	 */
	public IStatus messageLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor);

	/**
	 * Invoked when an error line is received from the server.
	 * <p>
	 * The status severity must indicate whether this is a warning,
	 * error, or informational text.  The status code must not be
	 * CVSException.SERVER_ERROR.
	 * </p>
	 * 
	 * @param line the line of error text sent by the server
	 * @param commandRoot the root directory of the command
	 * @param monitor the progress monitor
	 * @return a status indicating success or failure based on the text
	 */
	public IStatus errorLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor);
}
