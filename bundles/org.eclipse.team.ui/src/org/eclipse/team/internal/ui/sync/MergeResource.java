package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * Encapsulates information about a resource that requires
 * contact with the Team API.
 */
public class MergeResource {
	private IRemoteSyncElement syncTree;
	
	/**
	 * Creates a new merge resource based on the given sync information.
	 */
	public MergeResource(IRemoteSyncElement syncTree) {
		this.syncTree = syncTree;
	}
	
	/**
	 * Returns an InputStream for the base revision of this incoming resource.
	 */
	public InputStream getBaseRevision() throws CoreException {
		IRemoteResource remote = syncTree.getBase();
		if (remote != null && !remote.isContainer()) {
			try {
				return remote.getContents(new NullProgressMonitor());
			} catch (TeamException exception) {
				// The remote resource has gone.
				return null;
			}
		}
		return null;
	}
	
	public String getExtension() {
		if (syncTree.isContainer()) {
			return ITypedElement.FOLDER_TYPE;
		}
		String name = getName();
		if (name != null) {
			int index = name.lastIndexOf('.');
			if (index == -1)
				return ""; //$NON-NLS-1$
			if (index == (name.length() - 1))
				return ""; //$NON-NLS-1$
			return name.substring(index + 1);
		}
		return ITypedElement.FOLDER_TYPE;
	}
	
	/**
	 * Returns an InputStream for the latest repository version of this incoming resource.
	 */
	public InputStream getLatestRevision() throws CoreException {
		IRemoteResource remote = syncTree.getRemote();
		try {
			return remote.getContents(new NullProgressMonitor());
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	
	/**
	 * Returns an InputStream for the local resource.
	 */
	public InputStream getLocalStream() throws CoreException {
		IResource left = syncTree.getLocal();
		if (left == null) return null;
		if (left.exists() && left.getType() == IResource.FILE) {
			return ((IFile)left).getContents(true);
		}
		return null;
	}

	public String getName() {
		return syncTree.getName();
	}
	
	/*
	 * @see IMergeResource#getResource.
	 */
	public IResource getResource() {
		return syncTree.getLocal();
	}

	public IRemoteSyncElement getSyncElement() {
		return syncTree;
	}
	
	/**
	 * Returns true if this merge resource has a base resource,
	 * and false otherwise.
	 */
	public boolean hasBaseRevision() {
		return syncTree.getBase() != null;
	}
	
	/**
	 * Returns true if this merge resource has a latest revision,
	 * and false otherwise.
	 */
	public boolean hasLatestRevision() {
		return syncTree.getRemote() != null;
	}
	
	/**
	 * Is this a leaf node, i.e. a file?
	 */
	public boolean isLeaf() {
		return !syncTree.isContainer();
	}
	
	/**
	 * Updates the given compare configuration with appropriate left, right
	 * and ancestor labels for this resource.
	 */
	public void setLabels(CompareConfiguration config) {
		String name = getName();
		config.setLeftLabel(Policy.bind("MergeResource.workspaceFile", name)); //$NON-NLS-1$
	
	
		IRemoteResource remote = syncTree.getRemote();
		if (remote != null) {
			config.setRightLabel(Policy.bind("MergeResource.repositoryFile", name)); //$NON-NLS-1$
	//		config.setRightLabel(TeamUIPlugin.getResourceString("MergeResource.repositoryFile", new Object[] {name, remote.getVersionName()} ));
		} else {
			config.setRightLabel(Policy.bind("MergeResource.noRepositoryFile")); //$NON-NLS-1$
		}
	
		IRemoteResource base = syncTree.getBase();
		if (base != null) {
			config.setAncestorLabel(Policy.bind("MergeResource.commonFile", name)); //$NON-NLS-1$
	//		config.setAncestorLabel(TeamUIPlugin.getResourceString("MergeResource.commonFile", new Object[] {name, common.getVersionName()} ));
		} else {
			config.setAncestorLabel(Policy.bind("MergeResource.noCommonFile")); //$NON-NLS-1$
		}
	}
}
