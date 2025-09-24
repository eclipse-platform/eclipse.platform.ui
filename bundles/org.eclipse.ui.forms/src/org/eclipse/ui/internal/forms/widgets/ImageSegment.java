/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @version 1.0
 */
public class ImageSegment extends ObjectSegment {
	public static final String SEL_IMAGE_PREFIX = "isel."; //$NON-NLS-1$

	public Image getImage(Hashtable<String, Object> objectTable) {
		return getImage(getObjectId(), objectTable);
	}

	private Image getImage(String key, Hashtable<String, Object> objectTable) {
		if (key == null) {
			return null;
		}
		Object obj = objectTable.get(key);
		if (obj == null) {
			return null;
		}
		if (obj instanceof Image) {
			return (Image) obj;
		}
		return null;
	}

	private Image getSelectedImage(Hashtable<String, Object> objectTable, SelectionData selData) {
		String key = SEL_IMAGE_PREFIX + getObjectId();
		Image image = getImage(key, objectTable);
		if (image==null) {
			image = FormUtil.createAlphaMashImage(selData.display, getImage(objectTable));
			if (image!=null) {
				objectTable.put(key, image);
			}
		}
		return image;
	}
/*
	private String getSelectedImageId() {
		if (getObjectId() == null)
			return null;
		return SEL_IMAGE_PREFIX + getObjectId();
	}
*/

	@Override
	public void paint(GC gc, boolean hover, Hashtable<String, Object> resourceTable, boolean selected, SelectionData selData, Rectangle repaintRegion) {
		Image image = getImage(resourceTable);
		int iwidth = 0;
		int iheight = 0;
		if (image != null) {
			Rectangle rect = image.getBounds();
			iwidth = rect.width + (isSelectable()?2:0);
			iheight = rect.height + (isSelectable()?2:0);
		} else {
			return;
		}
		Rectangle bounds = getBounds();
		int ix = bounds.x+(isSelectable()?1:0);
		int iy = bounds.y+(isSelectable()?1:0);

		if (selData != null) {
			int leftOffset = selData.getLeftOffset(bounds.height);
			int rightOffset = selData.getRightOffset(bounds.height);
			boolean firstRow = selData.isFirstSelectionRow(bounds.y,
					bounds.height);
			boolean lastRow = selData.isLastSelectionRow(bounds.y,
					bounds.height);
			boolean selectedRow = selData
					.isSelectedRow(bounds.y, bounds.height);
			if (selectedRow) {
				if ((firstRow && leftOffset > ix) ||
					(lastRow && rightOffset < ix + iwidth/2)) {
					drawClipImage(gc, image, ix, iy, repaintRegion);
				}
				else {
					Color savedBg = gc.getBackground();
					gc.setBackground(selData.bg);
					int sx = ix;
					int sy = iy;
					if (repaintRegion!=null) {
						sx -= repaintRegion.x;
						sy -= repaintRegion.y;
					}
					gc.fillRectangle(sx, sy, iwidth, iheight);
					Image selImage = getSelectedImage(resourceTable, selData);
					gc.drawImage(selImage, sx, sy);
					gc.setBackground(savedBg);
				}
			} else {
				drawClipImage(gc, image, ix, iy, repaintRegion);
			}
		} else {
			drawClipImage(gc, image, ix, iy, repaintRegion);
		}
		if (selected) {
			int fx = bounds.x;
			int fy = bounds.y;
			if (repaintRegion!=null) {
				fx -= repaintRegion.x;
				fy -= repaintRegion.y;
			}
			Color fg = gc.getForeground();
			gc.setForeground(gc.getBackground());
			// Clean up to avoid canceling out XOR if it is already
			// selected.
			gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1,
					bounds.height - 1);
			gc.setForeground(fg);
			gc.drawFocus(fx, fy, bounds.width, bounds.height);
		}
	}
	private void drawClipImage(GC gc, Image image, int ix, int iy, Rectangle repaintRegion) {
		if (repaintRegion!=null) {
			ix -= repaintRegion.x;
			iy -= repaintRegion.y;
		}
		gc.drawImage(image, ix, iy);
	}

	@Override
	protected Point getObjectSize(Hashtable<String, Object> resourceTable, int wHint) {
		Image image = getImage(resourceTable);
		if (image==null) {
			return new Point(0, 0);
		}
		Rectangle ibounds = image.getBounds();
		return new Point(ibounds.width, ibounds.height);
	}
}
