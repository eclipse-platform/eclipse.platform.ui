package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.Policy;

public interface ICommandOutputListener {
	
	/*** Status to be returned when no error or warning occured ***/
	public static final IStatus OK = new CVSStatus(CVSStatus.OK,Policy.bind("ok")); //$NON-NLS-1$
	
	/**
	 * Invoked when a message line is received from the server.
	 * <p>
	 * Any status other than ICommandOutputListener.OK will be accumulated
	 * by the command and returned. The severity of the status matches those of
	 * IStatus and must indicate whether this is a warning, error, or informational 
	 * text.while the code should be one of the codes provided by CVSStatus.
	 * The status code must not be CVSStatus.SERVER_ERROR.
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
	 * Any status other than ICommandOutputListener.OK will be accumulated
	 * by the command and returned. The severity of the status matches those of
	 * IStatus and must indicate whether this is a warning, error, or informational 
	 * text.while the code should be one of the codes provided by CVSStatus.
	 * The status code must not be CVSStatus.SERVER_ERROR.
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
