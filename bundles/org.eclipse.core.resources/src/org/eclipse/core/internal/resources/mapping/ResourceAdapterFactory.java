/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory converting IResource to ResourceMapping
 * 
 * @since 3.1
 */
public class ResourceAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * Method declared on IAdapterFactory
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == ResourceMapping.class && adaptableObject instanceof IResource) {
			return new SimpleResourceMapping((IResource) adaptableObject);
		}
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IAdapterFactory
	 */
	public Class[] getAdapterList() {
		return new Class[] {ResourceMapping.class};
	}
}
