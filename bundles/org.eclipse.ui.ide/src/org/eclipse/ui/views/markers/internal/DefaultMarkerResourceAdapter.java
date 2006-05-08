/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * The DefaultMarkerResourceAdapter is the default
 * implementation of the IMarkerResourceAdapter used by the
 * MarkerView for resource adaption.
 */
class DefaultMarkerResourceAdapter implements ITaskListResourceAdapter {

    private static ITaskListResourceAdapter singleton;

    /**
     * Constructor for DefaultMarkerResourceAdapter.
     */
    DefaultMarkerResourceAdapter() {
        super();
    }

    /**
     * Return the default instance used for MarkerView adapting.
     */
    static ITaskListResourceAdapter getDefault() {
        if (singleton == null) {
			singleton = new DefaultMarkerResourceAdapter();
		}
        return singleton;
    }

    /**
     * @see IMarkerResourceAdapter#getAffectedResource(IAdaptable)
     */
    public IResource getAffectedResource(IAdaptable adaptable) {
        IResource resource = (IResource) adaptable.getAdapter(IResource.class);

        if (resource == null) {
			return (IFile) adaptable.getAdapter(IFile.class);
		} else {
			return resource;
		}
    }
}
