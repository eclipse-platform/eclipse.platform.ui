package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
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
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
			if (syncInfo != null) {
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
		FolderSyncInfo syncInfo = parent.getFolderSyncInfo();
		if (syncInfo == null) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.unmanagedParent", resource.getFullPath().toString()), null)); //$NON-NLS-1$
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
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.typesDiffer", resource.getFullPath().toString()), null)); //$NON-NLS-1$
		}
		return remote;
	}
	
	public static IRemoteSyncElement getRemoteSyncTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		ICVSRemoteResource baseTree = null;
		
		// The resource doesn't have a remote base. 
		// However, we still need to check to see if its been created remotely by a third party.
		if(resource.getType() == IResource.FILE) {
			baseTree = remote;
			ICVSRepositoryLocation location;
			if (remote == null) {
				// If there's no base for the file, get the repository location from the parent
				ICVSRemoteResource parent = CVSWorkspaceRoot.getRemoteResourceFor(resource.getParent());
				if (parent == null) {
					throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.unmanagedParent", resource.getFullPath().toString()), null)); //$NON-NLS-1$
				}
				location = parent.getRepository();
			} else {
				location = remote.getRepository();
			}
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFile)managed, tag, progress);
		} else {
			if (remote == null) {
				remote = getRemoteTreeFromParent(resource, managed, tag, progress);
			} else {
				ICVSRepositoryLocation location = remote.getRepository();
				baseTree = RemoteFolderTreeBuilder.buildBaseTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
				remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
			}
		}
		return new CVSRemoteSyncElement(true /*three way*/, resource, baseTree, remote);
	}
	
	/**
	 * Sync the given unshared project against the given repository and module.
	 */
	public static IRemoteSyncElement getRemoteSyncTree(IProject project, ICVSRepositoryLocation location, String moduleName, CVSTag tag, IProgressMonitor progress) throws TeamException {
		if (CVSWorkspaceRoot.getCVSFolderFor(project).isCVSFolder()) {
			return getRemoteSyncTree(project, tag, progress);
		} else {
			progress.beginTask(null, 100);
			RemoteFolder folder = new RemoteFolder(null, location, new Path(moduleName), tag);
			RemoteFolderTree remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)folder.getRepository(), folder, folder.getTag(), Policy.subMonitorFor(progress, 80));
			CVSRemoteSyncElement tree = new CVSRemoteSyncElement(true /*three way*/, project, null, remote);
			tree.makeFoldersInSync(Policy.subMonitorFor(progress, 10));
			try {
				if (!project.getDescription().hasNature(CVSProviderPlugin.getTypeId())) {
					Team.addNatureToProject(project, CVSProviderPlugin.getTypeId(), Policy.subMonitorFor(progress, 10));
				}
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			progress.done();
			return tree;
		}
	}
	
	public static IRemoteSyncElement getRemoteSyncTree(IProject project, IResource[] resources, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(project);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(project);
		if (remote == null) {
			return new CVSRemoteSyncElement(true /*three way*/, project, null, null);
		}
		ArrayList cvsResources = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			cvsResources.add(CVSWorkspaceRoot.getCVSResourceFor(resources[i]));
		}
		CVSRepositoryLocation location = (CVSRepositoryLocation)remote.getRepository();
		ICVSRemoteResource base = RemoteFolderTreeBuilder.buildBaseTree(location, (ICVSFolder)managed, tag, progress);
		remote = RemoteFolderTreeBuilder.buildRemoteTree(location, (ICVSFolder)managed, (ICVSResource[]) cvsResources.toArray(new ICVSResource[cvsResources.size()]), tag, progress);
		return new CVSRemoteSyncElement(true /*three way*/, project, base, remote);
	}
	
	public static ICVSRemoteResource getRemoteTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		if (remote == null) {
			remote = getRemoteTreeFromParent(resource, managed, tag, progress);
		} else if(resource.getType() == IResource.FILE) {
			ICVSRepositoryLocation location = remote.getRepository();
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFile)managed, tag, progress);
		} else {
			ICVSRepositoryLocation location = remote.getRepository();
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);		
		}
		return remote;
	}

	public ICVSRepositoryLocation getRemoteLocation() throws CVSException {
		FolderSyncInfo info = localRoot.getFolderSyncInfo();
		if (info == null) {
			throw new CVSException(Policy.bind("CVSWorkspaceRoot.notCVSFolder", localRoot.getName()));  //$NON-NLS-1$
		}
		return CVSProvider.getInstance().getRepository(info.getRoot());
	}

	public ICVSFolder getLocalRoot() {
		return localRoot;
	}
}