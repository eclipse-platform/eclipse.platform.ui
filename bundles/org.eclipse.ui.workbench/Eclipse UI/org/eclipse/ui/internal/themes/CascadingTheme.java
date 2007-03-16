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
package org.eclipse.ui.internal.themes;

import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.0
 */
public class CascadingTheme extends EventManager implements ITheme {

    private CascadingFontRegistry fontRegistry;

    private CascadingColorRegistry colorRegistry;

    private ITheme currentTheme;

    private IPropertyChangeListener listener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            fire(event);
        }
    };

    /**
     * @param colorRegistry
     * @param fontRegistry
     */
    public CascadingTheme(ITheme currentTheme,
            CascadingColorRegistry colorRegistry,
            CascadingFontRegistry fontRegistry) {
        this.currentTheme = currentTheme;
        this.colorRegistry = colorRegistry;
        this.fontRegistry = fontRegistry;

        fontRegistry.addListener(listener);
        colorRegistry.addListener(listener);
    }

    /**
     * @param event
     */
    protected void fire(PropertyChangeEvent event) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        addListenerObject(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getId()
     */
    public String getId() {
        return currentTheme.getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getLabel()
     */
    public String getLabel() {
        return currentTheme.getLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getColorRegistry()
     */
    public ColorRegistry getColorRegistry() {
        return colorRegistry;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getFontRegistry()
     */
    public FontRegistry getFontRegistry() {
        return fontRegistry;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#dispose()
     */
    public void dispose() {
        colorRegistry.removeListener(listener);
        fontRegistry.removeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getString(java.lang.String)
     */
    public String getString(String key) {
        return currentTheme.getString(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getInt(java.lang.String)
     */
    public int getInt(String key) {
        return currentTheme.getInt(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String key) {
        return currentTheme.getBoolean(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#keySet()
     */
    public Set keySet() {
        return currentTheme.keySet();
    }

}
