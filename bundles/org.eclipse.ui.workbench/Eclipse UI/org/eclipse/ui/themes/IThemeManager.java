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
package org.eclipse.ui.themes;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A theme manager is an object that contains references to usable 
 * <code>ITheme</code> objects.
 * 
 * @since 3.0
 */
public interface IThemeManager {
    
    /**
	 * The default theme id.
	 */
	public static final String DEFAULT_THEME = "org.eclipse.ui.defaultTheme"; //$NON-NLS-1$ 
	
	/**
	 * Indicates that the provided theme has changed in some way.
	 */
	//public static final String CHANGE_THEME = "CHANGE_THEME"; //$NON-NLS-1$
	
	/**
	 * Indicates that the current theme has changed to a new theme.
	 */
	public static final String CHANGE_CURRENT_THEME = "CHANGE_CURRENT_THEME"; //$NON-NLS-1$
	
    /**
	 * Get a theme.
	 * 
	 * @param id the theme to find.
	 * @return the <code>ITheme</code> or <code>null</code> if it cannot be found.
	 */
    public abstract ITheme getTheme(String id);

    /**
	 * Get the currently active theme.
	 * 
	 * @return the current theme.  This will never be <code>null</code>.
	 */
    public abstract ITheme getCurrentTheme();

    /**
	 * Adds a property listener to the manager.  Any events fired by the 
	 * underlying registries of the current theme will cause an event to be 
	 * fired.  This event is the same event that was fired by the registry.
	 * Additionally, an event is fired when the current theme changes to a 
	 * new theme.
	 * 
	 * @param listener the listener to add
	 */
    public abstract void addPropertyChangeListener(
            IPropertyChangeListener listener);

    /**
	 * Removes a property listener from the workbench.
	 * 
	 * @param listener the listener to remove 
	 */    
    public abstract void removePropertyChangeListener(
            IPropertyChangeListener listener);
    
    /**
	 * Set the currently active theme.
	 * 
	 * @param id the id of the new active theme
	 */
    public abstract void setCurrentTheme(String id);    
}