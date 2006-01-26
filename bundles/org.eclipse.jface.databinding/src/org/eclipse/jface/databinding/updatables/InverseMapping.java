/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.databinding.updatables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

////////////////////////////////////////////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
/* package */ final class InverseMapping {
	
    private Map elementsToAdapters = new HashMap();

    private Map adaptersToElementCollection = new HashMap();

    public InverseMapping() {
    }
    
    public boolean hasMapping(Object e) {
        return elementsToAdapters.containsKey(e);
    }
    
    public Object getValue(Object key) {
    	return elementsToAdapters.get(key);
    }
    
    /**
     * Adds the given mapping. Returns the set of keys that map onto the given value
     * @param key
     * @param value
     * @return
     */
    public Collection addMapping(Object key, Object value) {
        elementsToAdapters.put(key, value);
        
        Collection existingInverse = (Collection)adaptersToElementCollection.get(value);
        if (existingInverse == null) {
            existingInverse = new HashSet();
            adaptersToElementCollection.put(value, existingInverse);
        }
        existingInverse.add(key);
        
        return existingInverse;
    }
    
    public Collection getValues() {
    	return adaptersToElementCollection.keySet();
    }

    /**
     * Returns the set of keys that map onto the given value
     * @param value
     * @return the set of keys that map onto the given value, or the empty set if none
     */
    public Collection getInverse(Object value) {
        Collection result = (Collection) adaptersToElementCollection.get(value);
        if (result == null) {
            return Collections.EMPTY_SET;
        }
        return result;
    }

    /**
     * Removes the given element from the cache. Fills in an ElementReport to
     * indicate  
     * 
     * @param element element to remove
     * @param status ElementReport. Will contain the old value of the element and
     * the number of other domain objects that share this value.
     * @return
     */
    public Collection removeMapping(Object element) {
        Object adapter = elementsToAdapters.remove(element);
        if (adapter != null) {
            Collection elements = (Collection) adaptersToElementCollection.get(adapter);
            if (elements != null) {
                elements.remove(element);
                if (elements.isEmpty()) {
                    adaptersToElementCollection.remove(adapter);
                }
                
                return elements;
            }
        }
        
        return Collections.EMPTY_SET;
    }

    public void dispose() {
        adaptersToElementCollection.clear();
        elementsToAdapters.clear();
    }
}
