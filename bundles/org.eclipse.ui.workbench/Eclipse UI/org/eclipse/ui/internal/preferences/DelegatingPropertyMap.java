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

/**
 * Creates a wrapper around another given map. All methods delegate to the given
 * map, however this object maintains its own localized listener list. This can
 * be used to protect global property maps from listener leaks. For example, assume
 * that some client object needs access to some global property map. Instead of
 * giving the client object direct access to the global map, wrap the global map
 * in a DelegatingPropertyMap and give the client code access to the wrapper instead.
 * When it comes time to destroy the client object, dispose the wrapper. This will
 * remove all listeners which may have been attached by the client code.
 * 
 * @since 3.1
 */
public class DelegatingPropertyMap extends PropertyMapAdapter {

    private IDynamicPropertyMap realMap;
    
    private IPropertyMapListener listener = new IPropertyMapListener() {
        public void propertyChanged(String[] preferenceIds) {
            firePropertyChange(preferenceIds);
        }

        public void listenerAttached() {
        }
    };
    
    public DelegatingPropertyMap(IDynamicPropertyMap originalMap) {
        this.realMap = originalMap;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#attachListener()
     */
    protected void attachListener() {
        realMap.addListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#detachListener()
     */
    protected void detachListener() {
        realMap.removeListener(listener);    
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        return realMap.keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        return realMap.getValue(propertyId, propertyType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return realMap.propertyExists(propertyId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        realMap.setValue(propertyId, newValue);
    }

}
