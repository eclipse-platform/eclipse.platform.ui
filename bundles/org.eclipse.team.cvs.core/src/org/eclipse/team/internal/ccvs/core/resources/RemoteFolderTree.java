package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;

/**
 * Whereas the RemoteFolder class provides access to a remote hierarchy using
 * lazy retrieval via <code>getMembers()</code>, the RemoteFolderTree will force 
 * a recursive retrieval of the remote hierarchy in one round trip.
 */
public class RemoteFolderTree extends RemoteFolder  {
	
	public RemoteFolderTree(ICVSRepositoryLocation repository, IPath repositoryRelativePath, String tag) {
		super(repository, repositoryRelativePath, tag);
	}

	/* 
	 * Override of inherited method which persists the children
	 */
	public ICVSRemoteResource[] getMembers(String tagName, IProgressMonitor monitor) throws TeamException {
		if (getChildren() == null)
			setChildren(super.getMembers(tagName, monitor));
		return getChildren();
	}

	/* 
	 * This method is public to allow access by the RemoteFolderTreeBuilder utility class.
	 * No other external classes should use this method.
	 */
	public void setChildren(ICVSRemoteResource[] children) {
		super.setChildren(children);
	}
}

