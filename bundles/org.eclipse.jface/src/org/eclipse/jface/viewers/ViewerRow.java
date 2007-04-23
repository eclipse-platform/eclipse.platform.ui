/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                fix for bug 166346, bug 167325s
 *                                              - Fix for bug 174355
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * ViewerRow is the abstract superclass of the part that represents items in a
 * Table or Tree. Implementors of {@link ColumnViewer} have to provide a
 * concrete implementation for the underlying widget
 * 
 * @since 3.3
 * 
 */
public abstract class ViewerRow implements Cloneable {

	/**
	 * Constant denoting the row above the current one (value is 1).
	 * 
	 * @see #getNeighbor(int, boolean)
	 */
	public static final int ABOVE = 1;

	/**
	 * Constant denoting the row below the current one (value is 2).
	 * 
	 * @see #getNeighbor(int, boolean)
	 */
	public static final int BELOW = 2;

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
	 * @return {@link Widget}
	 */
	public abstract Widget getItem();

	/**
	 * Return the number of columns for the receiver.
	 * 
	 * @return the number of columns
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
		if (count == 0) {
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
			return new ViewerCell((ViewerRow) clone(), column, getElement());

		return null;
	}

	/**
	 * Get the Control for the receiver.
	 * 
	 * @return {@link Control}
	 */
	public abstract Control getControl();

	/**
	 * Returns a neighboring row, or <code>null</code> if no neighbor exists
	 * in the given direction. If <code>sameLevel</code> is <code>true</code>,
	 * only sibling rows (under the same parent) will be considered.
	 * 
	 * @param direction
	 *            the direction {@link #BELOW} or {@link #ABOVE}
	 * 
	 * @param sameLevel
	 *            if <code>true</code>, search only within sibling rows
	 * @return the row above/below, or <code>null</code> if not found
	 */
	public abstract ViewerRow getNeighbor(int direction, boolean sameLevel);

	/**
	 * The tree path used to identify an element by the unique path
	 * 
	 * @return the path
	 */
	public abstract TreePath getTreePath();

	public abstract Object clone();

	/**
	 * @return the model element
	 */
	public abstract Object getElement();

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getItem() == null) ? 0 : getItem().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ViewerRow other = (ViewerRow) obj;
		if (getItem() == null) {
			if (other.getItem() != null)
				return false;
		} else if (!getItem().equals(other.getItem()))
			return false;
		return true;
	}

}
