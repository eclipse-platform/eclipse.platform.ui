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

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;


/**
 * @since 3.1
 */
public final class PreferenceStoreDefaultAdapter implements IPropertyMap {
    
    private IPreferenceStore store;
    
    public PreferenceStoreDefaultAdapter(IPreferenceStore toConvert) {
        this.store = toConvert;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        if (propertyType.isAssignableFrom(String.class)) {
            return store.getDefaultString(propertyId);
        }
        
        if (propertyType == Boolean.class) {
            return new Boolean(store.getDefaultBoolean(propertyId));
        }
        
        if (propertyType == Double.class) {
            return new Double(store.getDefaultDouble(propertyId));
        }
        
        if (propertyType == Float.class) {
            return new Float(store.getDefaultFloat(propertyId));
        }
        
        if (propertyType == Integer.class) {
            return new Integer(store.getDefaultInt(propertyId));
        }
        
        if (propertyType == Long.class) {
            return new Long(store.getDefaultLong(propertyId));
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
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        if (newValue instanceof String) {
            store.setDefault(propertyId, (String)newValue);
        } else if (newValue instanceof Integer) {
            store.setDefault(propertyId, ((Integer)newValue).intValue());
        } else if (newValue instanceof Boolean) {
            store.setDefault(propertyId, ((Boolean)newValue).booleanValue());
        } else if (newValue instanceof Double) {
            store.setDefault(propertyId, ((Double)newValue).doubleValue());
        } else if (newValue instanceof Float) {
            store.setDefault(propertyId, ((Float)newValue).floatValue());
        } else if (newValue instanceof Integer) {
            store.setDefault(propertyId, ((Integer)newValue).intValue());
        } else if (newValue instanceof Long) {
            store.setDefault(propertyId, ((Long)newValue).longValue());
        }
    }
}
