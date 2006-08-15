/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

/**
 * TreeRowPart is the Tree implementation of RowPart.
 * @since 3.3
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 *
 */
public class TreeRowPart extends RowPart {
	private TreeItem item;
	
	/**
	 * Create a new instance of the receiver.
	 * @param item
	 */
	TreeRowPart(TreeItem item) {
		super(item);
		this.item = item;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getBounds(int)
	 */
	public Rectangle getBounds(int columnIndex) {
		return item.getBounds(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getBounds()
	 */
	public Rectangle getBounds() {
		return item.getBounds();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getColumnCount()
	 */
	public int getColumnCount() {
		return item.getParent().getItemCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getItem()
	 */
	public Item getItem() {
		return item;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getBackground(int)
	 */
	public Color getBackground(int columnIndex) {
		return item.getBackground(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getFont(int)
	 */
	public Font getFont(int columnIndex) {
		return item.getFont(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getForeground(int)
	 */
	public Color getForeground(int columnIndex) {
		return item.getForeground(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getImage(int)
	 */
	public Image getImage(int columnIndex) {
		return item.getImage(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#getText(int)
	 */
	public String getText(int columnIndex) {
		return item.getText(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#setBackground(int, org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(int columnIndex, Color color) {
		item.setBackground(columnIndex, color);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#setFont(int, org.eclipse.swt.graphics.Font)
	 */
	public void setFont(int columnIndex, Font font) {
		item.setFont(columnIndex, font);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#setForeground(int, org.eclipse.swt.graphics.Color)
	 */
	public void setForeground(int columnIndex, Color color) {
		item.setForeground(columnIndex, color);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#setImage(int, org.eclipse.swt.graphics.Image)
	 */
	public void setImage(int columnIndex, Image image) {
		item.setImage(columnIndex,image);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.RowPart#setText(int, java.lang.String)
	 */
	public void setText(int columnIndex, String text) {
		item.setText(columnIndex, text);
	}
}
