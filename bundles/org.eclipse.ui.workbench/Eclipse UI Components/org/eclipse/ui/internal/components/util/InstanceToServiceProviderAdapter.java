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
package org.eclipse.ui.internal.components.util;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.internal.components.framework.IServiceProvider;

/**
 * Converts an existing POJO into an IComponentProvider that returns the object itself
 * whenever asked for a class that the POJO implements. If the object implements IAdaptable,
 * it will also return adapters from the object.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class InstanceToServiceProviderAdapter implements IServiceProvider {
    private Object existingObject;
    
    public InstanceToServiceProviderAdapter(Object existingObject) {
        this.existingObject = existingObject;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#getComponent(java.lang.Object)
     */
    public Object getService(Object key) {
        if (key instanceof Class) {
            Class c = (Class)key;
            
            if (c.isInstance(existingObject)) {
                return existingObject;
            }
            
            if (existingObject instanceof IAdaptable) {
                return ((IAdaptable)existingObject).getAdapter(c);
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return getService(key) != null;
    }
}
