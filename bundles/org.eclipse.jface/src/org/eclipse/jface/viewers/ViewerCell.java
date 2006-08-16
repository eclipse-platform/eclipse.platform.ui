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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The ViewerCell is the JFace representation of a cell entry in a ViewerRow.
 * @since 3.3
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 *
 */
public class ViewerCell {
	private int columnIndex;
	private ViewerRow row;
	
	/**
	 * Create a new instance of the receiver on the row.
	 * @param row
	 * @param columnIndex
	 */
	public ViewerCell(ViewerRow row, int columnIndex) {
		this.row = row;
		this.columnIndex = columnIndex;
	}
	
	/**
	 * Get the index of the cell.
	 * @return int
	 */
	public int getColumnIndex() {
		return columnIndex;
	}
	
	/**
	 * Get the bounds of the cell.
	 * @return Rectangle
	 */
	public Rectangle getBounds() {
		return row.getBounds(columnIndex);
	}
	
	/**
	 * Get the element this row represents.
	 * @return Object
	 */
	public Object getElement() {
		return row.getItem().getData();
	}

	/**
	 * Return the text for the cell.
	 * @return String
	 */
	public String getText() {
		return row.getText(columnIndex);
	}

	/**
	 * Return the Image for the cell.
	 * @return Image
	 */
	public Image getImage() {
		return row.getImage(columnIndex);
	}

	/**
	 * Set the background color of the cell.
	 * @param background
	 */
	public void setBackground(Color background) {
		row.setBackground(columnIndex, background);
		
	}

	/**
	 * Set the foreground color of the cell.
	 * @param foreground
	 */
	public void setForeground(Color foreground) {
		row.setForeground(columnIndex, foreground);
		
	}

	/**
	 * Set the font of the cell.
	 * @param font
	 */
	public void setFont(Font font) {
		row.setFont(columnIndex, font);
		
	}

	/**
	 * Set the text for the cell.
	 * @param text
	 */
	public void setText(String text) {
		row.setText(columnIndex, text);
		
	}

	/**
	 * Set the Image for the cell.
	 * @param image
	 */
	public void setImage(Image image) {
		row.setImage(columnIndex, image);
		
	}

	/**
	 * Set the columnIndex.
	 * @param column
	 */
	void setColumn(int column) {
		columnIndex = column;
		
	}

	/**
	 * Set the ro to rowItem and the columnIndex to column.
	 * @param rowItem
	 * @param column
	 */
	void update(ViewerRow rowItem, int column) {
		row = rowItem;
		columnIndex = column;		
	}
}
