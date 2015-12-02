/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											 - fix in bug: 174355,195908,198035,215069,227421
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * TableViewerRow is the Table specific implementation of ViewerRow
 * @since 3.3
 *
 */
public class TableViewerRow extends ViewerRow {
	private TableItem item;

	/**
	 * Create a new instance of the receiver from item.
	 * @param item
	 */
	TableViewerRow(TableItem item) {
		this.item = item;
	}

	@Override
	public Rectangle getBounds(int columnIndex) {
		return item.getBounds(columnIndex);
	}

	@Override
	public Rectangle getBounds() {
		return item.getBounds();
	}

	@Override
	public Widget getItem() {
		return item;
	}

	void setItem(TableItem item) {
		this.item = item;
	}

	@Override
	public int getColumnCount() {
		return item.getParent().getColumnCount();
	}

	@Override
	public Color getBackground(int columnIndex) {
		return item.getBackground(columnIndex);
	}

	@Override
	public Font getFont(int columnIndex) {
		return item.getFont(columnIndex);
	}

	@Override
	public Color getForeground(int columnIndex) {
		return item.getForeground(columnIndex);
	}

	@Override
	public Image getImage(int columnIndex) {
		return item.getImage(columnIndex);
	}

	@Override
	public String getText(int columnIndex) {
		return item.getText(columnIndex);
	}

	@Override
	public void setBackground(int columnIndex, Color color) {
		item.setBackground(columnIndex, color);
	}

	@Override
	public void setFont(int columnIndex, Font font) {
		item.setFont(columnIndex, font);
	}

	@Override
	public void setForeground(int columnIndex, Color color) {
		item.setForeground(columnIndex, color);
	}

	@Override
	public void setImage(int columnIndex, Image image) {
		Image oldImage = item.getImage(columnIndex);
		if (oldImage != image) {
			item.setImage(columnIndex,image);
		}
	}

	@Override
	public void setText(int columnIndex, String text) {
		item.setText(columnIndex, text == null ? "" : text); //$NON-NLS-1$
	}

	@Override
	public Control getControl() {
		return item.getParent();
	}

	@Override
	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		if( direction == ViewerRow.ABOVE ) {
			return getRowAbove();
		} else if( direction == ViewerRow.BELOW ) {
			return getRowBelow();
		} else {
			throw new IllegalArgumentException("Illegal value of direction argument."); //$NON-NLS-1$
		}
	}


	private ViewerRow getRowAbove() {
		int index = item.getParent().indexOf(item) - 1;

		if( index >= 0 ) {
			return new TableViewerRow(item.getParent().getItem(index));
		}

		return null;
	}

	private ViewerRow getRowBelow() {
		int index = item.getParent().indexOf(item) + 1;

		if( index < item.getParent().getItemCount() ) {
			TableItem tmp = item.getParent().getItem(index);
			//TODO NULL can happen in case of VIRTUAL => How do we deal with that
			if( tmp != null ) {
				return new TableViewerRow(tmp);
			}
		}

		return null;
	}

	@Override
	public TreePath getTreePath() {
		return new TreePath(new Object[] {item.getData()});
	}

	@Override
	public Object clone() {
		return new TableViewerRow(item);
	}

	@Override
	public Object getElement() {
		return item.getData();
	}

	@Override
	public int getVisualIndex(int creationIndex) {
		int[] order = item.getParent().getColumnOrder();

		for (int i = 0; i < order.length; i++) {
			if (order[i] == creationIndex) {
				return i;
			}
		}

		return super.getVisualIndex(creationIndex);
	}

	@Override
	public int getCreationIndex(int visualIndex) {
		if( item != null && ! item.isDisposed() && hasColumns() && isValidOrderIndex(visualIndex) ) {
			return item.getParent().getColumnOrder()[visualIndex];
		}
		return super.getCreationIndex(visualIndex);
	}

	@Override
	public Rectangle getTextBounds(int index) {
		return item.getTextBounds(index);
	}

	@Override
	public Rectangle getImageBounds(int index) {
		return item.getImageBounds(index);
	}

	private boolean hasColumns() {
		return this.item.getParent().getColumnCount() != 0;
	}

	private boolean isValidOrderIndex(int currentIndex) {
		return currentIndex < this.item.getParent().getColumnOrder().length;
	}

	@Override
	int getWidth(int columnIndex) {
		return item.getParent().getColumn(columnIndex).getWidth();
	}

	@Override
	protected boolean scrollCellIntoView(int columnIndex) {
		item.getParent().showItem(item);
		if( hasColumns() ) {
			item.getParent().showColumn(item.getParent().getColumn(columnIndex));
		}

		return true;
	}
}
