package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;
import org.eclipse.team.internal.ccvs.core.resources.*;


/**
 * This handler is used by the RemoteResource hierarchy to retrieve M messages
 * from the CVS server in order to determine the files contained in a parent folder.
 */
public class UpdateMessageHandler extends ResponseHandler {

	/** The response key */
	public static final String NAME = "M";

	IUpdateMessageListener updateMessageListener;

	public UpdateMessageHandler(IUpdateMessageListener updateMessageListener) {
		this.updateMessageListener = updateMessageListener;
	}
	/**
	 * @see ResponseHandler#getName()
	 */
	public String getName() {
		return NAME;
	}
	/**
	 * Handle the response. This response sends the message to the
	 * progress monitor using <code>IProgressMonitor.subTask(Strin)
	 * </code>.
	 */
	public void handle(Connection connection, 
						PrintStream messageOutput,
						ICVSFolder mRoot,
						IProgressMonitor monitor) throws CVSException {
		String line = connection.readLine();
		if (updateMessageListener == null)
			return;
		String path = line.substring(2);
		updateMessageListener.fileInformation(line.charAt(0), path);

	}
}