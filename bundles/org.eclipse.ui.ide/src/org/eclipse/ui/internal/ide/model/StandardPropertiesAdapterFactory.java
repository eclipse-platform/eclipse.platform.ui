/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

/**
 * Dispenses an <code>IPropertySource</code> adapter for the core resource objects.
 */
public class StandardPropertiesAdapterFactory implements IAdapterFactory {
    /* (non-Javadoc)
     * Method declared on IAdapterFactory.
     */
    public Object getAdapter(Object o, Class adapterType) {
        if (adapterType.isInstance(o)) {
            return o;
        }
        if (adapterType == IPropertySource.class) {
            if (o instanceof IResource) {
                IResource resource = (IResource) o;
                if (resource.getType() == IResource.FILE) {
					return new FilePropertySource((IFile) o);
				}
				return new ResourcePropertySource((IResource) o);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IAdapterFactory.
     */
    public Class[] getAdapterList() {
        return new Class[] { IPropertySource.class };
    }
}
