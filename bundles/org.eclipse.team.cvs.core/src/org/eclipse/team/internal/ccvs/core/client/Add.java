package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class Add extends Command {
	/*** Local options: specific to add ***/

	protected Add() { }
	protected String getCommandId() {
		return "add";
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		// Check that all the arguments can give you an
		// repo that you will need while traversing the
		// file-structure
		try {
			for (int i = 0; i < resources.length; i++) {
				Assert.isNotNull(resources[i].getRemoteLocation(session.getLocalRoot()));
			}
		} catch (CVSException e) {
			Assert.isTrue(false);
		}
		
		// Get a vistor and use it on every resource we should
		// work on
		ICVSResourceVisitor visitor = new AddStructureVisitor(session, monitor);
		for (int i = 0; i < resources.length; i++) {
			resources[i].accept(visitor);
		}
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
				String repository;
				if (info == null) {
					// If the parent sync info is null, there may be some already with the folder itself
					// This is special case handling to allow an add of a root folder to CVS
					info = mFolder.getFolderSyncInfo();	
					repository = mFolder.getName();
				} else {
					repository = info.getRepository() + "/" + mFolder.getName();
				}			
				mFolder.setFolderSyncInfo(new FolderSyncInfo(repository, info.getRoot(), info.getTag(), info.getIsStatic()));
			}
		}
	}	
}