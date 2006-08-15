/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;

/**
 * RowPart is the abstract superclass of the part that represents items in a 
 * Table or Tree.
 * @since 3.3
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 * 
 */
public abstract class RowPart {
	/**
	 * Key used to reference RowPart in the widgets data-map
	 */
	public static final String ROWPART_KEY = Policy.JFACE + ".ROWPART"; //$NON-NLS-1$
		
	/**
	 * Create a new instance of the receiver.
	 * @param item
	 */
	RowPart(final Item item) {
		item.setData(RowPart.ROWPART_KEY, this);
		item.addDisposeListener(new DisposeListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				item.setData(ROWPART_KEY, null);
			}
			
		});
	}
	
	/**
	 * Get the bounds of the entry at the columnIndex,
	 * @param columnIndex
	 * @return Rectangle
	 */
	public abstract Rectangle getBounds(int columnIndex);

	/**
	 * Return the bounds for the whole item.
	 * @return Rectangle
	 */
	public abstract Rectangle getBounds();

	/**
	 * Return the item for the receiver.
	 * @return Item
	 */
	public abstract Item getItem();

	/**
	 * Return the number of columns for the receiver.
	 * @return int
	 */
	public abstract int getColumnCount();
	
	/**
	 * Return the image at the columnIndex.
	 * @param columnIndex
	 * @return Image
	 */
	public abstract Image getImage(int columnIndex);
	
	/**
	 * Set the image at the columnIndex
	 * @param columnIndex
	 * @param image
	 */
	public abstract void setImage(int columnIndex,Image image);
	
	/**
	 * Get the text at the columnIndex.
	 * @param columnIndex
	 * @return String
	 */
	public abstract String getText(int columnIndex);
	
	/**
	 * Set the text at the columnIndex
	 * @param columnIndex
	 * @param text
	 */
	public abstract void setText(int columnIndex,String text);
	
	/**
	 * Get the background at the columnIndex,
	 * @param columnIndex
	 * @return Color
	 */
	public abstract Color getBackground(int columnIndex);
	
	/**
	 * Set the background at the columnIndex.
	 * @param columnIndex
	 * @param color
	 */
	public abstract void setBackground(int columnIndex, Color color);
	
	/**
	 * Get the foreground at the columnIndex.
	 * @param columnIndex
	 * @return Color
	 */
	public abstract Color getForeground(int columnIndex);
	
	/**
	 * Set the foreground at the columnIndex.
	 * @param columnIndex
	 * @param color
	 */
	public abstract void setForeground(int columnIndex, Color color);
	
	/**
	 * Get the font at the columnIndex.
	 * @param columnIndex
	 * @return Font
	 */
	public abstract Font getFont(int columnIndex);
	
	/**
	 * Set the font at the columnIndex.
	 * @param columnIndex
	 * @param font
	 */
	public abstract void setFont(int columnIndex,Font font);
	
	
	/**
	 * Get the Cell at point.
	 * @param point
	 * @return Cell
	 */
	public Cell getCell(Point point) {
		int index = getColumnIndex(point);
		
		if( index >= 0 ) {
			return new Cell(this,index);
		}

		return null;
	}
	
	/**
	 * Get the columnIndex of the point.
	 * @param point
	 * @return the column index or -1 if it cannot be found.
	 */
	public int getColumnIndex(Point point) {
		int count = getColumnCount();
		
		for(int i = 0; i < count; i++ ) {
			if( getBounds(i).contains(point) ) {
				return i;
			}
		}
		
		return -1;
	}
	
}
