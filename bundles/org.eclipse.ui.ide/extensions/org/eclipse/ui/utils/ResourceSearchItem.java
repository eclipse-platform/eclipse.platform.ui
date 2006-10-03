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

package org.eclipse.ui.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.dialogs.AbstractSearchItem;

/**
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class ResourceSearchItem extends AbstractSearchItem {
	
	private IResource resource ;

	/**
	 * 
	 */
	public ResourceSearchItem(IResource resource) {
		this.resource = resource;
	}
	
	/**
	 * 
	 */
	public ResourceSearchItem(IResource resource, boolean isHistory) {
		this.resource = resource;
		if (isHistory)
			this.markAsHistory();
	}
	
	/**
	 * Get decorated resource
	 * @return decorated resource
	 */
	public IResource getResource(){
		return this.resource;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ResourceSearchItem) {
			ResourceSearchItem resourceSearchItem = (ResourceSearchItem) obj;
			return getResource().equals(resourceSearchItem.getResource());
		}
		return super.equals(obj);
	}

	public int hashCode() {
		return getResource().hashCode();
	}
	
	

}
