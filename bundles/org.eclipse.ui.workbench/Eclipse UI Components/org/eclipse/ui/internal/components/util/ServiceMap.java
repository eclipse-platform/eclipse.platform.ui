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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;

/**
 * Basic service provider implementation that provides a set of explicitly
 * registered services instances. Optionally delegates to another service
 * provider for any services that aren't explicitly provided by this provider.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class ServiceMap implements IServiceProvider {

    /**
     * Parent provider (or null if none). This provider will look to its parent if 
     * a component can't be found locally.
     */
    private IServiceProvider parent = null;
    
    /**
     * Map keys onto component instances (null if empty)
     */
    private Map overriddenInstances = null;
	
    /**
     * Creates a new service provider that initially provides no services
     */
    public ServiceMap() {
    }
    
    /**
     * Creates a new service provider that modifies the given parent provider.
     * 
     * @param parent parent context to modify
     */
    public ServiceMap(IServiceProvider parent) {
        Assert.isNotNull(parent);
        this.parent = parent;
    }
    
    /* (non-javadoc)
     * 
     */
	public Object getService(Object key) throws ComponentException {
        if (overriddenInstances != null && overriddenInstances.containsKey(key)) {
            return overriddenInstances.get(key);
        }
     
        if (parent != null) {
            return parent.getService(key);
        }
        
        return null;
	}
    
	/**
     * Maps the given key to the given service instance
     * 
     * @param key service key
     * @param instance service instance
     * @return this
	 */
    public ServiceMap map(Object key, Object instance) {
        if (overriddenInstances == null) {
            overriddenInstances = new HashMap();
        }
        
        overriddenInstances.put(key, instance);
        
        return this;
    }
    
    /**
     * Removes a previous mapping for the given service key 
     *
     * @param key key to unmap
     * @return this
     */
    public ServiceMap unmap(Object key) {
        if (overriddenInstances == null) {
            return this;
        }
        
        overriddenInstances.remove(key);
        
        if (overriddenInstances.isEmpty()) {
            overriddenInstances = null;
        }
        
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        if (overriddenInstances != null && overriddenInstances.containsKey(key)) {
            return overriddenInstances.get(key) != null;
        }
     
        if (parent != null) {
            return parent.hasService(key);
        }
        
        return false;
    }    
}
