package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Whereas the RemoteFolder class provides access to a remote hierarchy using
 * lazy retrieval via <code>getMembers()</code>, the RemoteFolderTree will force 
 * a recursive retrieval of the remote hierarchy in one round trip.
 */
public class RemoteFolderTree extends RemoteFolder  {
	
	public RemoteFolderTree(RemoteFolder parent, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag) {
		super(parent, repository, repositoryRelativePath, tag);
	}
	
	public RemoteFolderTree(RemoteFolder parent, String name, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag) {
		super(parent, name, repository, repositoryRelativePath, tag, false);
	}

	/* 
	 * Override of inherited method which persists the children
	 */
	public ICVSRemoteResource[] getMembers(CVSTag tagName, IProgressMonitor monitor) throws CVSException {
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
	
	/*
	 * @see ICVSFolder#acceptChildren(ICVSResourceVisitor)
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
		ICVSRemoteResource[] children = getChildren();
		if (children == null) return;
		for (int i=0; i<children.length; i++) {
			((ICVSResource)children[i]).accept(visitor);
		}
	}
}

