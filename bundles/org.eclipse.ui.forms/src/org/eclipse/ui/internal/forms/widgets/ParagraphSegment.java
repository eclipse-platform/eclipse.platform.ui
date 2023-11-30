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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @version 	1.0
 */
public abstract class ParagraphSegment {
	/**
	 * Moves the locator according to the content of this segment.
	 * @return <code>true</code> if text wrapped to the new line, <code>false</code> otherwise.
	 */
	public abstract boolean advanceLocator(GC gc, int wHint, Locator loc, Hashtable<String, Object> objectTable, boolean computeHeightOnly);
	/**
	 * Computes bounding rectangles and row heights of this segments.
	 */
	public abstract void layout(GC gc, int width, Locator loc, Hashtable<String, Object> resourceTable, boolean selected);
	/**
	 * Paints this segment.
	 */
	public abstract void paint(GC gc, boolean hover, Hashtable<String, Object> resourceTable, boolean selected, SelectionData selData, Rectangle region);
	/**
	 * Paints this segment.
	 */
	public abstract void computeSelection(GC gc, Hashtable<String, Object> resourceTable, SelectionData selData);
	/**
	 * Tests if the coordinates are contained in one of the
	 * bounding rectangles of this segment.
	 * @return true if inside the bounding rectangle, false otherwise.
	 */
	public abstract boolean contains(int x, int y);
	/**
	 * Tests if the source rectangle intersects with
	 * one of the bounding rectangles of this segment.
	 * @return true if the two rectangles intersect, false otherwise.
	 */
	public abstract boolean intersects(Rectangle rect);
	/**
	 * Returns the tool tip of this segment or <code>null</code>
	 * if not defined.
	 * @return tooltip or <code>null</code>.
	 */
	public String getTooltipText() {
		return null;
	}
	/**
	 * Clears the text metrics cache for the provided font id.
	 * @param fontId the id of the font that the cache is kept for.
	 */
	public void clearCache(String fontId) {
	}
}
