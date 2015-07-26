/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


public abstract class ObjectSegment extends ParagraphSegment {
	public static final int TOP = 1;

	public static final int MIDDLE = 2;

	public static final int BOTTOM = 3;

	private int alignment = BOTTOM;
	private boolean nowrap=false;
	private Rectangle bounds;
	private String objectId;

	public int getVerticalAlignment() {
		return alignment;
	}

	void setVerticalAlignment(int alignment) {
		this.alignment = alignment;
	}

	public String getObjectId() {
		return objectId;
	}

	void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	protected abstract Point getObjectSize(Hashtable<String, Object> resourceTable, int wHint);

	@Override
	public boolean advanceLocator(GC gc, int wHint, Locator loc,
			Hashtable<String, Object> objectTable, boolean computeHeightOnly) {
		Point objectSize = getObjectSize(objectTable, wHint);
		int iwidth = 0;
		int iheight = 0;
		boolean newLine = false;

		if (objectSize != null) {
			iwidth = objectSize.x + (isSelectable()?2:0);
			iheight = objectSize.y + (isSelectable()?2:0);
		}
		if (wHint != SWT.DEFAULT && !nowrap && loc.x + iwidth + loc.marginWidth > wHint) {
			// new line
			if (computeHeightOnly)
				loc.collectHeights();
			loc.resetCaret();
			loc.x += iwidth;
			loc.y += loc.rowHeight;
			loc.width = loc.x;
			loc.rowHeight = iheight;
			loc.leading = 0;
			newLine = true;
		} else {
			loc.x += iwidth;
			loc.width += iwidth;
			loc.rowHeight = Math.max(loc.rowHeight, iheight);
		}
		return newLine;
	}

	@Override
	public boolean contains(int x, int y) {
		if (bounds==null)
			return false;
		return bounds.contains(x, y);
	}
	@Override
	public boolean intersects(Rectangle rect) {
		if (bounds==null)
			return false;
		return bounds.intersects(rect);
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

	@Override
	public void paint(GC gc, boolean hover, Hashtable<String, Object> resourceTable, boolean selected, SelectionData selData, Rectangle repaintRegion) {
	}

	@Override
	public void layout(GC gc, int width, Locator loc, Hashtable<String, Object> resourceTable,
			boolean selected) {
		Point size = getObjectSize(resourceTable, width);

		int objWidth = 0;
		int objHeight = 0;
		if (size != null) {
			objWidth = size.x + (isSelectable()?2:0);
			objHeight = size.y + (isSelectable()?2:0);
		} else
			return;
		loc.width = objWidth;

		if (!nowrap && loc.x + objWidth + loc.marginWidth > width) {
			// new row
			loc.newLine();
			loc.rowCounter++;
		}
		int ix = loc.x;
		int iy = loc.y;

		if (alignment==MIDDLE)
			iy = loc.getMiddle(objHeight, false);
		else if (alignment==BOTTOM)
			iy = loc.getBaseline(objHeight, false);
		loc.x += objWidth;
		loc.rowHeight = Math.max(loc.rowHeight, objHeight);
		bounds = new Rectangle(ix, iy, objWidth, objHeight);
	}

	@Override
	public void computeSelection(GC gc, Hashtable<String, Object> resourceTable, SelectionData selData) {
		// TODO we should add this to the selection
		// if we want to support rich text
	}
}
