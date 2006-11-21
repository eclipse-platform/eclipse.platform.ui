/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Context sensitive label update request for an element.
 *  
 * @since 3.3
 */
public interface ILabelUpdate extends IViewerUpdate {
	
	/**
	 * Returns a tree path for the element the label update is for.
	 * 
	 * @return associated element tree path
	 */
	public TreePath getElement();
	
	/**
	 * Returns the id's of the visible columns in presentation order,
	 * or <code>null</code> if none.
	 * 
	 * @return column id's or <code>null</code>
	 */
	public String[] getColumnIds();

	/**
	 * Sets the text of the label of the specified column. Cannot be <code>null</code>.
	 * 
	 * @param text
	 * @param columnIndex column index (0 when no columns)
	 */
    public void setLabel(String text, int columnIndex);
    
    /**
     * Sets the font of the label.
     * 
     * @param fontData
     * @param columnIndex column index (0 when no columns)
     */
    public void setFontData(FontData fontData, int columnIndex);
    
    /**
     * Sets the image of the label.
     * 
     * @param image
     * @param columnIndex column index (0 when no columns)
     */
    public void setImageDescriptor(ImageDescriptor image, int columnIndex);
    
    /**
     * Sets the foreground color of the label.
     * 
     * @param foreground
     * @param columnIndex column index (0 when no columns)
     */
    public void setForeground(RGB foreground, int columnIndex);
    
    /**
     * Sets the background color of the label.
     * 
     * @param background
     * @param columnIndex column index (0 when no columns)
     */
    public void setBackground(RGB background, int columnIndex);
}
