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
import java.util.Vector;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This segment contains a collection of images and links that all belong to one
 * logical hyperlink.
 */
public class AggregateHyperlinkSegment extends ParagraphSegment implements
		IHyperlinkSegment {
	private String href;

	private Vector segments = new Vector();

	public AggregateHyperlinkSegment() {
	}

	public void add(TextHyperlinkSegment segment) {
		segments.add(segment);
	}

	public void add(ImageHyperlinkSegment segment) {
		segments.add(segment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.ParagraphSegment#advanceLocator(org.eclipse.swt.graphics.GC,
	 *      int, org.eclipse.ui.internal.forms.widgets.Locator,
	 *      java.util.Hashtable, boolean)
	 */
	public boolean advanceLocator(GC gc, int wHint, Locator loc,
			Hashtable objectTable, boolean computeHeightOnly) {
		boolean newLine = false;
		for (int i = 0; i < segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment) segments.get(i);
			if (segment.advanceLocator(gc, wHint, loc, objectTable,
					computeHeightOnly))
				newLine = true;
		}
		return newLine;
	}

	/**
	 * @return Returns the href.
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href
	 *            The href to set.
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#repaint(org.eclipse.swt.graphics.GC,
	 *      boolean)
	 */
	public void paint(GC gc, boolean hover, Hashtable resourceTable,
			boolean selected, SelectionData selData, Rectangle repaintRegion) {
		for (int i = 0; i < segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment) segments.get(i);
			segment.paint(gc, hover, resourceTable, selected, selData,
					repaintRegion);
		}
	}

	public String getText() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			buf.append(segment.getText());
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#paintFocus(org.eclipse.swt.graphics.GC,
	 *      org.eclipse.swt.graphics.Color, org.eclipse.swt.graphics.Color,
	 *      boolean)
	 */
	public void paintFocus(GC gc, Color bg, Color fg, boolean selected,
			Rectangle repaintRegion) {
		for (int i = 0; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			segment.paintFocus(gc, bg, fg, selected, repaintRegion);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#getBounds()
	 */
	public Rectangle getBounds() {
		if (segments.size() == 0)
			return new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
			
		IHyperlinkSegment segment0 = (IHyperlinkSegment) segments.get(0);
		Rectangle bounds = segment0.getBounds();
		for (int i = 1; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			Rectangle sbounds = segment.getBounds();
			bounds.add(sbounds);
		}
		return bounds;
	}

	public boolean contains(int x, int y) {
		for (int i = 0; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			if (segment.contains(x, y))
				return true;
		}
		return false;
	}

	public boolean intersects(Rectangle rect) {
		for (int i = 0; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			if (segment.intersects(rect))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.ParagraphSegment#layout(org.eclipse.swt.graphics.GC,
	 *      int, org.eclipse.ui.internal.forms.widgets.Locator,
	 *      java.util.Hashtable, boolean,
	 *      org.eclipse.ui.internal.forms.widgets.SelectionData)
	 */
	public void layout(GC gc, int width, Locator locator,
			Hashtable resourceTable, boolean selected) {
		for (int i = 0; i < segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment) segments.get(i);
			segment.layout(gc, width, locator, resourceTable, selected);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.forms.widgets.ParagraphSegment#computeSelection(org.eclipse.swt.graphics.GC,
	 *      java.util.Hashtable, boolean,
	 *      org.eclipse.ui.internal.forms.widgets.SelectionData)
	 */
	public void computeSelection(GC gc, Hashtable resourceTable,
			SelectionData selData) {
		for (int i = 0; i < segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment) segments.get(i);
			segment.computeSelection(gc, resourceTable, selData);
		}
	}

	public void clearCache(String fontId) {
		for (int i = 0; i < segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment) segments.get(i);
			segment.clearCache(fontId);
		}
	}

	public String getTooltipText() {
		if (segments.size() > 0)
			return ((ParagraphSegment) segments.get(0)).getTooltipText();
		return super.getTooltipText();
	}

	public boolean isFocusSelectable(Hashtable resourceTable) {
		return true;
	}

	public boolean setFocus(Hashtable resourceTable, boolean direction) {
		return true;
	}
}
