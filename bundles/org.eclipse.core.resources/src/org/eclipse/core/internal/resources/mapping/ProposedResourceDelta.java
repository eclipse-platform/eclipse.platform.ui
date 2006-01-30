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

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Concrete implementation of IResourceDelta used for operation validation
 */
public final class ProposedResourceDelta extends PlatformObject implements IResourceDelta {

	protected static int KIND_MASK = 0xFF;

	private List children = new ArrayList();
	private IPath movedFromPath;
	private IPath movedToPath;
	private IResource resource;
	protected int status;

	public ProposedResourceDelta(IResource resource) {
		this.resource = resource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#accept(org.eclipse.core.resources.IResourceDeltaVisitor)
	 */
	public void accept(IResourceDeltaVisitor visitor) throws CoreException {
		accept(visitor, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#accept(org.eclipse.core.resources.IResourceDeltaVisitor, boolean)
	 */
	public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException {
		accept(visitor, includePhantoms ? IContainer.INCLUDE_PHANTOMS : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#accept(org.eclipse.core.resources.IResourceDeltaVisitor, int)
	 */
	public void accept(IResourceDeltaVisitor visitor, int memberFlags) throws CoreException {
		if (!visitor.visit(this))
			return;
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			ProposedResourceDelta childDelta = (ProposedResourceDelta) iter.next();
			childDelta.accept(visitor, memberFlags);
		}
	}

	/**
	 * Adds a child delta to the list of children for this delta node.
	 * @param delta
	 */
	protected void add(ProposedResourceDelta delta) {
		if (children.size() == 0 && status == 0) {
			status |= IResourceDelta.CHANGED;
		}
		children.add(delta);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#findMember(org.eclipse.core.runtime.IPath)
	 */
	public IResourceDelta findMember(IPath path) {
		int segmentCount = path.segmentCount();
		if (segmentCount == 0)
			return this;

		//iterate over the path and find matching child delta
		ProposedResourceDelta current = this;
		segments: for (int i = 0; i < segmentCount; i++) {
			List currentChildren = current.children;
			for (Iterator iter = currentChildren.iterator(); iter.hasNext();) {
				ProposedResourceDelta child = (ProposedResourceDelta) iter.next();
				if (child.getFullPath().lastSegment().equals(path.segment(i))) {
					current = child;
					continue segments;
				}
			}
			//matching child not found, return
			return null;
		}
		return current;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getAffectedChildren()
	 */
	public IResourceDelta[] getAffectedChildren() {
		return getAffectedChildren(ADDED | REMOVED | CHANGED, IResource.NONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getAffectedChildren(int)
	 */
	public IResourceDelta[] getAffectedChildren(int kindMask) {
		return getAffectedChildren(kindMask, IResource.NONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getAffectedChildren(int, int)
	 */
	public IResourceDelta[] getAffectedChildren(int kindMask, int memberFlags) {
		List result = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			ProposedResourceDelta child = (ProposedResourceDelta) iter.next();
			if ((child.getKind() & kindMask) != 0)
				result.add(child);
		}
		return (IResourceDelta[]) result.toArray(new IResourceDelta[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getFlags()
	 */
	public int getFlags() {
		return status & ~KIND_MASK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getFullPath()
	 */
	public IPath getFullPath() {
		return getResource().getFullPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getKind()
	 */
	public int getKind() {
		return status & KIND_MASK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getMarkerDeltas()
	 */
	public IMarkerDelta[] getMarkerDeltas() {
		return new IMarkerDelta[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getMovedFromPath()
	 */
	public IPath getMovedFromPath() {
		return movedFromPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getMovedToPath()
	 */
	public IPath getMovedToPath() {
		return movedToPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getProjectRelativePath()
	 */
	public IPath getProjectRelativePath() {
		return getResource().getProjectRelativePath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDelta#getResource()
	 */
	public IResource getResource() {
		return resource;
	}
	
	protected void setKind(int kind) {
		status = getFlags() | (kind & KIND_MASK);
	}

	protected void setMovedFromPath(IPath path) {
		movedFromPath = path;
	}

	protected void setMovedToPath(IPath path) {
		movedToPath = path;
	}

}
