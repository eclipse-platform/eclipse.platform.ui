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
package org.eclipse.jface.viewers.deferred;

import java.util.HashMap;

/**
 * Represents a map of objects onto ints. This is intended for future optimization:
 * using int primitives would allow for an implementation that doesn't require
 * additional object allocations for Integers. However, the current implementation
 * simply delegates to the Java HashMap class. 
 * 
 * @since 3.1
 */
class IntHashMap {
    private HashMap map; 
    
    public IntHashMap(int size, float loadFactor) {
        map = new HashMap(size, loadFactor);
    }
    
    public IntHashMap() {
        map = new HashMap();
    }
    
    public void remove(Object key) {
        map.remove(key);
    }
    
    public void put(Object key, int value) {
        map.put(key, new Integer(value));
    }
    
    public int get(Object key) {
        return get(key, 0);
    }
    
    public int get(Object key, int defaultValue) {
        Integer result = (Integer)map.get(key);
        
        if (result != null) {
            return result.intValue();
        }
        
        return defaultValue;
    }
    
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    public int size() {
    	return map.size();
    }
}
