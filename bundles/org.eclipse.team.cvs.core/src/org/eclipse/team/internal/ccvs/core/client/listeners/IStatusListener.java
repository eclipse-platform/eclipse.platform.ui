package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;

public interface IStatusListener {
	
	public static final String FOLDER_REVISION = "";
	
	/**
	 * provides access to the revision of a file through
	 * the use of the Status command.
	 * 
	 * @see StatusMessageHandler
	 * @see StatusErrorHandler
	 */
	public void fileStatus(IPath path, String remoteRevision);
}
