/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.internal.core.Assert;

/**
 * DiffNode that represents a resource that is in sync.
 */
public class UnchangedResourceModelElement extends SynchronizeModelElement implements IAdaptable {

	private IResource resource;

	public UnchangedResourceModelElement(IDiffContainer parent, IResource resource) {
		super(parent);
		Assert.isNotNull(resource);
		this.resource = resource;
	}
	
	/**
	 * @return Returns the resource.
	 */
	public IResource getResource() {
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getName()
	 */
	public String getName() {
		return resource.getName();
	}
}
