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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * <code>IMarkerResourceAdapter</code> is an adapter interface that
 * supplies the resource to query for markers to display in the marker view
 * or any of its subclasses.
 * 
 * Implementors of this interface are typically registered with an
 * IAdapterFactory for lookup via the getAdapter() mechanism.
 */
public interface IMarkerResourceAdapter {

    /**
     * Returns the resource to query for the markers to display
     * for the given adaptable.
     * 
     * @param adaptable the adaptable being queried.
     * @return the resource or <code>null</code> if there
     * 	is no adapted resource for this object.
     */
    public IResource getAffectedResource(IAdaptable adaptable);

}
