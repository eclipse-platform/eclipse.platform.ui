package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class Add extends Command {
	/*** Local options: specific to add ***/

	protected Add() { }
	protected String getRequestId() {
		return "add";  //$NON-NLS-1$
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		// Check that all the arguments can give you an
		// repo that you will need while traversing the
		// file-structure
		for (int i = 0; i < resources.length; i++) {
			Assert.isNotNull(resources[i].getRemoteLocation(session.getLocalRoot()));
		}
		
		// Get a vistor and use it on every resource we should
		// work on
		AddStructureVisitor visitor = new AddStructureVisitor(session, monitor);
		visitor.visit(session, resources);
	}
	
	/**
	 * If the add succeeded then folders have to be initialized with the 
	 * sync info
	 */
	protected void commandFinished(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor,
		boolean succeeded) throws CVSException {
				
		ICVSFolder mFolder;
		ICVSResource[] mWorkResources;
		
		if (! succeeded) {
			return;
		}
				
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].isFolder()) {
				mFolder = (ICVSFolder) resources[i];
				FolderSyncInfo info = mFolder.getParent().getFolderSyncInfo();
				if (info == null)
					throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("Add.invalidParent", mFolder.getRelativePath(session.getLocalRoot())))); //$NON-NLS-1$
				String repository = info.getRepository() + "/" + mFolder.getName();	 //$NON-NLS-1$	
				mFolder.setFolderSyncInfo(new FolderSyncInfo(repository, info.getRoot(), info.getTag(), info.getIsStatic()));
			}
		}
	}	
}