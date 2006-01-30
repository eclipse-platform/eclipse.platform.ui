/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.*;

/**
 * Factory for creating a resource delta that describes a proposed change.
 */
public class ResourceChangeDescriptionFactory implements IResourceChangeDescriptionFactory {

	private ProposedResourceDelta root = new ProposedResourceDelta(ResourcesPlugin.getWorkspace().getRoot());

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#change(org.eclipse.core.resources.IFile)
	 */
	public void change(IFile file) {
		ProposedResourceDelta delta = getDelta(file);
		delta.setKind(IResourceDelta.CHANGED);
		delta.status |= IResourceDelta.CONTENT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#close(org.eclipse.core.resources.IProject)
	 */
	public void close(IProject project) {
		delete(project);
		ProposedResourceDelta delta = getDelta(project);
		delta.status |= IResourceDelta.OPEN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#copy(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IPath)
	 */
	public void copy(IResource resource, IPath destination) {
		moveOrCopy(resource, destination, false /* copy */);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory#create(org.eclipse.core.resources.IResource)
	 */
	public void create(IResource resource) {
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource child) {
					ProposedResourceDelta delta = getDelta(child);
					delta.setKind(IResourceDelta.ADDED);
					return true;
				}
			});
		} catch (CoreException e) {
			fail(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#delete(org.eclipse.core.resources.IResource)
	 */
	public void delete(IResource resource) {
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource child) {
					ProposedResourceDelta delta = getDelta(child);
					delta.setKind(IResourceDelta.REMOVED);
					return true;
				}
			});
		} catch (CoreException e) {
			fail(e);
		}
	}

	private void fail(CoreException e) {
		ResourcesPlugin.getPlugin().getLog().log(new Status(e.getStatus().getSeverity(), ResourcesPlugin.PI_RESOURCES, 0, "An internal error occurred while accumulating a change description.", e)); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#getDelta()
	 */
	public IResourceDelta getDelta() {
		return root;
	}

	ProposedResourceDelta getDelta(IResource resource) {
		ProposedResourceDelta delta = (ProposedResourceDelta) root.findMember(resource.getFullPath());
		if (delta != null) {
			return delta;
		}
		ProposedResourceDelta parent = getDelta(resource.getParent());
		delta = new ProposedResourceDelta(resource);
		parent.add(delta);
		return delta;
	}

	/*
	 * Return the resource at the destination path that corresponds to the source resource
	 * @param source the source resource
	 * @param sourcePrefix the path of the root of the move or copy
	 * @param destinationPrefix the path of the destination the root was copied to
	 * @return the destination resource
	 */
	protected IResource getDestinationResource(IResource source, IPath sourcePrefix, IPath destinationPrefix) {
		IPath relativePath = source.getFullPath().removeFirstSegments(sourcePrefix.segmentCount());
		IPath destinationPath = destinationPrefix.append(relativePath);
		IResource destination;
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		switch (source.getType()) {
			case IResource.FILE :
				destination = wsRoot.getFile(destinationPath);
				break;
			case IResource.FOLDER :
				destination = wsRoot.getFolder(destinationPath);
				break;
			case IResource.PROJECT :
				destination = wsRoot.getProject(destinationPath.segment(0));
				break;
			default :
				// Shouldn't happen
				destination = null;
		}
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#move(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IPath)
	 */
	public void move(IResource resource, IPath destination) {
		moveOrCopy(resource, destination, true /* move */);
	}

	/*
	 * Helper method that generate a move or copy delta for each resource
	 * moved or copied
	 */
	private void moveOrCopy(IResource resource, IPath destination, final boolean move) {
		final IPath sourcePrefix = resource.getFullPath();
		final IPath destinationPrefix = destination;
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource child) {
					// First, create the delta for the source
					if (move) {
						ProposedResourceDelta sourceDelta = getDelta(child);
						sourceDelta.setKind(IResourceDelta.REMOVED);
						sourceDelta.status |= IResourceDelta.MOVED_TO;
						sourceDelta.setMovedToPath(destinationPrefix.append(child.getFullPath().removeFirstSegments(sourcePrefix.segmentCount())));
					}
					// Next, create the delta for the destination
					IResource destinationResource = getDestinationResource(child, sourcePrefix, destinationPrefix);
					ProposedResourceDelta destinationDelta = getDelta(destinationResource);
					destinationDelta.setKind(IResourceDelta.ADDED);
					destinationDelta.status |= move ? IResourceDelta.MOVED_FROM : IResourceDelta.COPIED_FROM;
					destinationDelta.setMovedFromPath(child.getFullPath());
					return true;
				}
			});
		} catch (CoreException e) {
			fail(e);
		}
	}
}
