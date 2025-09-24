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

	private final Vector<ParagraphSegment> segments = new Vector<>();

	public AggregateHyperlinkSegment() {
	}

	public void add(TextHyperlinkSegment segment) {
		segments.add(segment);
	}

	public void add(ImageHyperlinkSegment segment) {
		segments.add(segment);
	}

	@Override
	public boolean advanceLocator(GC gc, int wHint, Locator loc,
			Hashtable<String, Object> objectTable, boolean computeHeightOnly) {
		boolean newLine = false;
		for (ParagraphSegment segment : segments) {
			if (segment.advanceLocator(gc, wHint, loc, objectTable,
					computeHeightOnly)) {
				newLine = true;
			}
		}
		return newLine;
	}

	/**
	 * @return Returns the href.
	 */
	@Override
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

	@Override
	public void paint(GC gc, boolean hover, Hashtable<String, Object> resourceTable,
			boolean selected, SelectionData selData, Rectangle repaintRegion) {
		for (ParagraphSegment segment : segments) {
			segment.paint(gc, hover, resourceTable, selected, selData,
					repaintRegion);
		}
	}

	@Override
	public String getText() {
		StringBuilder buf = new StringBuilder();
		for (ParagraphSegment psegment : segments) {
			IHyperlinkSegment segment = (IHyperlinkSegment) psegment;
			buf.append(segment.getText());
		}
		return buf.toString();
	}

	@Override
	public void paintFocus(GC gc, Color bg, Color fg, boolean selected,
			Rectangle repaintRegion) {
		for (ParagraphSegment psegment : segments) {
			IHyperlinkSegment segment = (IHyperlinkSegment) psegment;
			segment.paintFocus(gc, bg, fg, selected, repaintRegion);
		}
	}

	@Override
	public Rectangle getBounds() {
		if (segments.isEmpty()) {
			return new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
		}

		IHyperlinkSegment segment0 = (IHyperlinkSegment) segments.get(0);
		Rectangle bounds = segment0.getBounds();
		for (int i = 1; i < segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment) segments.get(i);
			Rectangle sbounds = segment.getBounds();
			bounds.add(sbounds);
		}
		return bounds;
	}

	@Override
	public boolean contains(int x, int y) {
		for (ParagraphSegment psegment : segments) {
			IHyperlinkSegment segment = (IHyperlinkSegment) psegment;
			if (segment.contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean intersects(Rectangle rect) {
		for (ParagraphSegment psegment : segments) {
			IHyperlinkSegment segment = (IHyperlinkSegment) psegment;
			if (segment.intersects(rect)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void layout(GC gc, int width, Locator locator,
			Hashtable<String, Object> resourceTable, boolean selected) {
		for (ParagraphSegment segment : segments) {
			segment.layout(gc, width, locator, resourceTable, selected);
		}
	}

	@Override
	public void computeSelection(GC gc, Hashtable<String, Object> resourceTable,
			SelectionData selData) {
		for (ParagraphSegment segment : segments) {
			segment.computeSelection(gc, resourceTable, selData);
		}
	}

	@Override
	public void clearCache(String fontId) {
		for (ParagraphSegment segment : segments) {
			segment.clearCache(fontId);
		}
	}

	@Override
	public String getTooltipText() {
		if (segments.size() > 0) {
			return segments.get(0).getTooltipText();
		}
		return super.getTooltipText();
	}

	@Override
	public boolean isFocusSelectable(Hashtable<String, Object> resourceTable) {
		return true;
	}

	@Override
	public boolean setFocus(Hashtable<String, Object> resourceTable, boolean direction) {
		return true;
	}
}
