/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.undo.snapshot.IResourceSnapshot;
import org.eclipse.core.resources.undo.snapshot.ResourceSnapshotFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ResourceDescription is a lightweight description that describes the common
 * attributes of a resource to be created.
 *
 * This class is not intended to be extended by clients.
 *
 * @since 3.3
 * @deprecated Replaced by IResourceSnapshot and ResourceSnapshotFactory in the
 *             org.eclipse.core.resources.undo.snapshot package
 */
@Deprecated
public abstract class ResourceDescription {

	/**
	 * Create a resource description given the specified resource. The resource is
	 * assumed to exist.
	 *
	 * @param resource the resource from which a description should be created
	 * @return the resource description
	 */
	public static ResourceDescription fromResource(IResource resource) {
		IResourceSnapshot<IResource> delegate = ResourceSnapshotFactory.fromResource(resource);
		return new ResourceDescription() {

			@Override
			public IResource createResourceHandle() {
				return delegate.createResourceHandle();
			}

			@Override
			public String getName() {
				return delegate.getName();
			}

			@Override
			public IResource createResource(IProgressMonitor monitor) throws CoreException {
				return delegate.createResource(monitor);
			}

			@Override
			public void createExistentResourceFromHandle(IResource resource, IProgressMonitor monitor)
					throws CoreException {
				delegate.createExistentResourceFromHandle(monitor);
			}

			@Override
			public boolean isValid() {
				return delegate.isValid();
			}

			@Override
			public void recordStateFromHistory(IResource resource, IProgressMonitor monitor) throws CoreException {
				delegate.recordStateFromHistory(monitor);
			}

			@Override
			public boolean verifyExistence(boolean checkMembers) {
				return delegate.verifyExistence(checkMembers);
			}
		};
	}

	/**
	 * Create a resource handle that can be used to create a resource from this
	 * resource description. This handle can be used to create the actual resource,
	 * or to describe the creation to a resource delta factory.
	 *
	 * @return the resource handle that can be used to create a resource from this
	 *         description
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
	 * @param monitor the progress monitor to use
	 * @return a resource that has the attributes of this resource description
	 * @throws CoreException if creation failed
	 */
	public abstract IResource createResource(IProgressMonitor monitor) throws CoreException;

	/**
	 * Given a resource handle, create an actual resource with the attributes of the
	 * receiver resource description.
	 *
	 * @param resource the resource handle
	 * @param monitor  the progress monitor to be used when creating the resource
	 * @throws CoreException if creation failed
	 */
	public abstract void createExistentResourceFromHandle(IResource resource, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Return a boolean indicating whether this resource description has enough
	 * information to create a resource.
	 *
	 * @return <code>true</code> if the resource can be created, and
	 *         <code>false</code> if it does not have enough information
	 */
	public abstract boolean isValid();

	/**
	 * Record the appropriate state of this resource description using any available
	 * resource history.
	 *
	 * @param resource the resource whose state is to be recorded.
	 * @param monitor  the progress monitor to be used
	 * @throws CoreException in case of error
	 */
	public abstract void recordStateFromHistory(IResource resource, IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a boolean indicating whether this description represents an existent
	 * resource.
	 *
	 * @param checkMembers Use <code>true</code> if members should also exist in
	 *                     order for this description to be considered existent. A
	 *                     value of <code>false</code> indicates that the existence
	 *                     of members does not matter.
	 *
	 * @return a boolean indicating whether this description represents an existent
	 *         resource.
	 */
	public abstract boolean verifyExistence(boolean checkMembers);
}

