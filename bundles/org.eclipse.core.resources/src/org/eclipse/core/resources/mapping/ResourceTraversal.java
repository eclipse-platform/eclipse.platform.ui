/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * A resource traversal is simply a set of resources and the depth to which
 * each is to be traversed. A set of traversals is used to describe the
 * resources that constitute a model element.
 * <p>
 * The flags of the traversal indicate which special resources should be
 * included or excluded from the traversal. The flags used are the same as
 * those passed to the <code>IResource#accept(IResourceVisitor, int, int)</code> method.
 * 
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p>

 * @see org.eclipse.core.resources.IResource
 * @since 3.1
 */
public class ResourceTraversal {

	private int depth;
	private int flags;
	private IResource[] resources;

	/**
	 * Creates a new resource traversal.
	 * @param resources The resources in the traversal
	 * @param depth The traversal depth
	 * 
	 * @deprecated Use {@link #ResourceTraversal(IResource[], int, int)} instead.
	 */
	public ResourceTraversal(IResource[] resources, int depth) {
		this(resources, depth, 0);
	}

	/**
	 * Creates a new resource traversal.
	 * @param resources The resources in the traversal
	 * @param depth The traversal depth
	 * @param flags the flags for this traversal. The traversal flags match those
	 * that are passed to the <code>IResource#accept</code> method.
	 */
	public ResourceTraversal(IResource[] resources, int depth, int flags) {
		this.resources = resources;
		this.depth = depth;
		this.flags = flags;
	}

	/**
	 * Visit the resources of this traversal.
	 * 
	 * @param visitor a resource visitor
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> A resource in this traversal does not exist.</li>
	 * <li> The visitor failed with this exception.</li>
	 * </ul>
	 */
	public void accept(IResourceVisitor visitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			resource.accept(visitor, depth, flags);
		}
	}

	/**
	 * Returns the depth to which the resources should be traversed.
	 * 
	 * @return the depth to which the physical resources are to be traversed
	 * (one of IResource.DEPTH_ZERO, IResource.DEPTH_ONE or
	 * IResource.DEPTH_INFINITE)
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Return the flags for this traversal. 
	 * The flags of the traversal indicate which special resources should be
	 * included or excluded from the traversal. The flags used are the same as
	 * those passed to the <code>IResource#accept(IResourceVisitor, int, int)</code> method.
	 * Clients who traverse the resources manually (i.e. without calling <code>accept</code>)
	 * should respect the flags when determining which resources are included
	 * in the traversal.
	 * 
	 * @return the flags for this traversal
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * Returns the file system resource(s) for this traversal. The returned
	 * resources must be contained within the same project and need not exist in
	 * the local file system. The traversal of the returned resources should be
	 * done considering the flag returned by getDepth. If a resource returned by
	 * a traversal is a file, it should always be visited. If a resource of a
	 * traversal is a folder then files contained in the folder can only be
	 * visited if the folder is IResource.DEPTH_ONE or IResource.DEPTH_INFINITE.
	 * Child folders should only be visited if the depth is
	 * IResource.DEPTH_INFINITE.
	 * 
	 * @return The resources in this traversal
	 */
	public IResource[] getResources() {
		return resources;
	}
}
