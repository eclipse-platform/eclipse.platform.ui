/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation which checks for the existance of the .project file
 * in a remote folder. The operation can be run using the <code>hasMetaFile</code>
 * static method of by executing the operation and then checking <code>metaFileExists</code>
 */
public class HasProjectMetaFileOperation extends CVSOperation {

	private ICVSRemoteFolder remoteFolder;
	private boolean metaFileExists;
	
	public static boolean hasMetaFile(IWorkbenchPart part, ICVSRemoteFolder remoteFolder) throws InvocationTargetException, InterruptedException {
		HasProjectMetaFileOperation op = new HasProjectMetaFileOperation(part, remoteFolder);
		op.run();
		return op.metaFileExists();
	}
	
	public HasProjectMetaFileOperation(IWorkbenchPart part, ICVSRemoteFolder remoteFolder) {
		super(part);
		this.remoteFolder = remoteFolder;
	}
	
	/*
	 * Return true if the provided remote folder contains a valid meta-file 
	 * (i.e. .project file).
	 */
	private boolean hasMetaFile(ICVSRemoteFolder folder, IProgressMonitor monitor) throws CVSException {
		
		// make a copy of the folder so that we will not effect the original folder when we refetch the members
		// TODO: this is a strange thing to need to do. We shold fix this.
		folder = (ICVSRemoteFolder)folder.forTag(remoteFolder.getTag());

		try {
			folder.members(monitor);
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		}
		// Check for the existance of the .project file
		try {
			folder.getFile(".project"); //$NON-NLS-1$
			return true;
		} catch (TeamException e) {
			// We couldn't retrieve the meta file so assume it doesn't exist
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		metaFileExists = hasMetaFile(remoteFolder, monitor);
	}
	
	/**
	 * Return true if the meta file exists remotely. This method should only be invoked
	 * after the operation has been executed;
	 * @return
	 */
	public boolean metaFileExists() {
		return metaFileExists;
	}

	protected String getTaskName() {
		return CVSUIMessages.HasProjectMetaFile_taskName; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#canRunAsJob()
	 */
	public boolean canRunAsJob() {
		// This operation should never be run in the background.
		return false;
	}

}
