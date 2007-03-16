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

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * <code>ITaskListResourceAdapter</code> is an adapter interface that
 * supplies the resource to query for markers to display in the task list.
 * <p>
 * The Tasks view checks for this adapter before the <code>IResource</code>
 * adapter, allowing the object to provide a resource to the Tasks view without
 * necessarily exposing it to other components that look for an <code>IResource</code>
 * adapter.     
 * </p>
 * <p>
 * Implementors of this interface are typically registered with an
 * IAdapterFactory for lookup via the getAdapter() mechanism.
 * </p>
 */
public interface ITaskListResourceAdapter {

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
