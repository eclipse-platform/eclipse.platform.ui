/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.util;

 
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * Cleanup any CVS folders that were copied by a builder. This will also clean up
 * CVS folders that were copied by the user since the last auto-build.
 */
public class BuildCleanupListener implements IResourceDeltaVisitor, IResourceChangeListener {
	
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
		boolean movedFrom = (delta.getFlags() & IResourceDelta.MOVED_FROM) > 0;
		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				// make sure the added resource isn't a phantom
				if (resource.exists() && resource.getType() == IResource.FOLDER) {
					handleOrphanedSubtree((IContainer)resource);
				}	
				break;
			case IResourceDelta.CHANGED :
				// This state means there is a resource before and after but changes were made by deleting and moving.
				// For files, we shouldn'd do anything.
				// For folders, we should purge the CVS info
				if (movedFrom && resource.getType() == IResource.FOLDER && resource.exists()) {
					// When folders are moved, purge the CVS folders
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
	private boolean handleOrphanedSubtree(IContainer container) {
		try {
			ICVSFolder mFolder = CVSWorkspaceRoot.getCVSFolderFor(container);
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
						CVSProviderPlugin.log(e);
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
								delta.accept(BuildCleanupListener.this);
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

}
