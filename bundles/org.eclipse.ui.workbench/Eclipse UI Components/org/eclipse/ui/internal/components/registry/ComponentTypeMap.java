/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.components.ClassIdentifier;

/**
 * @since 3.1
 */
public class ComponentTypeMap {
   
    /**
     * map of String (fully qualified class names) onto List. The Lists cont
     */
    private Map types = new HashMap();
    private Map keys = new HashMap();
    
    public void put(ClassIdentifier key, Object value) {
        types.put(key.getTypeName(), value);
        keys.put(key.getTypeName(), key);
    }
    
    public Object get(Class key) {
        return types.get(key.getName());
    }
    
    public Object get(ClassIdentifier key) {
        return types.get(key.getTypeName());
    }
    
    public void remove(ClassIdentifier key) {
        types.remove(key.getTypeName());
        keys.remove(key.getTypeName());
    }
    
    public ClassIdentifier[] getTypes() {
        Collection keys = this.keys.values();
        
        return (ClassIdentifier[]) keys.toArray(new ClassIdentifier[keys.size()]);
    }

    public boolean containsKey(Class key) {
    	return types.containsKey(key.getName());
    }
    
    /**
     * @since 3.1 
     *
     * @return
     */
    public boolean isEmpty() {
        return types.isEmpty();
    }
    
}
