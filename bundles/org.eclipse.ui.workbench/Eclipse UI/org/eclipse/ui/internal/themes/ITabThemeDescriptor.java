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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Tab theme descriptor interface.
 * 
 * @since 3.0
 */
public interface ITabThemeDescriptor {
		
	/**
	 * @return
	 */
	public ImageDescriptor getCloseActiveImageDesc();

	/**
	 * @return
	 */
	public ImageDescriptor getCloseInactiveImageDesc();

	/**
	 * @return
	 */
	public ImageDescriptor getHoverImageDesc();

	/**
	 * @return
	 */
	public ImageDescriptor getMouseDownImageDesc();

	/**
	 * @return
	 */
	public ImageDescriptor getSelectedImageDesc();

	/**
	 * @return
	 */
	public int getShowInTab();

	/**
	 * @return
	 */
	public int getTabFixedHeight();

	/**
	 * @return
	 */
	public int getTabFixedWidth();

	/**
	 * @return
	 */
	public int getTabPosition();

	/**
	 * @return
	 */
	public ImageDescriptor getUnselectedImageDesc();

	/**
	 * @return
	 */
	public boolean isCustomTabDefined();

	/**
	 * @return
	 */
	public boolean isDragInFolder() ;

	/**
	 * @return
	 */
	public boolean isShowClose() ;
	
	/**
	 * @return
	 */
	public int getTabMarginSize(int position) ;
	
	/**
	 * @return
	 */
	public Color getTabMarginColor(int position);

	/**
	 * @param key
	 * @return
	 */
	public Font getFont (String key);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Color getColor (String key);
	
	/**
	 * @return
	 */
	public int getBorderStyle ();
	
	/**
	 * @return
	 */
	public int[] getItemMargins ();

	/**
	 * @return
	 */
	public boolean isShowTooltip() ;
	
}
