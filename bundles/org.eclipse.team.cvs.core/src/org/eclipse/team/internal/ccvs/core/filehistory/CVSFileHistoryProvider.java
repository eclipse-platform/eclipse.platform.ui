/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.core.filehistory;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.filehistory.IFileHistory;
import org.eclipse.team.core.filehistory.IFileHistoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

public class CVSFileHistoryProvider implements IFileHistoryProvider {

	/**
	 * see <code>org.eclipse.team.core.IFileHistoryProvider</code>
	 */
	public IFileHistory getFileHistoryFor(IResource resource, IProgressMonitor monitor){
		ICVSRemoteResource remoteResource;
		try {
			monitor.beginTask(null, 100);
			remoteResource = CVSWorkspaceRoot.getRemoteResourceFor(resource);
			monitor.worked(40);
			CVSFileHistory remoteFile = null;
			if (remoteResource instanceof ICVSFile){
				remoteFile = new CVSFileHistory((ICVSFile) remoteResource);
			}
			return remoteFile;
		} catch (CVSException e) {	
		} finally {
			monitor.done();
		}
		
		return null;
	}

}
