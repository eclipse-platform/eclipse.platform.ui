/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;

/**
 * @version 1.0
 * @author
 */
public class ImageSegment extends ParagraphSegment {
	public static final String SEL_IMAGE_PREFIX = "isel.";

	public static final int TOP = 1;

	public static final int MIDDLE = 2;

	public static final int BOTTOM = 3;

	private int alignment = BOTTOM;

	private String imageId;
	private boolean nowrap=false;
	private Rectangle bounds;

	public int getVerticalAlignment() {
		return alignment;
	}

	void setVerticalAlignment(int alignment) {
		this.alignment = alignment;
	}

	public Image getImage(Hashtable objectTable) {
		return getImage(imageId, objectTable);
	}

	private Image getImage(String key, Hashtable objectTable) {
		if (key == null)
			return null;
		Object obj = objectTable.get(key);
		if (obj == null)
			return null;
		if (obj instanceof Image)
			return (Image) obj;
		return null;
	}

	private Image getSelectedImage(Hashtable objectTable, SelectionData selData) {
		String key = SEL_IMAGE_PREFIX + getObjectId();
		Image image = getImage(key, objectTable);
		if (image==null) {
			image = FormUtil.createAlphaMashImage(selData.display, getImage(objectTable));
			if (image!=null)
				objectTable.put(key, image);
		}
		return image;
	}

	public String getObjectId() {
		return imageId;
	}

	void setObjectId(String imageId) {
		this.imageId = imageId;
	}

	public boolean advanceLocator(GC gc, int wHint, Locator loc,
			Hashtable objectTable, boolean computeHeightOnly) {
		Image image = getImage(objectTable);
		int iwidth = 0;
		int iheight = 0;
		boolean newLine = false;

		if (image != null) {
			Rectangle rect = image.getBounds();
			iwidth = rect.width + (isSelectable()?2:0);
			iheight = rect.height + (isSelectable()?2:0);
		}
		if (wHint != SWT.DEFAULT && !nowrap && loc.x + iwidth > wHint) {
			// new line
			if (computeHeightOnly)
				loc.collectHeights();
			loc.x = loc.indent;
			loc.x += iwidth + (isSelectable()?1:0);
			loc.y += loc.rowHeight;
			loc.width = iwidth;			
			loc.rowHeight = iheight;
			loc.leading = 0;
			newLine = true;
		} else {
			loc.x += iwidth + (isSelectable()?1:0);
			loc.width += iwidth;
			loc.rowHeight = Math.max(loc.rowHeight, iheight);
		}
		return newLine;
	}

	private String getSelectedImageId() {
		if (imageId == null)
			return null;
		return SEL_IMAGE_PREFIX + imageId;
	}

	public void paint(GC gc, int width, Locator loc, Hashtable resourceTable,
			boolean selected, SelectionData selData) {
		Image image = getImage(resourceTable);

		int iwidth = 0;
		int iheight = 0;
		if (image != null) {
			Rectangle rect = image.getBounds();
			iwidth = rect.width + (isSelectable()?2:0);
			iheight = rect.height + (isSelectable()?2:0);
		} else
			return;
		loc.width = iwidth;
		loc.height = iheight;

		if (!nowrap && loc.x + iwidth > width) {
			// new row
			loc.newLine();
			loc.rowCounter++;
		}
		int ix = loc.x+(isSelectable()?1:0);
		int iy = loc.getBaseline(iheight, false)+(isSelectable()?1:0);
		if (selData != null) {
			int leftOffset = selData.getLeftOffset(loc);
			int rightOffset = selData.getRightOffset(loc);
			if (selData.isSelectedRow(loc)) {
				if ((selData.isFirstSelectionRow(loc) && leftOffset > ix) ||
					(selData.isLastSelectionRow(loc) && rightOffset < ix + iwidth/2)) {
					gc.drawImage(image, ix, iy);
				}
				else {
					Color savedBg = gc.getBackground();
					gc.setBackground(selData.bg);
					gc.fillRectangle(ix, iy, iwidth, iheight);
					Image selImage = getSelectedImage(resourceTable, selData);
					gc.drawImage(selImage, ix, iy);
					gc.setBackground(savedBg);
				}
			}
			else
				gc.drawImage(image, ix, iy);
		} else
			gc.drawImage(image, ix, iy);
		loc.x += iwidth + (isSelectable()?1:0);
		loc.rowHeight = Math.max(loc.rowHeight, iheight);
		bounds = new Rectangle(ix-(isSelectable()?1:0), 
				iy-(isSelectable()?1:0), iwidth+1, iheight+1);
		if (selected) {
			gc.drawFocus(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}
	
	public boolean contains(int x, int y) {
		if (bounds==null) 
			return false;
		return bounds.contains(x, y);
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public boolean isSelectable() {
		return false;
	}
	/**
	 * @return Returns the nowrap.
	 */
	public boolean isNowrap() {
		return nowrap;
	}
	/**
	 * @param nowrap The nowrap to set.
	 */
	public void setNowrap(boolean nowrap) {
		this.nowrap = nowrap;
	}
}