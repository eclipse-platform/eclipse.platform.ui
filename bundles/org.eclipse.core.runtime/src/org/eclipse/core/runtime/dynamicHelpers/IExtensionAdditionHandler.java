/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.dynamicHelpers;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

/**
 * IExtensionAdditionHandler is notified of changes for a given extension point in the context 
 * of an extension tracker.
 * 
 * This API is EXPERIMENTAL and provided as early access.
 * @since 3.1
 */
public interface IExtensionAdditionHandler {

    /**
     * This method is being called whenever an extesion conforming to the extension point filter
     * is being added to the registry.
     * This method does not automatically register objects to the tracker.     
     * @param tracker : a tracker to which the handler has been registered.
     * @param extension : the extension being added. 
     */
	public void addInstance(IExtensionTracker tracker, IExtension extension);
    
    /**
     * Return the extension point in which the addition handler is interested. 
     * When <code>null</code> is returned, no filtering is done and the handler will be notified of the addition of any extension.
     * @return an extension point or <code>null</code>.
     */
    public IExtensionPoint getExtensionPointFilter();
}
