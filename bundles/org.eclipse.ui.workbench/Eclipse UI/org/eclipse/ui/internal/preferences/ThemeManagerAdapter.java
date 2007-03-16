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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.1
 */
public class ThemeManagerAdapter extends PropertyMapAdapter {

    private IThemeManager manager;
    
    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event.getProperty());
        }
    };
    
    public ThemeManagerAdapter(IThemeManager manager) {
        this.manager = manager;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#attachListener()
     */
    protected void attachListener() {
        manager.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#detachListener()
     */
    protected void detachListener() {
        manager.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#keySet()
     */
    public Set keySet() {
        Set result = ThemeAdapter.getKeySet(manager.getCurrentTheme());
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        return ThemeAdapter.getValue(manager.getCurrentTheme(), propertyId, propertyType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return keySet().contains(propertyId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String, java.lang.Object)
     */
    public void setValue(String propertyId, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
