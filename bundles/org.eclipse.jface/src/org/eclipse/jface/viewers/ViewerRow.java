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
 *                                                fix for bug 166346
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/**
 * ViewerRow is the abstract superclass of the part that represents items in a
 * Table or Tree.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
public abstract class ViewerRow {
	/**
	 * Key used to reference ViewerRow in the widgets data-map
	 */
	public static final String ROWPART_KEY = Policy.JFACE + ".ROWPART"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param item
	 */
	protected ViewerRow(final Item item) {
		item.setData(ViewerRow.ROWPART_KEY, this);
	}

	/**
	 * Get the bounds of the entry at the columnIndex,
	 * 
	 * @param columnIndex
	 * @return {@link Rectangle}
	 */
	public abstract Rectangle getBounds(int columnIndex);

	/**
	 * Return the bounds for the whole item.
	 * 
	 * @return {@link Rectangle}
	 */
	public abstract Rectangle getBounds();

	/**
	 * Return the item for the receiver.
	 * 
	 * @return {@link Item}
	 */
	public abstract Item getItem();

	/**
	 * Return the number of columns for the receiver.
	 * 
	 * @return int
	 */
	public abstract int getColumnCount();

	/**
	 * Return the image at the columnIndex.
	 * 
	 * @param columnIndex
	 * @return {@link Image} or <code>null</code>
	 */
	public abstract Image getImage(int columnIndex);

	/**
	 * Set the image at the columnIndex
	 * 
	 * @param columnIndex
	 * @param image
	 */
	public abstract void setImage(int columnIndex, Image image);

	/**
	 * Get the text at the columnIndex.
	 * 
	 * @param columnIndex
	 * @return {@link String}
	 */
	public abstract String getText(int columnIndex);

	/**
	 * Set the text at the columnIndex
	 * 
	 * @param columnIndex
	 * @param text
	 */
	public abstract void setText(int columnIndex, String text);

	/**
	 * Get the background at the columnIndex,
	 * 
	 * @param columnIndex
	 * @return {@link Color} or <code>null</code>
	 */
	public abstract Color getBackground(int columnIndex);

	/**
	 * Set the background at the columnIndex.
	 * 
	 * @param columnIndex
	 * @param color
	 */
	public abstract void setBackground(int columnIndex, Color color);

	/**
	 * Get the foreground at the columnIndex.
	 * 
	 * @param columnIndex
	 * @return {@link Color} or <code>null</code>
	 */
	public abstract Color getForeground(int columnIndex);

	/**
	 * Set the foreground at the columnIndex.
	 * 
	 * @param columnIndex
	 * @param color
	 */
	public abstract void setForeground(int columnIndex, Color color);

	/**
	 * Get the font at the columnIndex.
	 * 
	 * @param columnIndex
	 * @return {@link Font} or <code>null</code>
	 */
	public abstract Font getFont(int columnIndex);

	/**
	 * Set the {@link Font} at the columnIndex.
	 * 
	 * @param columnIndex
	 * @param font
	 */
	public abstract void setFont(int columnIndex, Font font);

	/**
	 * Get the ViewerCell at point.
	 * 
	 * @param point
	 * @return {@link ViewerCell}
	 */
	public ViewerCell getCell(Point point) {
		int index = getColumnIndex(point);

		return getCell(index);
	}

	/**
	 * Get the columnIndex of the point.
	 * 
	 * @param point
	 * @return int or -1 if it cannot be found.
	 */
	public int getColumnIndex(Point point) {
		int count = getColumnCount();
		
		// If there are no columns the column-index is 0
		if( count == 0 ) {
			return 0;
		}
		
		for (int i = 0; i < count; i++) {
			if (getBounds(i).contains(point)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Get a ViewerCell for the column at index.
	 * 
	 * @param column
	 * @return {@link ViewerCell} or <code>null</code> if the index is
	 *         negative.
	 */
	public ViewerCell getCell(int column) {
		if (column >= 0)
			return new ViewerCell(this, column);

		return null;
	}

	/**
	 * Get the Control for the receiver.
	 * 
	 * @return {@link Control}
	 */
	public abstract Control getControl();

}
