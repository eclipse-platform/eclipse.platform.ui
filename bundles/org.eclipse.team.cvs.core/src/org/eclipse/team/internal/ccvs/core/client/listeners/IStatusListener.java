package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;

public interface IStatusListener {
	
	public static final String FOLDER_REVISION = "";
	
	/**
	 * Provides access to the revision of a file through the use of the Status command.
	 * 
	 * The provided path is the absoulte remote path of the resource including the repository root directory
	 */
	public void fileStatus(IPath path, String remoteRevision);
}
