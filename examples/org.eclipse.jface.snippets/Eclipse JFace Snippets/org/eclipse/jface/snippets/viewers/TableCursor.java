/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;


public class TableCursor extends AbstractCellCursor {

	public TableCursor(AbstractTableViewer viewer) {
		super(viewer,SWT.NONE);
	}

	@Override
	protected void paint(Event event) {
		if (getSelectedCells().length == 1 && getSelectedCells()[0] == null) return;
		ViewerCell cell = getSelectedCells()[0];

		GC gc = event.gc;
		Display display = getDisplay();
		gc.setBackground(getBackground());
		gc.setForeground(getForeground());
		gc.fillRectangle(event.x, event.y, event.width, event.height);
		int x = 0;
		Point size = getSize();
		Image image = cell.getImage();
		if (image != null) {
			Rectangle imageSize = image.getBounds();
			int imageY = (size.y - imageSize.height) / 2;
			gc.drawImage(image, x, imageY);
			x += imageSize.width;
		}
		String text = cell.getText();
		if (text != "") { //$NON-NLS-1$
			Rectangle bounds = cell.getBounds();
			Point extent = gc.stringExtent(text);
			// Temporary code - need a better way to determine table trim
			if (Util.isWin32()) {
				if (((Table)getParent()).getColumnCount() == 0 || cell.getColumnIndex() == 0) {
					x += 2;
				} else {
					int alignmnent = ((Table)getParent()).getColumn(cell.getColumnIndex()).getAlignment();
					switch (alignmnent) {
						case SWT.LEFT:
							x += 6;
							break;
						case SWT.RIGHT:
							x = bounds.width - extent.x - 6;
							break;
						case SWT.CENTER:
							x += (bounds.width - x - extent.x) / 2;
							break;
					}
				}
			}  else {
				if (((Table)getParent()).getColumnCount() == 0) {
					x += 5;
				} else {
					int alignmnent = ((Table)getParent()).getColumn(cell.getColumnIndex()).getAlignment();
					switch (alignmnent) {
						case SWT.LEFT:
							x += 5;
							break;
						case SWT.RIGHT:
							x = bounds.width- extent.x - 2;
							break;
						case SWT.CENTER:
							x += (bounds.width - x - extent.x) / 2 + 2;
							break;
					}
				}
			}
			int textY = (size.y - extent.y) / 2;
			gc.drawString(text, x, textY);
		}
		if (isFocusControl()) {
			gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.drawFocus(0, 0, size.x, size.y);
		}
	}
}
