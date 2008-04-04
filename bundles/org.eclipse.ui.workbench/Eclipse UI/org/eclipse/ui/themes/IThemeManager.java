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

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A theme manager is an object that contains references to usable 
 * <code>ITheme</code> objects and maintains a reference to the currently active
 * theme.  This theme will be used by the workbench to decorate tab folders and
 * other controls where possible.  The workbench implementation of this 
 * interface will push the values of the current theme into the underlying jface
 * registries ({@link org.eclipse.jface.resource.ColorRegistry} and 
 * {@link org.eclipse.jface.resource.FontRegistry} whenever the current theme 
 * changes.  Clients who do not need access to specific themes may instead 
 * attach listeners to these registries directly.  
 * 
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @see org.eclipse.ui.IWorkbench#getThemeManager()
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IThemeManager {

    /**
     * Indicates that the current theme has changed to a new theme.
     */
    public static final String CHANGE_CURRENT_THEME = "CHANGE_CURRENT_THEME"; //$NON-NLS-1$

    /**
     * The default theme id.
     */
    public static final String DEFAULT_THEME = "org.eclipse.ui.defaultTheme"; //$NON-NLS-1$ 

    /**
     * Adds a property listener to the manager.  Any events fired by the 
     * underlying registries of the current theme will cause an event to be 
     * fired.  This event is the same event that was fired by the registry.
     * As such, the "source" attribute of the event will not be this manager, 
     * but rather the color or font registry.  Additionally, an event is fired 
     * when the current theme changes to a new theme.  The "property" attribute
     * of such an event will have the value {@link IThemeManager#CHANGE_CURRENT_THEME}.
     * 
     * @param listener the listener to add
     */
    void addPropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Get the currently active theme.
     * 
     * @return the current theme.  This will never be <code>null</code>.
     */
    ITheme getCurrentTheme();

    /**
     * Get a theme.
     * 
     * @param id the theme to find.
     * @return the <code>ITheme</code> or <code>null</code> if it cannot be found.
     */
    ITheme getTheme(String id);

    /**
     * Removes a property listener from the workbench.
     * 
     * @param listener the listener to remove 
     */
    void removePropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Set the currently active theme.
     * 
     * @param id the id of the new active theme
     */
    void setCurrentTheme(String id);
}
