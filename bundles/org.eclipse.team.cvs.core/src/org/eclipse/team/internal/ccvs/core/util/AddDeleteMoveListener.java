package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Listen for the addition of orphaned subtrees as a result of a copy or move.
 */
public class AddDeleteMoveListener implements IResourceDeltaVisitor, IResourceChangeListener, IResourceStateChangeListener {

	public static final String CVS_MARKER = "org.eclipse.team.cvs.core.cvsmarker";//$NON-NLS-1$
	public static final String DELETION_MARKER = "org.eclipse.team.cvs.core.cvsremove";//$NON-NLS-1$
	public static final String ADDITION_MARKER = "org.eclipse.team.cvs.core.cvsadd";//$NON-NLS-1$
	
	public static final String NAME_ATTRIBUTE = "name";//$NON-NLS-1$
	
	private void clearCVSMarkers(IResource resource) throws CoreException {
		IMarker[] markers = resource.findMarkers(CVS_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			markers[i].delete();
		}
	}
	
	protected IMarker createDeleteMarker(IResource resource) {
		if (! CVSProviderPlugin.getPlugin().getShowTasksOnAddAndDelete()) {
			return null;
		}
		try {
			IMarker marker = getDeletionMarker(resource);
			if (marker != null) {
				return marker;
			}
			marker = resource.getParent().createMarker(DELETION_MARKER);
			marker.setAttribute("name", resource.getName());//$NON-NLS-1$
			marker.setAttribute(IMarker.MESSAGE, Policy.bind("AddDeleteMoveListener.deletedResource", resource.getName()));//$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			return marker;
		} catch (CoreException e) {
			Util.logError(Policy.bind("AddDeleteMoveListener.Error_creating_deletion_marker_1"), e); //$NON-NLS-1$
		}
		return null;
	}
	
	protected IMarker createAdditonMarker(IResource resource) {
		if (! CVSProviderPlugin.getPlugin().getShowTasksOnAddAndDelete()) {
			return null;
		}
		try {
			IMarker marker = getAdditionMarker(resource);
			if (marker != null) {
				return marker;
			}
			marker = resource.createMarker(ADDITION_MARKER);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			marker.setAttribute(IMarker.MESSAGE, Policy.bind("AddDeleteMoveListener.Local_addition_not_under_CVS_control_2")); //$NON-NLS-1$
			return marker;
		} catch (CoreException e) {
			Util.logError(Policy.bind("AddDeleteMoveListener.Error_creating_addition_marker_3"), e); //$NON-NLS-1$
		}
		return null;
	}
	
	protected IMarker getAdditionMarker(IResource resource) throws CoreException {
   		IMarker[] markers = resource.findMarkers(ADDITION_MARKER, false, IResource.DEPTH_ZERO);
   		if (markers.length == 1) {
   			return markers[0];
   		}
		return null;
	}
	
	protected IMarker getDeletionMarker(IResource resource) throws CoreException {
		if (resource.getParent().exists()) {
			String name = resource.getName();
	   		IMarker[] markers = resource.getParent().findMarkers(DELETION_MARKER, false, IResource.DEPTH_ZERO);
	   		for (int i = 0; i < markers.length; i++) {
				IMarker iMarker = markers[i];
				String markerName = (String)iMarker.getAttribute(NAME_ATTRIBUTE);
				if (markerName.equals(name))
					return iMarker;
			}
		}
		return null;
	}
	
	public static IResource getResourceFor(IProject container, IResource destination, IPath originating) {
		switch(destination.getType()) {
			case IResource.FILE : return container.getFile(originating); 			
			case IResource.FOLDER: return container.getFolder(originating);
			case IResource.PROJECT: return ResourcesPlugin.getWorkspace().getRoot().getProject(originating.toString());
		}
		return destination;
	}
	
	/**
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		IProject project = resource.getProject();
		boolean movedTo = (delta.getFlags() & IResourceDelta.MOVED_TO) > 0;
		boolean movedFrom = (delta.getFlags() & IResourceDelta.MOVED_FROM) > 0;
		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				if (resource.getType() == IResource.FOLDER) {
					handleOrphanedSubtree((IContainer)resource);
					handleAddedFolder((IFolder) resource);
				} else if (resource.getType() == IResource.FILE) {
					handleAddedFile((IFile)resource);
				}
				break;
			case IResourceDelta.REMOVED :
				if (resource.getType() == IResource.FILE) {
					handleDeletedFile((IFile)resource);
				}
				break;
			case IResourceDelta.CHANGED :
				// This state means there is a resource before and after but changes were made by deleting and moving.
				// For files, we shouldn'd do anything.
				// For folders, we should purge the CVS info
				if (resource.getType() == IResource.FOLDER) {
					// When folders are moved, purge the CVS folders
					if (movedFrom)
						return ! handleOrphanedSubtree((IContainer)resource);
				}
				handleChangedResource(resource);
				break;
		}
		return true;
	}
	
	/*
	 * Determine if the container is an orphaned subtree. 
	 * If it is, handle it and return true. 
	 * Otherwise, return false
	 */
	private boolean handleOrphanedSubtree(IContainer resource) {
		try {
			ICVSFolder mFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resource);
			if (mFolder.isCVSFolder() && ! mFolder.isManaged() && mFolder.getParent().isCVSFolder()) {
				mFolder.unmanage(null);
				return true;
			}
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
		return false;
	}
	
	
	/*
	 * Mark deleted managed files as outgoing deletions
	 */
	private void handleDeletedFile(IFile resource) {
		try {
			ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
			if (mFile.isManaged()) {
				ResourceSyncInfo info = mFile.getSyncInfo();
				if (info.isAdded()) {
					mFile.unmanage(null);
				} else {
					createDeleteMarker(resource);
					MutableResourceSyncInfo deletedInfo = info.cloneMutable();
					deletedInfo.setDeleted(true);
					mFile.setSyncInfo(deletedInfo);
				}
			}
			// XXX If .cvsignore was deleted, we may have unmanaged additions in the same folder
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
	}
	
	/*
	 * Handle the case where an added file has the same name as a "cvs removed" file
	 * by restoring the sync info to what it was before the delete
	 */
	private void handleAddedFile(IFile resource) {
		try {
			ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
			if (mFile.isManaged()) {
				ResourceSyncInfo info = mFile.getSyncInfo();
				if (info.isDeleted()) {
					// Handle a replaced deletion
					MutableResourceSyncInfo undeletedInfo = info.cloneMutable();
					undeletedInfo.setDeleted(false);
					mFile.setSyncInfo(undeletedInfo);
					try {
						IMarker marker = getDeletionMarker(resource);
						if (marker != null) marker.delete();
					} catch (CoreException e) {
						CVSProviderPlugin.log(e.getStatus());
					}
				} else if (info.isDirectory()) {
					// XXX This is a gender change against the server! We should prevent this creation.
					mFile.unmanage(null);
					createAdditonMarker(resource);
				}
			} else if ( mFile.getParent().isCVSFolder() && ! mFile.isIgnored()) {
				createAdditonMarker(resource);
			}
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
	}
	
	private void handleAddedFolder(IFolder resource) {
		try {
			ICVSFolder mFolder = CVSWorkspaceRoot.getCVSFolderFor(resource);
			if (mFolder.isManaged()) {
				ResourceSyncInfo info = mFolder.getSyncInfo();
				if ( ! info.isDirectory()) {
					// XXX This is a gender change against the server! We should prevent this creation.
					mFolder.unmanage(null);
					createAdditonMarker(resource);
				}
			} else if ( mFolder.getParent().isCVSFolder() && ! mFolder.isIgnored()) {
				createAdditonMarker(resource);
			}
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
	}
	
	private void handleChangedResource(IResource resource) {
		if (resource.getType() == IResource.PROJECT) return;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		// Make sure that unmanaged resources whose parent is a cvs folder have an addition task on them
		if ( ! cvsResource.isManaged() && ! cvsResource.isIgnored() && cvsResource.getParent().isCVSFolder()) {
			createAdditonMarker(resource);
		}
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {							
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());	

				// if a project is moved the originating project will not be associated with the CVS provider
				// however listeners will probably still be interested in the move delta.	
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) > 0) {																
					IResource destination = getResourceFor(resource.getProject(), resource, delta.getMovedToPath());
					provider = RepositoryProvider.getProvider(destination.getProject());
				}
				
				if(provider!=null) {
					delta.accept(this);
				}
			}
		} catch (CoreException e) {
			Util.logError(Policy.bind("ResourceDeltaVisitor.visitError"), e);//$NON-NLS-1$
		}
	}
	
	/*
	 * @see IResourceStateChangeListener#resourceStateChanged(IResource[])
	 */
	public void resourceStateChanged(IResource[] changedResources) {
		for (int i = 0; i < changedResources.length; i++) {
			try {
				final IResource resource = changedResources[i];
				// Only update markers for projects with a provider
				RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());	
				if (provider == null) break;
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
				// Handle addition markers
				if (cvsResource.isManaged() || cvsResource.isIgnored()) {
					if (cvsResource.exists()) {
						// Remove the addition marker for managed or ignored resources
						IMarker marker = getAdditionMarker(resource);
						if (marker != null)
							marker.delete();
						// For managed folders, add addition markers to unmanaged/unignored children
						if (cvsResource.isManaged() && cvsResource.isFolder()) {
							IResource[] children = ((IFolder)resource).members();
							for (int j = 0; j < children.length; j++) {
								IResource iResource = children[j];
								ICVSResource child = CVSWorkspaceRoot.getCVSResourceFor(iResource);
								if ( ! child.isManaged() && ! child.isIgnored()) {
									createAdditonMarker(iResource);
								}
							}
						}
					}
				} else if (cvsResource.getParent().isCVSFolder()) {
					// If the parent is a CVS folder, place an addition marker on the resource
					IMarker marker = getAdditionMarker(resource);
					if (marker == null) {
						createAdditonMarker(resource);
					}
				}
				
				// Handle deletion markers
				if (resource.getType() == IResource.FILE) {
					if (cvsResource.exists()) {
						IMarker marker = getDeletionMarker(resource);
						if (marker != null)
							marker.delete();
					} else {
						if (cvsResource.isManaged()) {
							createDeleteMarker(resource);
						} else {
							IMarker marker = getDeletionMarker(resource);
							if (marker != null)
								marker.delete();
							cvsResource.getParent().run(new ICVSRunnable() {
								public void run(IProgressMonitor monitor) throws CVSException {
									pruneEmptyParents(resource);
								}
	
							}, Policy.monitorFor(null));
						}
					}
				}
			} catch (CVSException e) {
				Util.logError(Policy.bind("AddDeleteMoveListener.Error_updating_marker_state_4"), e); //$NON-NLS-1$
			} catch (CoreException e) {
				Util.logError(Policy.bind("AddDeleteMoveListener.Error_updating_marker_state_4"), e); //$NON-NLS-1$
			}
		}
	}

	private void pruneEmptyParents(IResource resource) throws CVSException {
		// Don't prune if pruning is off
		if ( ! CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) {
			return;
		}
		// Make sure it's a folder and not the project or workspace root
		IContainer parent = resource.getParent();
		if (parent.getType() != IResource.FOLDER) {
			return;
		}
		// XXX Could use members on IFolder once team-private works
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(parent);
		if (folder.exists() 
				&& folder.isManaged()
				&& folder.getFiles().length == 0 
				&& folder.getFolders().length == 0) {
			folder.delete();
			folder.unmanage(null);
			pruneEmptyParents(parent);
		}
	}
	
	/**
	 * @see IResourceStateChangeListener#projectConfigured(IProject)
	 */
	public void projectConfigured(final IProject project) {
		try {
			final ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(project);
			root.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					if (file.getParent().isCVSFolder()) {
						if (file.isManaged() && file.getSyncInfo().isDeleted()) {
							createDeleteMarker(project.getFile(file.getRelativePath(root)));
						} else if ( ! file.isManaged() && ! file.isIgnored()) {
							createAdditonMarker(project.getFile(file.getRelativePath(root)));
						}
					}
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (folder.isCVSFolder()) {
						folder.acceptChildren(this);
					} else if ( ! folder.isIgnored() && folder.getParent().isCVSFolder()) {
						createAdditonMarker(project.getFolder(folder.getRelativePath(root)));
					}
				}
			});
		} catch (CVSException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
	}

	/**
	 * @see IResourceStateChangeListener#projectDeconfigured(IProject)
	 */
	public void projectDeconfigured(IProject project) {
		try {
			clearCVSMarkers(project);
		} catch (CoreException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
	}

}