/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ResourceDescription is a lightweight description that describes the common
 * attributes of a resource to be created.
 * 
 * This class is not intended to be instantiated or used by clients.
 * 
 * @since 3.3
 * 
 */
public abstract class ResourceDescription {
	IContainer parent;

	long modificationStamp = IResource.NULL_STAMP;

	long localTimeStamp = IResource.NULL_STAMP;

	ResourceAttributes resourceAttributes;

	MarkerDescription[] markerDescriptions;

	/**
	 * Create a resource description given the specified resource. The resource
	 * is assumed to exist.
	 * 
	 * @param resource
	 *            the resource from which a description should be created
	 * @return the resource description
	 */
	public static ResourceDescription fromResource(IResource resource) {
		if (resource.getType() == IResource.PROJECT) {
			return new ProjectDescription((IProject) resource);
		} else if (resource.getType() == IResource.FOLDER) {
			return new FolderDescription((IFolder) resource);
		} else if (resource.getType() == IResource.FILE) {
			return new FileDescription((IFile) resource);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Create a resource description with no initial attributes
	 */
	protected ResourceDescription() {
		super();
	}

	/**
	 * Create a resource description from the specified resource.
	 * 
	 * @param resource
	 *            the resource to be described
	 */
	protected ResourceDescription(IResource resource) {
		super();
		parent = resource.getParent();
		if (resource.isAccessible()) {
			modificationStamp = resource.getModificationStamp();
			localTimeStamp = resource.getLocalTimeStamp();
			resourceAttributes = resource.getResourceAttributes();
			try {
				IMarker[] markers = resource.findMarkers(null, true,
						IResource.DEPTH_INFINITE);
				markerDescriptions = new MarkerDescription[markers.length];
				for (int i = 0; i < markers.length; i++) {
					markerDescriptions[i] = new MarkerDescription(markers[i]);
				}
			} catch (CoreException e) {
				// Eat this exception because it only occurs when the resource
				// does not exist and we have already checked this.
				// We do not want to throw exceptions on the simple constructor,
				// as no one has actually tried to do anything yet.
			}
		}
	}

	/**
	 * Create a resource handle that can be used to create a resource from this
	 * resource description. This handle can be used to create the actual
	 * resource, or to describe the creation to a resource delta factory.
	 * 
	 * @return the resource handle that can be used to create a resource from
	 *         this description
	 */
	public abstract IResource createResourceHandle();

	/**
	 * Get the name of this resource.
	 * 
	 * @return the name of the Resource
	 */
	public abstract String getName();

	/**
	 * Create an existent resource from this resource description.
	 * 
	 * @param monitor
	 *            the progress monitor to use
	 * @return a resource that has the attributes of this resource description
	 * @throws CoreException
	 */
	public IResource createResource(IProgressMonitor monitor)
			throws CoreException {
		IResource resource = createResourceHandle();
		createExistentResourceFromHandle(resource, monitor);
		restoreResourceAttributes(resource);
		return resource;
	}

	/**
	 * Given a resource handle, create an actual resource with the attributes of
	 * the receiver resource description.
	 * 
	 * @param resource
	 *            the resource handle
	 * @param monitor
	 *            the progress monitor to be used when creating the resource
	 * @throws CoreException
	 */
	public abstract void createExistentResourceFromHandle(IResource resource,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a boolean indicating whether this resource description has enough
	 * information to create a resource.
	 * 
	 * @return <code>true</code> if the resource can be created, and
	 *         <code>false</code> if it does not have enough information
	 */
	public boolean isValid() {
		return parent == null || parent.exists();
	}

	/**
	 * Record the appropriate state of this resource description using
	 * any available resource history.
	 * 
	 * @param resource
	 *            the resource whose state is to be recorded.
	 * @param monitor
	 *            the progress monitor to be used
	 * @throws CoreException
	 */
	public abstract void recordStateFromHistory(IResource resource,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Restore any saved attributed of the specified resource. This method is
	 * called after the existent resource represented by the receiver has been
	 * created.
	 * 
	 * @param resource
	 *            the newly created resource
	 * @throws CoreException
	 */
	protected void restoreResourceAttributes(IResource resource) throws CoreException {
		if (modificationStamp != IResource.NULL_STAMP) {
			resource.revertModificationStamp(modificationStamp);
		}
		if (localTimeStamp != IResource.NULL_STAMP) {
			resource.setLocalTimeStamp(localTimeStamp);
		}
		if (resourceAttributes != null) {
			resource.setResourceAttributes(resourceAttributes);
		}
		if (markerDescriptions != null) {
			for (int i = 0; i < markerDescriptions.length; i++) {
				markerDescriptions[i].resource = resource;
				markerDescriptions[i].createMarker();
			}
		}
	}

	/*
	 * Return the workspace.
	 */
	IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Return a boolean indicating whether this description represents an
	 * existent resource.
	 * 
	 * @param checkMembers
	 *            Use <code>true</code> if members should also exist in order
	 *            for this description to be considered existent. A value of
	 *            <code>false</code> indicates that the existence of members
	 *            does not matter.
	 * 
	 * @return a boolean indicating whether this description represents an
	 *         existent resource.
	 */
	public boolean verifyExistence(boolean checkMembers) {
		IContainer p = parent;
		if (p == null) {
			p = getWorkspace().getRoot();
		}
		IResource handle = p.findMember(getName());
		return handle != null;
	}
}