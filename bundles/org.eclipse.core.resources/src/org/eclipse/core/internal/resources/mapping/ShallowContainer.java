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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A special model object used to represent shallow folders
 */
public class ShallowContainer extends PlatformObject {
	
	private IContainer container;
	
	public ShallowContainer(IContainer container) {
		this.container = container;
	}

	public IContainer getResource() {
		return container;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof ShallowContainer) {
			ShallowContainer other = (ShallowContainer) obj;
			return other.getResource().equals(getResource());
		}
		return false;
	}
	
	public int hashCode() {
		return getResource().hashCode();
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class || adapter == IContainer.class)
			return container;
		return super.getAdapter(adapter);
	}

}
