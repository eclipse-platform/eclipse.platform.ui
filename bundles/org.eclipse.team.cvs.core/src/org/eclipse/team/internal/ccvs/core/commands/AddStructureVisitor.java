package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * This is a visitor that is specially created for the add-command.<br>
 * It traverses the file-structure in the other direction, so that
 * all the parents are send until a parent is found that should allready
 * be known by the to the root are send.<br>
 * The visitor remembers the folders it has allready been to and does not
 * send them again (if possible). 
 */
public class AddStructureVisitor extends AbstractStructureVisitor {
	
	private boolean forceSend = false;
	private Set visitedFolders = new HashSet();
	private IManagedFolder lastVisitedFolder;
	private IManagedFolder mRoot;
	private RequestSender requestSender;
	
	/**
	 * Constructor for AddStructureVisitor.
	 * @param requestSender
	 * @param mRoot
	 * @param monitor
	 */
	public AddStructureVisitor(
		RequestSender requestSender,
		IManagedFolder mRoot,
		IProgressMonitor monitor) {
		super(requestSender, mRoot, monitor);
		this.mRoot = mRoot;
		this.requestSender = requestSender;
	}

	/**
	 * @see IManagedVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(IManagedFile mFile) throws CVSException {
		
		if (!mFile.getParent().equals(lastVisitedFolder)) {
			forceSend = true;
			mFile.getParent().accept(this);
		}
		
		// We just send the fact, that the file is modified
		// not the data, we do not need it.
		requestSender.sendIsModified(mFile.getName());
		
	}

	/**
	 * @see IManagedVisitor#visitFolder(IManagedFolder)
	 */
	public void visitFolder(IManagedFolder mFolder) throws CVSException {
		
		Assert.isNotNull(mFolder);
		
		// Save the status wheter we want to send
		// this folder in every case
		boolean alreadyVisited;
		boolean forceSend = this.forceSend;
		this.forceSend = false;
		
		alreadyVisited = visitedFolders.contains(mFolder);
		
		if (!mFolder.equals(mRoot) && !alreadyVisited) {
			mFolder.getParent().accept(this);
		}
		
		if (forceSend || !alreadyVisited) {
			visitedFolders.add(mFolder);
			lastVisitedFolder = mFolder;
			sendFolder(mFolder,false,false);
		}
	}

}

