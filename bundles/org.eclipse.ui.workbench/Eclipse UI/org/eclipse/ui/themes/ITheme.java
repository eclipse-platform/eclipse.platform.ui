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

import java.util.Set;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * 
 * <p><em>EXPERIMENTAL</em></p>
 * 
 * @since 3.0
 */
public interface ITheme {

    /**
	 * Adds a property listener to the theme.  Any events fired by the 
	 * underlying registries will cause an event to be fired.  This event is the
	 * same event that was fired by the registry.
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
	 * @return the id of this theme.  Guarenteed not to be <code>null</code>.
	 */
    String getId();
    
	/**
	 * Returns the label of this theme.
	 * 
	 * @return the label of this theme.  Guarenteed not be <code>null</code>.
	 */    
    String getLabel();
    
    /**
     * Return this themes color registry.
     * 
     * @return this themes color registry
     */
    public ColorRegistry getColorRegistry();

    /**
     * Return this themes font registry.
     * 
     * @return this themes font registry
     */
    public FontRegistry getFontRegistry();

    /**
     * Dispose of this theme.
     */
    public void dispose();
    
    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or <code>null</code> if none exists.
     */
    public String getString(String key);
    
    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or the default value <code>0</code> if none exists or 
     * if the value cannot be treated as an integer.
     */
    public int getInt(String key);
    
    /**
     * Get arbitrary data associated with this theme.
     *
     * @param key the key
     * @return the data, or the default value <code>false</code> if none exists 
     * or if the value cannot be treated as a boolean.
     */
    public boolean getBoolean(String key);
    
   
    
    /**
     * Get the set of keys associated with this theme.
     *  
     * @return the Set of keys
     */
    public Set keySet();
}