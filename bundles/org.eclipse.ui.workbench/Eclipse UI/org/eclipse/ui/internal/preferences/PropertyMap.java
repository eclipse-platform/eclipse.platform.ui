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
package org.eclipse.ui.internal.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since 3.1
 */
public final class PropertyMap implements IPropertyMap {

    private Map map;
    
    public PropertyMap() {
        this(new HashMap());
    }
    
    public PropertyMap(Map underlyingMap) {
        map = underlyingMap;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        return map.keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        Object result = map.get(propertyId);
        
        if (propertyType.isInstance(result)) {
            return result;
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#isCommonProperty(java.lang.String)
     */
    public boolean isCommonProperty(String propertyId) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return map.containsKey(propertyId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        map.put(propertyId, newValue);
    }
    
    public void removeValue(String propertyId) {
        map.remove(propertyId);
    }
    
    public boolean equals(Object toCompare) {
        return toCompare instanceof IPropertyMap && PropertyUtil.isEqual(this, (IPropertyMap)toCompare);
    }
}
