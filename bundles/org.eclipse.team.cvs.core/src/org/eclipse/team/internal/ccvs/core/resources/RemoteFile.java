package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IStatusListener;
import org.eclipse.team.internal.ccvs.core.response.custom.LogHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusMessageHandler;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.ccvs.core.IRemoteFile;

/**
 * This class provides the implementation of IRemoteFile
 */
public class RemoteFile extends RemoteResource implements IRemoteFile {

	/**
	 * Constructor for RemoteFile.
	 */
	protected RemoteFile(RemoteFolder parent, String name, String tag) {
		super(parent, name, tag);
	}

	/**
	 * @see IRemoteFile#getContents()
	 */
	public InputStream getContents(final IProgressMonitor monitor) throws TeamException {
			
		// Perform a "cvs update..."
		RemoteManagedFolder folder = new RemoteManagedFolder(".", getConnection(), parent.getFullPath(), getName());
		List localOptions = getLocalOptionsForTag();
		Client.execute(
			Client.UPDATE,
			Client.EMPTY_ARGS_LIST, 
			(String[])localOptions.toArray(new String[localOptions.size()]),
			new String[]{getName()}, 
			folder,
			monitor,
			CVSTeamProvider.getPrintStream(),
			getConnection(),
			null);
		return ((RemoteManagedFile)folder.getChild(getName())).getCachedContents();
	}

	/**
	 * @see IRemoteFile#getLogEntries()
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws CVSException {
		
		// NOTE: Should we be using the localOptions here?
		
		// Perform a "cvs status..." with a custom message hanlder
		RemoteManagedFolder folder = new RemoteManagedFolder(".", getConnection(), parent.getFullPath(), getName());
		List localOptions = getLocalOptionsForTag();
		final List entries = new ArrayList();
		Client.execute(
			Client.LOG,
			Client.EMPTY_ARGS_LIST, 
			(String[])localOptions.toArray(new String[localOptions.size()]),
			new String[]{getName()}, 
			folder,
			monitor,
			CVSTeamProvider.getPrintStream(),
			getConnection(),
			new IResponseHandler[] {new LogHandler(this, entries)});
		return (ILogEntry[])entries.toArray(new ILogEntry[entries.size()]);
	}
	
	/**
	 * @see IRemoteFile#getRevision()
	 */
	public String getRevision(IProgressMonitor monitor) throws CVSException {
		
		// Create a listener for receiving the revision info
		final String[] revision = new String[] { null };
		IStatusListener listener = new IStatusListener() {
			public void fileStatus(IPath path, String remoteRevision) {
				revision[0] = remoteRevision;
			}
		};
			
		// Perform a "cvs status..." with a custom message hanlder
		RemoteManagedFolder folder = new RemoteManagedFolder(".", getConnection(), parent.getFullPath(), getName());
		List localOptions = getLocalOptionsForTag();
		Client.execute(
			Client.STATUS,
			Client.EMPTY_ARGS_LIST, 
			(String[])localOptions.toArray(new String[localOptions.size()]),
			new String[]{getName()}, 
			folder,
			monitor,
			CVSTeamProvider.getPrintStream(),
			getConnection(),
			new IResponseHandler[] {new StatusMessageHandler(listener)});
		return revision[0];
	}
	
	/**
	 * @see IRemoteResource#getType()
	 */
	public int getType() {
		return FILE;
	}
	
	public RemoteFileRevision toRemoteFileRevision(String revision) {
		return new RemoteFileRevision(parent, getName(), revision);
	}
}

