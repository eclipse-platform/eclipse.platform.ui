package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.IRemoteFolder;
import org.eclipse.team.ccvs.core.IRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.commands.CommandDispatcher;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.response.custom.IStatusListener;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusMessageHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;

/**
 * This class provides the implementation of IRemoteFolder
 */
public class RemoteFolder extends RemoteResource implements IRemoteFolder {

	/**
	 * Constructor for RemoteFolder.
	 */
	public RemoteFolder(RemoteFolder parent, String name, String tag) {
		super(parent, name, tag);
	}

	// Get the file revisions for the given filenames
	protected String[] getFileRevisions(Connection connection, String[] fileNames, IProgressMonitor monitor) throws CVSException {
		
		// Create a listener for receiving the revision info
		final Map revisions = new HashMap();
		IStatusListener listener = new IStatusListener() {
			public void fileStatus(IPath path, String remoteRevision) {
				revisions.put(path.lastSegment(), remoteRevision);
			}
		};
			
		// Perform a "cvs status..." with a custom message hanlder
		RemoteManagedFolder folder = new RemoteManagedFolder(".", getConnection(), getFullPath(), fileNames, tag);
		List localOptions = getLocalOptionsForTag();
		
		// NOTE: This should be in a single methodin Client
		ResponseDispatcher responseDispatcher = new ResponseDispatcher(connection, new IResponseHandler[] {new StatusMessageHandler(listener)});
		RequestSender requestSender = new RequestSender(connection);
		CommandDispatcher commandDispatcher = new CommandDispatcher(responseDispatcher, requestSender);		
		commandDispatcher.execute(
			Client.STATUS,
			Client.EMPTY_ARGS_LIST, 
			(String[])localOptions.toArray(new String[localOptions.size()]),
			fileNames,
			folder,
			monitor,
			CVSTeamProvider.getPrintStream());

//		client.execute(
//			Client.STATUS,
//			Client.EMPTY_ARGS_LIST, 
//			(String[])localOptions.toArray(new String[localOptions.size()]),
//			fileNames,
//			folder,
//			monitor,
//			CVSTeamProvider.getPrintStream(),
//			connection,
//			new IResponseHandler[] {new StatusMessageHandler(listener)});
		
		if (revisions.size() != fileNames.length)
			throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions"));
		String[] result = new String[fileNames.length];
		for (int i=0;i<fileNames.length;i++) {
			String revision = (String)revisions.get(fileNames[i]);
			if (revision == null)
				throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions"));
			result[i] = revision;
		}
		return result;
	}
	
	/**
	 * @see IRemoteFolder#getMembers()
	 */
	public IRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException {
		return getMembers(tag, monitor);
	}

	/**
	 * @see IRemoteRoot#getMembers()
	 */
	public IRemoteResource[] getMembers(final String tagName, final IProgressMonitor monitor) throws TeamException {
		
		// Create the listener for remote files and folders
		final List errors = new ArrayList();
		final List newRemoteDirectories = new ArrayList();
		final List newRemoteFiles = new ArrayList();
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory && path.segmentCount() == 1)
					newRemoteDirectories.add(path.lastSegment());
//				monitor.subTask(path.lastSegment().toString());
//				monitor.worked(1);
			}
			public void directoryDoesNotExist(IPath path) {
//				monitor.worked(1);
			}
			public void fileInformation(char type, String filename) {
				IPath filePath = new Path(filename);	
				if( filePath.segmentCount() == 1 ) {
					String properFilename = filePath.lastSegment();
					newRemoteFiles.add(properFilename);
//					monitor.subTask(properFilename);
//					monitor.worked(1);
				}
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tagName != null) && (!tagName.equals("HEAD"))) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tagName);
		}
		
		// Retrieve the children and any file revision numbers in a single connection
		Connection c = getConnection().openConnection();
		List result = new ArrayList();
		try {
			// Perform a "cvs -n update -d -r tagName folderName" with custom message and error handlers
			Client.execute(
				Client.UPDATE,
				new String[]{Client.NOCHANGE_OPTION}, 
				(String[])localOptions.toArray(new String[localOptions.size()]), 
				new String[]{"."}, 
				new RemoteManagedFolder(".", getConnection(), getFullPath()),
				monitor,
				CVSTeamProvider.getPrintStream(),
				c,
				new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)});
			// Get the revision numbers for the files
			
			if (newRemoteFiles.size() > 0) {
				String[] revisions = getFileRevisions(c, (String[])newRemoteFiles.toArray(new String[newRemoteFiles.size()]), monitor);
				for (int i=0;i<newRemoteFiles.size();i++) {
					result.add(new RemoteFile(this, (String)newRemoteFiles.get(i), revisions[i]));
				}
			}
			for (int i=0;i<newRemoteDirectories.size();i++)
				result.add(new RemoteFolder(this, (String)newRemoteDirectories.get(i), tagName));


			
		} catch (CVSException e) {
			throw CVSTeamProvider.wrapException(e, errors);
		} finally {
			c.close();
		}
		return (IRemoteResource[])result.toArray(new IRemoteResource[0]);
	}

	/**
	 * @see IRemoteResource#getType()
	 */
	public int getType() {
		return FOLDER;
	}

}

