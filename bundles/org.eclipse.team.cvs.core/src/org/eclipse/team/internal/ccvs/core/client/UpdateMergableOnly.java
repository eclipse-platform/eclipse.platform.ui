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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This custom update command will only update files that 
 * are either incoming changes (Update-existing) or auto-mergable
 * (Merged with no "+=" in entry line).
 */
public class UpdateMergableOnly extends Update {
	
	List skippedFiles = new ArrayList();
	
	public class MergableOnlyUpdatedHandler extends UpdatedHandler {
		
		public MergableOnlyUpdatedHandler() {
			// handle "Merged" responses
			super(UpdatedHandler.HANDLE_MERGED);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.core.client.UpdatedHandler#getTargetFile(org.eclipse.team.internal.ccvs.core.ICVSFolder, java.lang.String, byte[])
		 */
		protected ICVSFile getTargetFile(ICVSFolder mParent, String fileName, byte[] entryBytes) throws CVSException {
			String adjustedFileName = fileName;
			if (ResourceSyncInfo.isMergedWithConflicts(entryBytes)) {
				// for merged-with-conflict, return a temp file
				adjustedFileName = ".##" + adjustedFileName + " " + ResourceSyncInfo.getRevision(entryBytes); //$NON-NLS-1$ //$NON-NLS-2$
				skippedFiles.add(((IContainer)mParent.getIResource()).getFile(new Path(fileName)));
			}
			return super.getTargetFile(mParent, adjustedFileName, entryBytes);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.core.client.UpdatedHandler#receiveTargetFile(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.ICVSFile, java.lang.String, java.util.Date, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected void receiveTargetFile(
			Session session,
			ICVSFile mFile,
			String entryLine,
			Date modTime,
			boolean binary,
			boolean readOnly,
			IProgressMonitor monitor)
			throws CVSException {
			
			if (ResourceSyncInfo.isMergedWithConflicts(entryLine.getBytes())) {
				// For merged-with-conflict, just recieve the file contents.
				// Use the Updated handler type so that the file will be created or
				// updated.
				session.receiveFile(mFile, binary, UpdatedHandler.HANDLE_UPDATED, monitor);
			} else {
				super.receiveTargetFile(session, mFile, entryLine, modTime, binary, readOnly, monitor);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#doExecute(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption[], org.eclipse.team.internal.ccvs.core.client.Command.LocalOption[], java.lang.String[], org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus doExecute(
		Session session,
		GlobalOption[] globalOptions,
		LocalOption[] localOptions,
		String[] arguments,
		ICommandOutputListener listener,
		IProgressMonitor monitor)
		throws CVSException {
		
		MergableOnlyUpdatedHandler newHandler = new MergableOnlyUpdatedHandler();
		ResponseHandler oldHandler = session.getResponseHandler(newHandler.getResponseID());
		skippedFiles.clear();
		try {
			session.registerResponseHandler(newHandler);
			return super.doExecute(
				session,
				globalOptions,
				localOptions,
				arguments,
				listener,
				monitor);
		} finally {
			session.registerResponseHandler(oldHandler);
		}
	}

	public IFile[] getSkippedFiles() {
		return (IFile[]) skippedFiles.toArray(new IFile[skippedFiles.size()]);
	}
	
}
