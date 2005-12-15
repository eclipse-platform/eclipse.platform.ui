/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping.provider;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * Implementation of {@link IResourceDiff}.
 * <p>
 * This class may be subclassed by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class ResourceDiff extends TwoWayDiff implements IResourceDiff {

	private final IResourceVariant before;
	private final IResourceVariant after;
	private final IResource resource;

	/**
	 * Create a two-way resource diff
	 * @param resource the resource
	 * @param kind the kind of change
	 * @param flags additional flags that describe the change
	 * @param before the before state of the model object
	 * @param after the after state of the model object
	 */
	public ResourceDiff(IResource resource, int kind, int flags, IResourceVariant before, IResourceVariant after) {
		super(resource.getFullPath(), kind, flags);
		this.resource = resource;
		this.before = before;
		this.after = after;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiff#getBeforeState()
	 */
	public IResourceVariant getBeforeState() {
		return before;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiff#getAfterState()
	 */
	public IResourceVariant getAfterState() {
		return after;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiff#getResource()
	 */
	public IResource getResource() {
		return resource;
	}

}
