package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSDiffException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

/**
 * Runs the CVS diff command.
 */
class Diff extends AbstractMessageCommand {

	/**
	 * The diff command needs to send a file structure to the server that differs somewhat from the canonical
	 * format sent by other commands. Instead of sending new files as questionables this class sends
	 * new files as modified and fakes them being added. The contents are sent to the server and are 
	 * included in the returned diff report.
	 */
	private class DiffStructureVisitor extends FileStructureVisitor {
		public DiffStructureVisitor(RequestSender requestSender, ICVSFolder mRoot, IProgressMonitor monitor, boolean modifiedOnly, boolean emptyFolders) {
			super(requestSender, mRoot, monitor, modifiedOnly, emptyFolders);
		}
		
		/**
		 * Send unmanaged files as modified with a default entry line.
		 */
		protected void sendFile(ICVSFile mFile, boolean sendQuestionable, String mode) throws CVSException {
			boolean binary = mode != null && mode.indexOf(ResourceSyncInfo.BINARY_TAG) != -1;
			boolean newFile = false;
	
			if (mFile.isManaged()) {
				requestSender.sendEntry(mFile.getSyncInfo().getEntryLine(false));
			} else {
				ResourceSyncInfo info = new ResourceSyncInfo(mFile.getName(), ResourceSyncInfo.ADDED_REVISION, null, null, null, null);
				requestSender.sendEntry(info.getEntryLine(false));
				newFile = true;
			}
	
			if (!mFile.exists()) {
				return;
			}
	
			if (mFile.isModified() || newFile) {
				requestSender.sendModified(mFile, monitor, binary);
			} else {
				requestSender.sendUnchanged(mFile.getName());
			}
		}			
	}
	
	public Diff(ResponseDispatcher responseDispathcer, RequestSender requestSender) {
		super(responseDispathcer, requestSender);
	}
	
	/**
	 * Overwritten to throw the CVSDiffException if the server returns an error, because it just does so when there is a 
	 * difference between  the checked files.	
	 */
	public void execute(String[] globalOptions, String[] localOptions, String[] arguments, ICVSFolder mRoot, IProgressMonitor monitor, PrintStream messageOut) throws CVSException {
		try {	
			super.execute(globalOptions, localOptions, arguments, mRoot, monitor, messageOut);
		} catch (CVSServerException e) {
			throw new CVSDiffException();
		}
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.DIFF;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.DIFF;
	}
	
	/**
	 * Overriden from <code>AbstractMessageCommand</code> to create the diff structure visitor.
	 */
	protected void sendFileStructure(ICVSResource mResource, IProgressMonitor monitor, boolean modifiedOnly, boolean emptyFolders) throws CVSException {
		FileStructureVisitor fsVisitor;
		fsVisitor = new DiffStructureVisitor(requestSender, getRoot(), monitor, modifiedOnly, emptyFolders);
		mResource.accept(fsVisitor);
	}
}