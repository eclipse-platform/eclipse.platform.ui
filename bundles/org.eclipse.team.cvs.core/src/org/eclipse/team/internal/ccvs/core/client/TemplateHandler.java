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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TemplateHandler extends ResponseHandler {

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.ResponseHandler#getResponseID()
	 */
	public String getResponseID() {
		return "Template"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.ResponseHandler#handle(org.eclipse.team.internal.ccvs.core.client.Session, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void handle(Session session, String localDir, IProgressMonitor monitor) throws CVSException {
		String remoteDir = session.readLine();
		ICVSFolder localFolder = getExistingFolder(session, localDir);
		IContainer container = (IContainer)localFolder.getIResource();
		if (container == null) return;
		ICVSFile templateFile = CVSWorkspaceRoot.getCVSFileFor(SyncFileWriter.getTemplateFile(container));
		session.receiveFile(templateFile, false, UpdatedHandler.HANDLE_UPDATED, monitor);
	}

}
