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
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.ccvs.core.ICVSRunnable;
import org.eclipse.team.ccvs.core.IResourceStateChangeListener;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Listen for the addition of orphaned subtrees as a result of a copy or move.
 */
public class AddDeleteMoveListener implements IResourceDeltaVisitor, IResourceChangeListener, IResourceStateChangeListener {

	public static final String DELETION_MARKER = "org.eclipse.team.cvs.core.cvsremove";
	public static final String ADDITION_MARKER = "org.eclipse.team.cvs.core.cvsadd";
	
	public static final String NAME_ATTRIBUTE = "name";
	
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
			marker.setAttribute("name", resource.getName());
			marker.setAttribute(IMarker.MESSAGE, resource.getName() + " has been deleted locally");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			return marker;
		} catch (CoreException e) {
			Util.logError("Error creating deletion marker", e);
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
			marker.setAttribute(IMarker.MESSAGE, "Local addition not under CVS control");
			return marker;
		} catch (CoreException e) {
			Util.logError("Error creating addition marker", e);
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
				if (resource.getType() == IResource.FILE) {
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					if (cvsResource.isManaged() || cvsResource.isIgnored()) {
						if (cvsResource.exists()) {
							IMarker marker = getAdditionMarker(resource);
							if (marker != null)
								marker.delete();
						}
					} else if ( ! cvsResource.exists()) {
						IMarker marker = getDeletionMarker(resource);
						if (marker != null)
							marker.delete();
						cvsResource.getParent().run(new ICVSRunnable() {
							public void run(IProgressMonitor monitor) throws CVSException {
								pruneEmptyParents(resource);
							}

						}, Policy.monitorFor(null));
					} else {
						// The resource is not managed or ignored. Make sure there is an addition marker on it
						IMarker marker = getAdditionMarker(resource);
						if (marker == null) {
							createAdditonMarker(resource);
						}
					}
				} else if (resource.getType() == IResource.FOLDER) {
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					if (cvsResource.isManaged() || cvsResource.isIgnored()) {
						IMarker marker = getAdditionMarker(resource);
						if (marker != null) {
							marker.delete();
							// Check to see if there are unmanaged, unignored children
							// If there are, mark them
							if (cvsResource.isManaged()) {
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
					} 
				}
			} catch (CVSException e) {
				Util.logError("Error updating marker state", e);
			} catch (CoreException e) {
				Util.logError("Error updating marker state", e);
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
}