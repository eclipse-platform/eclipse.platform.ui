/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.internal.resources.mapping.ResourceModelProvider;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.internal.provisional.ide.IEditorOpenStrategy;

/**
 * Adapter factory which provides an editor open strategy for the resource model.
 */
public class ModelProviderAdapterFactory implements IAdapterFactory {
	
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
    	// TODO fix up internal ref to ResourceModelProvider
        if (adaptableObject instanceof ResourceModelProvider) {
            if (adapterType == IEditorOpenStrategy.class) {
                return new ResourceEditorOpenStrategy();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] { IEditorOpenStrategy.class };
    }
    
}
