/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ccvs.core.*;

/**
 * Handles a "Removed" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Removed ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * It removes the file from both the entries of the parent-folder
 * and from the local filesystem.
 */
class RemovedHandler extends ResponseHandler {
	public String getResponseID() {
		return "Removed"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir, IProgressMonitor monitor) throws CVSException {
		
		// read additional data for the response
		String repositoryFile = session.readLine();

		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);
		
		if ( ! mFile.isManaged()) {
			throw new CVSException(Policy.bind("RemovedHandler.invalid", new Path(null, localDir).append(fileName).toString())); //$NON-NLS-1$
		}
		
		// delete then unmanage the file
		try {
            if (mFile.isReadOnly()) mFile.setReadOnly(false);
	        mFile.delete();
	        mFile.unmanage(null);
        } catch (CVSException e) {
            session.handleResponseError(new CVSStatus(IStatus.ERROR, CVSStatus.RESPONSE_HANDLING_FAILURE, Policy.bind("RemovedHandler.0", getPath(mFile)), e)); //$NON-NLS-1$
        }
	}

    private String getPath(ICVSFile file) {
        try {
            IResource resource = file.getIResource();
            if (resource != null) {
                return resource.getFullPath().toString();
            }
        } catch (CVSException e) {
            // Ignore
        }
        try {
            return file.getRepositoryRelativePath();
        } catch (CVSException e1) {
            return file.getName();
        }
    }
}

