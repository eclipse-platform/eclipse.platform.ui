package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

public class CVSWorkspaceRoot {

	private ICVSFolder localRoot;
	
	public CVSWorkspaceRoot(IContainer resource){
		this.localRoot = getCVSFolderFor(resource);
	}
	
	public static ICVSFolder getCVSFolderFor(IContainer resource) {
		return new EclipseFolder(resource);
	}


	public static ICVSFile getCVSFileFor(IFile resource) {
		return new EclipseFile(resource);
	}


	public static ICVSResource getCVSResourceFor(IResource resource) {
		if (resource.getType() == IResource.FILE)
			return getCVSFileFor((IFile) resource);
		else
			return getCVSFolderFor((IContainer) resource);
	}
	
	public static ICVSRemoteResource getRemoteResourceFor(IResource resource) throws CVSException {
		ICVSResource managed = getCVSResourceFor(resource);
		return getRemoteResourceFor(managed);
	}
	
	public static ICVSRemoteResource getRemoteResourceFor(ICVSResource resource) throws CVSException {
		if (resource.isFolder()) {
			ICVSFolder folder = (ICVSFolder)resource;
			if (folder.isCVSFolder()) {
				FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
				return new RemoteFolder(null, CVSProvider.getInstance().getRepository(syncInfo.getRoot()), new Path(syncInfo.getRepository()), syncInfo.getTag());
			}
		} else {
			if (resource.isManaged())
				return RemoteFile.getBase((RemoteFolder)getRemoteResourceFor(resource.getParent()), (ICVSFile)resource);
		}
		return null;
	}
	
	/*
	 * Helper method that uses the parent of a local resource that has no base to ensure that the resource
	 * wasn't added remotely by a third party
	 */
	private static ICVSRemoteResource getRemoteTreeFromParent(IResource resource, ICVSResource managed, CVSTag tag, IProgressMonitor progress) throws TeamException {
		// If the parent isn't mapped to CVS, there's nothing we can do
		ICVSFolder parent = managed.getParent();
		if (!parent.isCVSFolder()) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, resource.getFullPath(), Policy.bind("CVSTeamProvider.unmanagedParent", resource.getFullPath().toString()), null)); //$NON-NLS-1$
		}
		ICVSRepositoryLocation location = CVSProvider.getInstance().getRepository(parent.getFolderSyncInfo().getRoot());
		// XXX We build and fetch the whole tree from the parent. We could restrict the search to just the desired child
		RemoteFolder remoteParent = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, parent, tag, progress);
		ICVSRemoteResource remote = null;
		if (remoteParent != null) {
			try {
				remote = (ICVSRemoteResource)remoteParent.getChild(resource.getName());
			} catch (CVSException e) {
				remote = null;
			}
			// The types need to match or we're in trouble
			if (remote != null && !(remote.isContainer() == managed.isFolder()))
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, resource.getFullPath(), Policy.bind("CVSTeamProvider.typesDiffer", resource.getFullPath().toString()), null)); //$NON-NLS-1$
		}
		return remote;
	}
	
	public static IRemoteSyncElement getRemoteSyncTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		ICVSRemoteResource baseTree = null;
		
		// The resource doesn't have a remote base. 
		// However, we still need to check to see if its been created remotely by a third party.
		if (remote == null) {
			remote = getRemoteTreeFromParent(resource, managed, tag, progress);
		} else if(resource.getType() == IResource.FILE) {
			baseTree = remote;
			ICVSRemoteResource remoteParent = CVSWorkspaceRoot.getRemoteResourceFor(resource.getParent());
			remote = RemoteFile.getLatest((RemoteFolder)remoteParent, (ICVSFile)managed, tag, progress);
		} else {
			ICVSRepositoryLocation location = remote.getRepository();
			baseTree = RemoteFolderTreeBuilder.buildBaseTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
		}
		return new CVSRemoteSyncElement(true /*three way*/, resource, baseTree, remote);
	}
	
	public static ICVSRemoteResource getRemoteTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		if (remote == null) {
			remote = getRemoteTreeFromParent(resource, managed, tag, progress);
		} else if(resource.getType() == IResource.FILE) {
			ICVSRemoteResource remoteParent = CVSWorkspaceRoot.getRemoteResourceFor(resource.getParent());
			remote = RemoteFile.getLatest((RemoteFolder)remoteParent, (ICVSFile)managed, tag, progress);
		} else {
			ICVSRepositoryLocation location = remote.getRepository();
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);		
		}
		return remote;
	}

	public ICVSRepositoryLocation getRemoteLocation() throws CVSException {
		return CVSProvider.getInstance().getRepository(localRoot.getFolderSyncInfo().getRoot());
	}

	public ICVSFolder getLocalRoot() {
		return localRoot;
	}
}