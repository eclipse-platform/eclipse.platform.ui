/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client.listeners;
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;

public interface IStatusListener {
	
	public static final String FOLDER_REVISION = ""; //$NON-NLS-1$
	
	/**
	 * Provides access to the revision of a file through the use of the Status command.
	 * 
	 * @param commandRoot the root directory of the command
	 * @param path the absolute remote path of the resource including the repository root directory
	 * @param remoteRevision the remote revision of the file
	 */
	public void fileStatus(ICVSFolder commandRoot, IPath path, String remoteRevision);
}
