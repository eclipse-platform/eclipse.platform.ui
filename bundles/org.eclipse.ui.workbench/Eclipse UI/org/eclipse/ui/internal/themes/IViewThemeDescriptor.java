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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Theme descriptor for a view/
 *
 * @since 3.0
 */
public interface IViewThemeDescriptor {
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Color getColor (String key);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Color [] getGradientColors (String key); 
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public int [] getGradientPercents(String key);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public int getGradientDirection(String key);
	
	/**
	 * @param key
	 * @return
	 */
	public Font getFont (String key);
	
	/**
	 * @return
	 */
	public int getBorderStyle ();

}
