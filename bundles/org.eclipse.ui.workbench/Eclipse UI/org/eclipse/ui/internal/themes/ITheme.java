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
package org.eclipse.ui.internal.themes;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * 
 * <p><em>EXPERIMENTAL</em></p>
 * 
 * @since 3.0
 */
public interface ITheme {

    /**
	 * Indicates that the provided theme has changed in some way.
	 * @since 3.0
	 */
	public static final String CHANGE_THEME = "CHANGE_THEME"; //$NON-NLS-1$    
    
    /**
	 * Adds a property listener to the theme.
	 * 
	 * @param listener the listener to add
	 */
	void addPropertyChangeListener(IPropertyChangeListener listener);
	
	/**
	 * Removes a property listener from the theme.
	 * 
	 * @param listener the listener to remove
	 */
	void removePropertyChangeListener(IPropertyChangeListener listener);    
    
	/**
	 * Returns the id of this theme.
	 * 
	 * @return the id of this theme.
	 */
    String getId();
    
    public ColorRegistry getColorRegistry();
    
    public FontRegistry getFontRegistry();
    
    public GradientRegistry getGradientRegistry();

    public void dispose();
    
    public ITabThemeDescriptor getTabTheme();
}