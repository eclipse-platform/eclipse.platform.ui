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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.1
 */
public class ThemeAdapter extends PropertyMapAdapter {

    private ITheme targetTheme;
    
    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event.getProperty());
        }
    };
    
    public ThemeAdapter(ITheme targetTheme) {
        this.targetTheme = targetTheme;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#attachListener()
     */
    protected void attachListener() {
        targetTheme.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.PropertyMapAdapter#detachListener()
     */
    protected void detachListener() {
        targetTheme.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getKeySet()
     */
    public Set keySet() {
        return getKeySet(targetTheme);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#getValue(java.lang.String, java.lang.Class)
     */
    public Object getValue(String propertyId, Class propertyType) {
        return getValue(targetTheme, propertyId, propertyType);
    }

    public static Set getKeySet(ITheme targetTheme) {
        Set result = new HashSet();
        
        result.addAll(targetTheme.keySet());
        result.addAll(targetTheme.getColorRegistry().getKeySet());
        result.addAll(targetTheme.getFontRegistry().getKeySet());
        
        return result;        
    }
    
    public static Object getValue(ITheme targetTheme, String propertyId, Class propertyType) {

        if (propertyType.isAssignableFrom(String.class)) {
            return targetTheme.getString(propertyId);
        }
        
        if (propertyType.isAssignableFrom(Color.class)) {
            Color result = targetTheme.getColorRegistry().get(propertyId);
            if (result != null) {
                return result;
            }
        }
        
        if (propertyType.isAssignableFrom(Font.class)) {
            FontRegistry fonts = targetTheme.getFontRegistry();
            
            if (fonts.hasValueFor(propertyId)) {
                return fonts.get(propertyId);
            }
        }
        
        if (propertyType == Integer.class) {
            return new Integer(targetTheme.getInt(propertyId));
        }

        if (propertyType == Boolean.class) {
            return targetTheme.getBoolean(propertyId) ? Boolean.TRUE : Boolean.FALSE;
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String propertyId) {
        return keySet().contains(propertyId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMap#setValue(java.lang.String)
     */
    public void setValue(String propertyId, Object newValue) {
        throw new UnsupportedOperationException();        
    }

}
