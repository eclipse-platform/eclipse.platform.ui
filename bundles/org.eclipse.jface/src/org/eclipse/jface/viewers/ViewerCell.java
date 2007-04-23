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
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * The ViewerCell is the JFace representation of a cell entry in a ViewerRow.
 * 
 * @since 3.3
 * 
 */
public class ViewerCell {
	private int columnIndex;

	private ViewerRow row;

	private Object element;

	/**
	 * Constant denoting the cell above current one (value is 1).
	 */
	public static int ABOVE = 1;

	/**
	 * Constant denoting the cell below current one (value is 2).
	 */
	public static int BELOW = 1 << 1;

	/**
	 * Constant denoting the cell to the left of the current one (value is 4).
	 */
	public static int LEFT = 1 << 2;

	/**
	 * Constant denoting the cell to the right of the current one (value is 8).
	 */
	public static int RIGHT = 1 << 3;

	/**
	 * Create a new instance of the receiver on the row.
	 * 
	 * @param row
	 * @param columnIndex
	 */
	ViewerCell(ViewerRow row, int columnIndex, Object element) {
		this.row = row;
		this.columnIndex = columnIndex;
		this.element = element;
	}

	/**
	 * Get the index of the cell.
	 * 
	 * @return the index
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * Get the bounds of the cell.
	 * 
	 * @return {@link Rectangle}
	 */
	public Rectangle getBounds() {
		return row.getBounds(columnIndex);
	}

	/**
	 * Get the element this row represents.
	 * 
	 * @return {@link Object}
	 */
	public Object getElement() {
		if (element!=null) {
			return element;
		}
		return row.getElement();
	}

	/**
	 * Return the text for the cell.
	 * 
	 * @return {@link String}
	 */
	public String getText() {
		return row.getText(columnIndex);
	}

	/**
	 * Return the Image for the cell.
	 * 
	 * @return {@link Image} or <code>null</code>
	 */
	public Image getImage() {
		return row.getImage(columnIndex);
	}

	/**
	 * Set the background color of the cell.
	 * 
	 * @param background
	 */
	public void setBackground(Color background) {
		row.setBackground(columnIndex, background);

	}

	/**
	 * Set the foreground color of the cell.
	 * 
	 * @param foreground
	 */
	public void setForeground(Color foreground) {
		row.setForeground(columnIndex, foreground);

	}

	/**
	 * Set the font of the cell.
	 * 
	 * @param font
	 */
	public void setFont(Font font) {
		row.setFont(columnIndex, font);

	}

	/**
	 * Set the text for the cell.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		row.setText(columnIndex, text);

	}

	/**
	 * Set the Image for the cell.
	 * 
	 * @param image
	 */
	public void setImage(Image image) {
		row.setImage(columnIndex, image);

	}

	/**
	 * Set the columnIndex.
	 * 
	 * @param column
	 */
	void setColumn(int column) {
		columnIndex = column;

	}

	/**
	 * Set the row to rowItem and the columnIndex to column.
	 * 
	 * @param rowItem
	 * @param column
	 */
	void update(ViewerRow rowItem, int column, Object element) {
		row = rowItem;
		columnIndex = column;
		this.element = element;
	}

	/**
	 * Return the item for the receiver.
	 * 
	 * @return {@link Item}
	 */
	public Widget getItem() {
		return row.getItem();
	}

	/**
	 * Get the control for this cell.
	 * 
	 * @return {@link Control}
	 */
	public Control getControl() {
		return row.getControl();
	}

	/**
	 * Returns the specified neighbor of this cell, or <code>null</code> if no
	 * neighbor exists in the given direction. Direction constants can be
	 * combined by bitwise OR; for example, this method will return the cell to
	 * the upper-left of the current cell by passing {@link #ABOVE} |
	 * {@link #LEFT}. If <code>sameLevel</code> is <code>true</code>, only
	 * cells in sibling rows (under the same parent) will be considered.
	 * 
	 * @param directionMask
	 *            the direction mask used to identify the requested neighbor
	 *            cell
	 * @param sameLevel
	 *            if <code>true</code>, only consider cells from sibling rows
	 * @return the requested neighbor cell, or <code>null</code> if not found
	 */
	public ViewerCell getNeighbor(int directionMask, boolean sameLevel) {
		ViewerRow row;
		int columnIndex;

		if ((directionMask & ABOVE) == ABOVE) {
			row = this.row.getNeighbor(ViewerRow.ABOVE, sameLevel);
		} else if ((directionMask & BELOW) == BELOW) {
			row = this.row.getNeighbor(ViewerRow.BELOW, sameLevel);
		} else {
			row = this.row;
		}

		if (row != null) {
			if ((directionMask & LEFT) == LEFT) {
				columnIndex = getColumnIndex() - 1;
			} else if ((directionMask & RIGHT) == RIGHT) {
				columnIndex = getColumnIndex() + 1;
			} else {
				columnIndex = getColumnIndex();
			}

			if (columnIndex >= 0 && columnIndex < row.getColumnCount()) {
				return row.getCell(columnIndex);
			}
		}

		return null;
	}
	
	/**
	 * @return the row
	 */
	public ViewerRow getViewerRow() {
		return row;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnIndex;
		result = prime * result + ((row == null) ? 0 : row.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ViewerCell other = (ViewerCell) obj;
		if (columnIndex != other.columnIndex)
			return false;
		if (row == null) {
			if (other.row != null)
				return false;
		} else if (!row.equals(other.row))
			return false;
		return true;
	}
}
