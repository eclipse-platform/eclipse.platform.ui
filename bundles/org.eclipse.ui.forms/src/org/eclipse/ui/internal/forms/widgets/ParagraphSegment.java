/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.GC;

/**
 * @version 	1.0
 * @author
 */
public abstract class ParagraphSegment {
	/**
	 * Moves the locator according to the content of this segment.
	 * @param gc
	 * @param wHint
	 * @param loc
	 * @param objectTable
	 * @param computeHeightOnly
	 * @return
	 */
	public abstract boolean advanceLocator(GC gc, int wHint, Locator loc, Hashtable objectTable, boolean computeHeightOnly);
	/**
	 * Computes bounding rectangles and row heights of this segments.
	 * @param gc
	 * @param width
	 * @param loc
	 * @param resourceTable
	 * @param selected
	 */
	public abstract void layout(GC gc, int width, Locator loc, Hashtable resourceTable, boolean selected);
	/**
	 * Paints this segment.
	 * @param gc
	 * @param hover
	 * @param resourceTable
	 * @param selected
	 * @param selData
	 * @param region
	 */
	public abstract void paint(GC gc, boolean hover, Hashtable resourceTable, boolean selected, SelectionData selData, Rectangle region);
	/**
	 * Paints this segment.
	 * @param gc
	 * @param resourceTable
	 * @param selected
	 * @param selData
	 */
	public abstract void computeSelection(GC gc, Hashtable resourceTable, SelectionData selData);	
	/**
	 * Tests if the coordinates are contained in one of the
	 * bounding rectangles of this segment.
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract boolean contains(int x, int y);
	/**
	 * Tests if the source rectangle intersects with
	 * one of the bounding rectangles of this segment.
	 * @param rect
	 * @return
	 */
	public abstract boolean intersects(Rectangle rect);
	/**
	 * Returns the tool tip of this segment or <code>null</code>
	 * if not defined.
	 * @return
	 */
	public String getTooltipText() {
		return null;
	}
	/**
	 * @param fontId TODO
	 * 
	 */
	public void clearCache(String fontId) {
	}
}
