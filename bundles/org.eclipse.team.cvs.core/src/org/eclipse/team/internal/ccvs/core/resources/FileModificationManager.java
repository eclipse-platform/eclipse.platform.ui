/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;

/**
 * This class performs several functions related to determining the modified
 * status of files under CVS control. First, it listens for change delta's for
 * files and brodcasts them to all listeners. It also registers as a save
 * participant so that deltas generated before the plugin are loaded are not
 * missed. Secondly, it listens for CVS resource state change events and uses
 * these to properly mark files and folders as modified.
 */
public class FileModificationManager implements IResourceChangeListener, ISaveParticipant {
		
	private Set modifiedResources = new HashSet();

	// consider the following changes types and ignore the others (e.g. marker and description changes are ignored)
	protected int INTERESTING_CHANGES = 	IResourceDelta.CONTENT | 
																	IResourceDelta.MOVED_FROM | 
																	IResourceDelta.MOVED_TO |
																	IResourceDelta.OPEN | 
																	IResourceDelta.REPLACED |
																	IResourceDelta.TYPE;

	/**
	 * Listen for file modifications and fire modification state changes
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					
					if (resource.getType()==IResource.PROJECT) {
						IProject project = (IProject)resource;
						if (!project.isAccessible()) {
							return false;
						}
						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
							return false;
						} 
						if (RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) == null) {
							return false;
						}
					}
					
					if (resource.getType()==IResource.FILE && delta.getKind() == IResourceDelta.CHANGED && resource.exists()) {
						int flags = delta.getFlags();
						if((flags & INTERESTING_CHANGES) != 0) {
							contentsChanged((IFile)resource);
						}
					} else if (delta.getKind() == IResourceDelta.ADDED) {
						resourceAdded(resource);
					} else if (delta.getKind() == IResourceDelta.REMOVED) {
						// provide notifications for deletions since they may not have been managed
						// The move/delete hook would have updated the parent counts properly
						modifiedResources.add(resource);
					}

					return true;
				}
			});
			if (!modifiedResources.isEmpty()) {
				CVSProviderPlugin.broadcastModificationStateChanges(
					(IResource[])modifiedResources.toArray(new IResource[modifiedResources.size()]));
				modifiedResources.clear();
			}
		} catch (CoreException e) {
			CVSProviderPlugin.log(e.getStatus());
		}

	}
	
	/**
	 * We register a save participant so we can get the delta from workbench
	 * startup to plugin startup.
	 * @throws CoreException
	 */
	public void registerSaveParticipant() throws CoreException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISavedState ss = ws.addSaveParticipant(CVSProviderPlugin.getPlugin(), this);
		if (ss != null) {
			ss.processResourceChangeEvents(this);
		}
		ws.removeSaveParticipant(CVSProviderPlugin.getPlugin());
	}
	
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {
	}
	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
	}

	
	/**
	 * Method updated flags the objetc as having been modfied by the updated
	 * handler. This flag is read during the resource delta to determine whether
	 * the modification made the file dirty or not.
	 * 
	 * @param mFile
	 */
	public void updated(ICVSFile mFile) throws CVSException {
		if (mFile instanceof EclipseFile)
			((EclipseFile)mFile).updated();
	}
	
	public void contentsChanged(IFile file) throws CoreException {
		try {
			EclipseFile cvsFile = (EclipseFile)CVSWorkspaceRoot.getCVSFileFor(file);
			cvsFile.handleModification(false /* addition */);
			modifiedResources.add(file);
		} catch (CVSException e) {
			throw e.toCoreException();
		}
	}
	
	public void created(IResource resource) throws CVSException {
		EclipseResource cvsResource = (EclipseResource)CVSWorkspaceRoot.getCVSResourceFor(resource);
		cvsResource.created();
	}
	
	/*
	 * Handle an added resource.
	 */
	private void resourceAdded(IResource resource) throws CoreException {
		try {
			EclipseResource cvsResource = (EclipseResource)CVSWorkspaceRoot.getCVSResourceFor(resource);
			cvsResource.handleModification(true /* addition */);
			modifiedResources.add(resource);
		} catch (CVSException e) {
			throw e.toCoreException();
		}
	}
}

