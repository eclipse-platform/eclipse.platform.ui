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
package org.eclipse.ui.themes;

import java.util.Set;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A theme is a collection of colors, fonts and supporting data that may 
 * be used by plugins to help provide uniform look and feel to their components.
 * The workbench has a default theme (one whos id has the value {@link org.eclipse.ui.themes.IThemeManager#DEFAULT_THEME}) 
 * that defines the initial values for a collection of fonts and colors.  Other
 * themes may extend and override the default theme to provide new values.
 * 
 * <p>
 * Clients may obtain themes via {@link org.eclipse.ui.themes.IThemeManager#getTheme(String)}.
 * </p>
 * 
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p> 
 * 
 * @see org.eclipse.ui.IWorkbench#getThemeManager()  
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITheme {

    /**
     * Adds a property listener to the theme.  Any events fired by the 
     * underlying registries will cause an event to be fired.  This event is the
     * same event that was fired by the registry.  As such, the "source" 
     * attribute of the event will not be this theme, but rather the color or 
     * font registry.
     * 
     * @param listener the listener to add
     */
    void addPropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Dispose of this theme.  This method is called by the workbench when
     * appropriate and should never be called by a user.
     */
    void dispose();

    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or the default value <code>false</code> if none exists 
     * or if the value cannot be treated as a boolean.
     */
    boolean getBoolean(String key);

    /**
     * Return this themes color registry.
     * 
     * @return this themes color registry
     */
    ColorRegistry getColorRegistry();

    /**
     * Return this themes font registry.
     * 
     * @return this themes font registry
     */
    FontRegistry getFontRegistry();

    /**
     * Returns the id of this theme.
     * 
     * @return the id of this theme.  Guaranteed not to be <code>null</code>.
     */
    String getId();

    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or the default value <code>0</code> if none exists or 
     * if the value cannot be treated as an integer.
     */
    public int getInt(String key);

    /**
     * Returns the label of this theme.
     * 
     * @return the label of this theme.  Guaranteed not be <code>null</code>.
     */
    String getLabel();

    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or <code>null</code> if none exists.
     */
    String getString(String key);

    /**
     * Get the set of keys associated with this theme.
     *  
     * @return the Set of keys
     */
    Set keySet();

    /**
     * Removes a property listener from the theme.
     * 
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(IPropertyChangeListener listener);
}
