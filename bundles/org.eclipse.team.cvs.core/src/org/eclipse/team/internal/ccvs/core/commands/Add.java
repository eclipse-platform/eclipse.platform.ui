package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

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

		IManagedResource[] mWorkResources;		

		try {
			mWorkResources = getWorkResources();
		
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
		
		IManagedResource[] mWorkResources;
		IManagedVisitor vistor;
		
		Assert.isTrue(getArguments().length != 0); 
		
		// Check that all the arguments can give you an
		// repo that you will need while traversing the
		// file-structure
		Assert.isTrue(canTraverse());
		
		// Get a vistor and use it on every resource we should
		// work on
		vistor = new AddStructureVisitor(requestSender,getRoot(),monitor);
		mWorkResources = getWorkResources();		
		for (int i = 0; i < mWorkResources.length; i++) {
			mWorkResources[i].accept(vistor);
		}
		
		sendHomeFolder();
	}
	
	/**
	 * If we were successful in adding, then acctually managed
	 * the folders on disk
	 */
	protected void finished(boolean succsess) throws CVSException {
				
		IManagedFolder mFolder;
		IManagedResource[] mWorkResources;
		FolderProperties folderInfo;
		
		mWorkResources = getWorkResources();
				
		if (!succsess) {
			return;
		}
				
		for (int i=0; i<mWorkResources.length; i++) {
			if (mWorkResources[i].isFolder()) {
				
				mFolder = (IManagedFolder) mWorkResources[i];
				
				folderInfo = mFolder.getParent().getFolderInfo();
				folderInfo.setRepository(folderInfo.getRepository() + 
										Client.SERVER_SEPARATOR + mFolder.getName());
				mFolder.setFolderInfo(folderInfo);
				
			}
		}

	}
	
}

