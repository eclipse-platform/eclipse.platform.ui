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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.NonDisposingHandle;
import org.eclipse.ui.internal.components.framework.ServiceFactory;

/**
 * Converts an existing POJO into an AbstractServiceFactory that always returns
 * handles to the original object when asked for a Class that the object 
 * implements. If the object implements IAdaptable, the factory will also
 * return handles to the object's adapters.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public class InstanceToServiceFactoryAdapter extends ServiceFactory {
    private Object originalObject;
    private ComponentHandle handleForOriginalObject;
    
    /**
     * Constructs a service factory that always returns adapters to the
     * given object.
     * 
     * @param originalObject
     */
    public InstanceToServiceFactoryAdapter(Object originalObject) {
        this.handleForOriginalObject = new NonDisposingHandle(originalObject);
        this.originalObject = originalObject;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getHandle(java.lang.Object, org.eclipse.core.components.IComponentProvider)
     */
    public ComponentHandle createHandle(Object componentKey,
            IServiceProvider container) {
        
        if (componentKey instanceof Class) {
            Class c = (Class) componentKey;
            
            if (c.isInstance(originalObject)) {
                return handleForOriginalObject;
            }
            
            if (originalObject instanceof IAdaptable) {
                Object adapter = ((IAdaptable)originalObject).getAdapter(c);
                
                if (adapter != null) {
                    return new NonDisposingHandle(adapter);
                }
            }
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {
        if (componentKey instanceof Class) {
            Class c = (Class) componentKey;
            
            if (c.isInstance(originalObject)) {
                return true;
            }
            
            if (originalObject instanceof IAdaptable) {
                Object adapter = ((IAdaptable)originalObject).getAdapter(c);
                
                if (adapter != null) {
                    return true;
                }
            }
        }
        
        return false;    
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        return Collections.EMPTY_SET;
    }
}
