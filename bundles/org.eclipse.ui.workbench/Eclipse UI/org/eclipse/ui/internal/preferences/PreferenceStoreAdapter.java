/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 3.1
 */
public final class PreferenceStoreAdapter extends PropertyMapAdapter {

    private IPreferenceStore store;
    
    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event.getProperty());
        }
    };
    
    public PreferenceStoreAdapter(IPreferenceStore toConvert) {
        this.store = toConvert;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#attachListener()
     */
    protected void attachListener() {
        store.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#detachListener()
     */
    protected void detachListener() {
        store.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        if (propertyType.isAssignableFrom(String.class)) {
            return store.getString(propertyId);
        }
        
        if (propertyType == Boolean.class) {
            return store.getBoolean(propertyId) ? Boolean.TRUE : Boolean.FALSE;
        }
        
        if (propertyType == Double.class) {
            return new Double(store.getDouble(propertyId));
        }
        
        if (propertyType == Float.class) {
            return new Float(store.getFloat(propertyId));
        }
        
        if (propertyType == Integer.class) {
            return new Integer(store.getInt(propertyId));
        }
        
        if (propertyType == Long.class) {
            return new Long(store.getLong(propertyId));
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return store.contains(propertyId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        if (newValue instanceof String) {
            store.setValue(propertyId, (String)newValue);
        } else if (newValue instanceof Integer) {
            store.setValue(propertyId, ((Integer)newValue).intValue());
        } else if (newValue instanceof Boolean) {
            store.setValue(propertyId, ((Boolean)newValue).booleanValue());
        } else if (newValue instanceof Double) {
            store.setValue(propertyId, ((Double)newValue).doubleValue());
        } else if (newValue instanceof Float) {
            store.setValue(propertyId, ((Float)newValue).floatValue());
        } else if (newValue instanceof Integer) {
            store.setValue(propertyId, ((Integer)newValue).intValue());
        } else if (newValue instanceof Long) {
            store.setValue(propertyId, ((Long)newValue).longValue());
        }
    }

}
