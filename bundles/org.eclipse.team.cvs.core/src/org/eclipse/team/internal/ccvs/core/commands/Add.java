package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;

class Add extends Command {

	/**
	 * Constructor for Add.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Add(ResponseDispatcher responseContainer, RequestSender requestSender) {
		super(responseContainer, requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.ADD;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.ADD;
	}
	
	/**
	 * Checks wether all the arguments (that are meand as Files/Folders)
	 * to add, can give a remoteLocation (that is needed to add them)
	 */
	protected boolean canTraverse() {

		ICVSResource[] mWorkResources;		

		try {
			mWorkResources = getResourceArguments();
		
			for (int i=0; i<mWorkResources.length; i++) {
				Assert.isNotNull(mWorkResources[i].getRemoteLocation(getRoot()));
			}
		} catch (CVSException e) {
			Assert.isTrue(false);
		}
					  		
		return true;
	}
	
	/**
	 * @see Command#sendRequestsToServer(IProgressMonitor)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor) throws CVSException {
		
		ICVSResource[] mWorkResources;
		ICVSResourceVisitor vistor;
		
		Assert.isTrue(getArguments().length != 0); 
		
		// Check that all the arguments can give you an
		// repo that you will need while traversing the
		// file-structure
		Assert.isTrue(canTraverse());
		
		// Get a vistor and use it on every resource we should
		// work on
		vistor = new AddStructureVisitor(requestSender,getRoot(),monitor);
		mWorkResources = getResourceArguments();		
		for (int i = 0; i < mWorkResources.length; i++) {
			mWorkResources[i].accept(vistor);
		}
		
		sendHomeFolder();
	}
	
	/**
	 * If the add succeeded then folders have to be initialized with the 
	 * sync info
	 */
	protected void finished(boolean succsess) throws CVSException {
				
		ICVSFolder mFolder;
		ICVSResource[] mWorkResources;
		
		mWorkResources = getResourceArguments();
				
		if (!succsess) {
			return;
		}
				
		for (int i=0; i<mWorkResources.length; i++) {
			if (mWorkResources[i].isFolder()) {
				mFolder = (ICVSFolder) mWorkResources[i];
				FolderSyncInfo info = mFolder.getParent().getFolderSyncInfo();				
				String repository = info.getRepository() + Client.SERVER_SEPARATOR + mFolder.getName();			
				mFolder.setFolderSyncInfo(new FolderSyncInfo(repository, info.getRoot(), info.getTag(), info.getIsStatic()));
			}
		}
	}	
}