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
package org.eclipse.ui.internal.preferences;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.1
 */
public class PropertyMapUnion implements IPropertyMap {

    private Map values;
    
    private final static class PropertyInfo {
        Object value;
        boolean commonAttribute;
        
        PropertyInfo(Object value, boolean commonAttribute) {
            this.value = value;
            this.commonAttribute = commonAttribute;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        return values.keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        PropertyInfo info = (PropertyInfo)values.get(propertyId);
        
        if (info == null) {
            return null;
        }
        
        Object value = info.value;
        
        if (propertyType.isInstance(value)) {
            return value;
        }
        
        return null;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#isCommonProperty(java.lang.String)
     */
    public boolean isCommonProperty(String propertyId) {
        PropertyInfo info = (PropertyInfo)values.get(propertyId);
        
        if (info == null) {
            return false;
        }
        
        return info.commonAttribute;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return values.get(propertyId) != null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        PropertyInfo info = new PropertyInfo(newValue, true);
        
        values.put(propertyId, info);
    }

    public void addMap(IPropertyMap toAdd) {
        Set keySet = toAdd.keySet();

        // Update any existing attributes
        for (Iterator iter = keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            
            PropertyInfo localInfo = (PropertyInfo)values.get(key);
            // Shouldn't be null, but just in case...
            if (localInfo != null) {
                // If the attribute exists in the new map
                if (toAdd.propertyExists(key)) {
                    // Determine if the value is common
	                Object value = toAdd.getValue(key, Object.class);
	                
	                if (!Util.equals(value, toAdd.getValue(key, Object.class))) {
	                    // Set the value to null if not common
	                    localInfo.value = null;
	                }
	                
	                // The attribute must be common in both the receiver and the new map to be common
	                // everywhere
	                localInfo.commonAttribute = localInfo.commonAttribute && toAdd.isCommonProperty(key);
                } else {
                    // If the attribute doesn't exist in the new map, it cannot be common
                    localInfo.commonAttribute = false;
                }
            }
        }
        
        // Add any new attributes that exist in the target
        for (Iterator iter = keySet.iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            
            PropertyInfo localInfo = (PropertyInfo)values.get(element);
            if (localInfo == null) {
                Object value = toAdd.getValue(element, Object.class);
                
                boolean isCommon = toAdd.isCommonProperty(element);
                
                localInfo = new PropertyInfo(value, isCommon);
                values.put(element, localInfo);
            }        
        }        
    }
    
    public void removeValue(String propertyId) {
        values.remove(propertyId);
    }
}
