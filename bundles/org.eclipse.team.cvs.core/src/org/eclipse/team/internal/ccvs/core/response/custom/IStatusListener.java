package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.ccvs.core.response.*;

public interface IStatusListener {
	
	public static final String FOLDER_RIVISION = "";
	
	/**
	 * provides access to the revision of a file through
	 * the use of the Status command.
	 * 
	 * @see StatusMessageHandler
	 * @see StatusErrorHandler
	 */
	public void fileStatus(IPath path, String remoteRevision);
}
