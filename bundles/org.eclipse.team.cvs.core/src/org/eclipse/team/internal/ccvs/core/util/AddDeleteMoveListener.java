package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
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
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Listen for the addition of orphaned subtrees as a result of a copy or move.
 */
public class AddDeleteMoveListener implements IResourceDeltaVisitor, IResourceChangeListener, IResourceStateChangeListener {

	public static final String CVS_MARKER = "org.eclipse.team.cvs.core.cvsmarker";//$NON-NLS-1$
	public static final String DELETION_MARKER = "org.eclipse.team.cvs.core.cvsremove";//$NON-NLS-1$
	public static final String ADDITION_MARKER = "org.eclipse.team.cvs.core.cvsadd";//$NON-NLS-1$
	
	public static final String NAME_ATTRIBUTE = "name";//$NON-NLS-1$
	
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
				// make sure the added resource isn't a phantom
				if (resource.exists()) {
					if (resource.getType() == IResource.FOLDER) {
						handleOrphanedSubtree((IContainer)resource);
						handleAddedFolder((IFolder) resource);
					} else if (resource.getType() == IResource.FILE) {
						handleAddedFile((IFile)resource);
					}
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
				if (resource.getType() == IResource.FOLDER && resource.exists()) {
					// When folders are moved, purge the CVS folders
					if (movedFrom)
						return ! handleOrphanedSubtree((IContainer)resource);
					if ((delta.getFlags() & IResourceDelta.REPLACED) > 0) {
						handleAddedFolder((IFolder)resource);
						return true;
					}
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
			if (mFolder.isCVSFolder() && ! mFolder.isManaged() && mFolder.getIResource().getParent().getType() != IResource.ROOT) {
				// linked resources are not considered orphans even if they have CVS folders in them
				if (isLinkedResource(mFolder)) return false;
				mFolder.unmanage(null);
				return true;
			}
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
		return false;
	}
	
	private boolean isLinkedResource(ICVSResource cvsResource) throws CVSException {
		IResource iResource = cvsResource.getIResource();
		if (iResource != null)
			return CVSWorkspaceRoot.isLinkedResource(iResource);
		return false;
	}
	/*
	 * Mark deleted managed files as outgoing deletions
	 */
	private void handleDeletedFile(IFile resource) {
		try {
			ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
			byte[] syncBytes = mFile.getSyncBytes();
			if (syncBytes != null) {
				if (ResourceSyncInfo.isAddition(syncBytes)) {
					mFile.unmanage(null);
				} else if ( ! ResourceSyncInfo.isDeletion(syncBytes)) {
					mFile.setSyncBytes(ResourceSyncInfo.convertToDeletion(syncBytes), ICVSFile.UNKNOWN);
				}
			}
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
			EclipseSynchronizer.getInstance().created(resource);	
			ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
			byte[] syncBytes = mFile.getSyncBytes();
			if (syncBytes != null) {
				if (ResourceSyncInfo.isDeletion(syncBytes)) {
					// Handle a replaced deletion
					mFile.setSyncBytes(ResourceSyncInfo.convertFromDeletion(syncBytes), ICVSFile.UNKNOWN);
					try {
						IMarker marker = getDeletionMarker(resource);
						if (marker != null) marker.delete();
					} catch (CoreException e) {
						CVSProviderPlugin.log(e.getStatus());
					}
				} else if (ResourceSyncInfo.isFolder(syncBytes)) {
					// This is a gender change against the server! 
					// We will allow it but the user will get an error if they try to commit
					mFile.unmanage(null);
				}
			}
			createNecessaryMarkers(new IResource[] {resource});
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
	}
	
	private void handleAddedFolder(IFolder resource) {
		try {
			EclipseSynchronizer.getInstance().created(resource);		
			ICVSFolder mFolder = CVSWorkspaceRoot.getCVSFolderFor(resource);
			if (mFolder.isManaged()) {
				ResourceSyncInfo info = mFolder.getSyncInfo();
				if ( ! info.isDirectory()) {
					// This is a gender change against the server!
					// Operation failure will notify the user of this situation
					mFolder.unmanage(null);
				}
			}
			createNecessaryMarkers(new IResource[] {resource});
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {							
				final IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				
				if (resource.getType() == IResource.PROJECT) {
					// If the project is not accessible, don't process it
					if (!resource.isAccessible()) continue;
					if ((delta.getFlags() & IResourceDelta.OPEN) != 0) continue;
				}
				
				RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());	

				// Make sure that the project is a CVS folder.
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(resource.getProject());
				if (provider != null) {
					try {
						if (! folder.isCVSFolder()) {
							RepositoryProvider.unmap(resource.getProject());
							provider = null;
						}
					} catch (TeamException e) {
						CVSProviderPlugin.log(e.getStatus());
					}
				}
				
				// if a project is moved the originating project will not be associated with the CVS provider
				// however listeners will probably still be interested in the move delta.	
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) > 0) {																
					IResource destination = getResourceFor(resource.getProject(), resource, delta.getMovedToPath());
					provider = RepositoryProvider.getProvider(destination.getProject());
				}
				
				if(provider!=null) {
					// Traverse the delta is a runnable so that files are only written at the end
					folder.run(new ICVSRunnable() {
						public void run(IProgressMonitor monitor) throws CVSException {
							try {
								delta.accept(AddDeleteMoveListener.this);
							} catch (CoreException e) {
								Util.logError(Policy.bind("ResourceDeltaVisitor.visitError"), e);//$NON-NLS-1$
							}
						}
					}, Policy.monitorFor(null));
				}
			}
		} catch (CVSException e) {
			Util.logError(Policy.bind("ResourceDeltaVisitor.visitError"), e);//$NON-NLS-1$
		}
	}
	
	/*
	 * @see IResourceStateChangeListener#resourceStateChanged(IResource[])
	 */
	public void resourceSyncInfoChanged(IResource[] changedResources) {
		createNecessaryMarkers(changedResources);
	}
			
	/**
	 * @see IResourceStateChangeListener#projectConfigured(IProject)
	 */
	public void projectConfigured(final IProject project) {
		try {
			// Create deletion tasks for any deleted resources
			final ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(project);
			root.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					if (file.getParent().isCVSFolder()) {
						byte[] syncBytes = file.getSyncBytes();
						if (syncBytes != null && ResourceSyncInfo.isDeletion(syncBytes)) {
							createDeleteMarker(project.getFile(file.getRelativePath(root)));
						}
					}
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (folder.isCVSFolder()) {
						folder.acceptChildren(this);
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
	
	public static void refreshAllMarkers() throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if(RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) != null) {
				refreshMarkers(project);
			}
		}		
	}
	
	private static IMarker createDeleteMarker(IResource resource) {
		if (! CVSProviderPlugin.getPlugin().getShowTasksOnAddAndDelete()) {
			return null;
		}
		try {
			IMarker marker = getDeletionMarker(resource);
			if (marker != null) {
				return marker;
			}
			IContainer parent = resource.getParent();
			if (! parent.exists()) return null;
			marker = parent.createMarker(DELETION_MARKER);
			marker.setAttribute("name", resource.getName());//$NON-NLS-1$
			marker.setAttribute(IMarker.MESSAGE, Policy.bind("AddDeleteMoveListener.deletedResource", resource.getName()));//$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			return marker;
		} catch (CoreException e) {
			Util.logError(Policy.bind("AddDeleteMoveListener.Error_creating_deletion_marker_1"), e); //$NON-NLS-1$
		}
		return null;
	}
	
	private static IMarker getAdditionMarker(IResource resource) throws CoreException {
   		IMarker[] markers = resource.findMarkers(ADDITION_MARKER, false, IResource.DEPTH_ZERO);
   		if (markers.length == 1) {
   			return markers[0];
   		}
		return null;
	}
	
	private static IMarker getDeletionMarker(IResource resource) throws CoreException {
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
	
	private static void createNecessaryMarkers(IResource[] changedResources) {
		for (int i = 0; i < changedResources.length; i++) {
			try {
				final IResource resource = changedResources[i];
				
				if (resource.exists()) {
					// First, delete any addition markers even though we no longer create them
					IMarker marker = getAdditionMarker(resource);
					if (marker != null)
						marker.delete();
					// Also, delete any deletion markers stored on the parent
					if (resource.getType() == IResource.FILE) {
						marker = getDeletionMarker(resource);
						if (marker != null)
							marker.delete();
					}
				} else if (resource.getType() == IResource.FILE) {
					// Handle deletion markers on non-existant files
					RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());	
					if (provider == null) break;
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					if (cvsResource.isManaged()) {
						createDeleteMarker(resource);
					} else {
						IMarker marker = getDeletionMarker(resource);
						if (marker != null)
							marker.delete();
					}
				}
			} catch (CVSException e) {
				Util.logError(Policy.bind("AddDeleteMoveListener.Error_updating_marker_state_4"), e); //$NON-NLS-1$
			} catch (CoreException e) {
				Util.logError(Policy.bind("AddDeleteMoveListener.Error_updating_marker_state_4"), e); //$NON-NLS-1$
			}
		}
	}
	
	private static void refreshMarkers(IResource resource) throws CoreException {
		final List resources = new ArrayList();
		clearCVSMarkers(resource);
		resource.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if(resource.getType() != IResource.PROJECT) { 
					resources.add(resource);
				}
				return true;
			}
		}, IResource.DEPTH_INFINITE, true /*include phantoms*/);
		createNecessaryMarkers((IResource[]) resources.toArray(new IResource[resources.size()]));
	}
	
	public static void clearAllCVSMarkers() throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if(RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) != null) {
				clearCVSMarkers(project);
			}
		}
	}
	
	private static void clearCVSMarkers(IResource resource) throws CoreException {
		IMarker[] markers = resource.findMarkers(CVS_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			markers[i].delete();
		}
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceModificationStateChanged(org.eclipse.core.resources.IResource[])
	 */
	public void resourceModified(IResource[] changedResources) {
		// Nothing to do here
	}
}