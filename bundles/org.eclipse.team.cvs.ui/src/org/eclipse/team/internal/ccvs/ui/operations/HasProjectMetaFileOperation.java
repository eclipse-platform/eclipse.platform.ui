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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;

/**
 * Operation which checks for the existance of the .project file (or .vcm_meta file) 
 * in a remote folder. The operation can be run using the <code>hasMetaFile</code>
 * static method of by executing the operation and then checking <code>metaFileExists</code>
 */
public class HasProjectMetaFileOperation extends CVSOperation {

	private ICVSRemoteFolder remoteFolder;
	private boolean metaFileExists;
	
	public static boolean hasMetaFile(Shell shell, ICVSRemoteFolder remoteFolder, IRunnableContext runnableContext) throws CVSException, InterruptedException {
		HasProjectMetaFileOperation op = new HasProjectMetaFileOperation(shell, remoteFolder);
		if (runnableContext != null) {
			op.setRunnableContext(runnableContext);
		}
		op.execute();
		return op.metaFileExists();
	}
	
	public HasProjectMetaFileOperation(Shell shell, ICVSRemoteFolder remoteFolder) {
		super(shell);
		this.remoteFolder = remoteFolder;
	}
	
	/*
	 * Return true if the provided remote folder contains a valid meta-file 
	 * (i.e. .project or the older .vcm_meta file).
	 */
	private boolean hasMetaFile(ICVSRemoteFolder folder, IProgressMonitor monitor) throws CVSException {
		
		// make a copy of the folder so that we will not effect the original folder when we refetch the members
		// TODO: this is a strang thing to need to do. We shold fix this.
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
		// If the above failed, look for the old .vcm_meta file
		try {
			folder.getFile(".vcm_meta"); //$NON-NLS-1$
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

}
