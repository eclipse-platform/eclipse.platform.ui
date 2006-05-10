/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client.listeners;
 
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
	public void fileStatus(ICVSFolder commandRoot, String path, String remoteRevision);
}
