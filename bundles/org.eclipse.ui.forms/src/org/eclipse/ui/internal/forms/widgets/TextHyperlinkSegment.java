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

import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @version 1.0
 * @author
 */
public class TextHyperlinkSegment extends TextSegment implements
		IHyperlinkSegment {
	private String href;
	private String tooltipText;

	private static final String LINK_FG = "c.___link_fg";

	private HyperlinkSettings settings;

	public TextHyperlinkSegment(String text, HyperlinkSettings settings,
			String fontId) {
		super(text, fontId);
		this.settings = settings;
		underline = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_ALWAYS;
	}

	/*
	 * @see IObjectReference#getObjectId()
	 */
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public void paint(GC gc, int width, Locator locator,
			Hashtable resourceTable, boolean selected, SelectionData selData) {
		resourceTable.put(LINK_FG, settings.getForeground());
		setColorId(LINK_FG);
		super.paint(gc, width, locator, resourceTable, selected, selData);
	}

	public void repaint(GC gc, boolean hover, SelectionData selData) {
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();

		for (int i = 0; i < areaRectangles.size(); i++) {
			AreaRectangle areaRectangle = (AreaRectangle) areaRectangles.get(i);
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			int textX = rect.x + 1;
			int lineY = rect.y + lineHeight - descent + 1;
			repaintString(gc, text, extent.x, textX, rect.y, lineY, selData,
					rect, hover);
		}
	}

	private void repaintString(GC gc, String s, int swidth, int x, int y,
			int lineY, SelectionData selData, Rectangle bounds, boolean hover) {
		boolean rolloverMode = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_HOVER;
		if (selData != null && selData.isEnclosed()) {
			Color savedBg = gc.getBackground();
			Color savedFg = gc.getForeground();
			int leftOffset = selData.getLeftOffset(bounds.height);
			int rightOffset = selData.getRightOffset(bounds.height);
			boolean firstRow = selData.isFirstSelectionRow(bounds.y,
					bounds.height);
			boolean lastRow = selData.isLastSelectionRow(bounds.y,
					bounds.height);
			boolean selectedRow = selData
					.isSelectedRow(bounds.y, bounds.height);

			int sstart = -1;
			int sstop = -1;

			if ((firstRow && x + swidth < leftOffset)
					|| (lastRow && x > rightOffset)) {
				repaintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode);
				return;
			}

			if (firstRow && bounds.x + swidth > leftOffset) {
				sstart = convertOffsetToStringIndex(gc, s, bounds.x, swidth,
						leftOffset);
			}
			if (lastRow && bounds.x + swidth > rightOffset) {
				sstop = convertOffsetToStringIndex(gc, s, bounds.x, swidth,
						rightOffset);
			}

			if (firstRow && sstart != -1) {
				String left = s.substring(0, sstart);
				int width = gc.textExtent(left).x;
				repaintStringSegment(gc, left, width, x, y, lineY, hover, rolloverMode);
				x += width;
			}
			if (selectedRow) {
				int lindex = sstart != -1 ? sstart : 0;
				int rindex = sstop != -1 ? sstop : s.length();
				String mid = s.substring(lindex, rindex);
				Point extent = gc.textExtent(mid);
				gc.setForeground(selData.fg);
				gc.setBackground(selData.bg);
				gc.fillRectangle(x, y, extent.x, extent.y);
				repaintStringSegment(gc, mid, extent.x, x, y, lineY, hover, rolloverMode);
				x += extent.x;
				gc.setForeground(savedFg);
				gc.setBackground(savedBg);
			} else {
				repaintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode);
			}
			if (lastRow && sstop != -1) {
				String right = s.substring(sstop);
				repaintStringSegment(gc, right, gc.textExtent(right).x, x, y, lineY, hover, rolloverMode);
			}
		} else {
			repaintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode);
		}
	}

	/**
	 * @param gc
	 * @param s
	 * @param x
	 * @param y
	 * @param lineY
	 * @param hover
	 * @param rolloverMode
	 */
	private void repaintStringSegment(GC gc, String s, int swidth, int x, int y, int lineY, boolean hover, boolean rolloverMode) {
		boolean reverse = false;
		if (underline || hover || rolloverMode) {
			if (rolloverMode && !hover)
				reverse=true;
		}
		if (reverse) {
			drawUnderline(gc, swidth, x, lineY, hover, rolloverMode);
			gc.drawString(s, x, y, false);
		}
		else {
			gc.drawString(s, x, y, false);
			drawUnderline(gc, swidth, x, lineY, hover, rolloverMode);
		}
	}

	private void drawUnderline(GC gc, int swidth, int x, int y, boolean hover,
			boolean rolloverMode) {
		if (underline || hover || rolloverMode) {
			Color saved = null;
			if (rolloverMode && !hover) {
				saved = gc.getForeground();
				gc.setForeground(gc.getBackground());
			}
			gc.drawLine(x, y, x + swidth, y);
			if (saved != null)
				gc.setForeground(saved);
		}
	}
	public String getTooltipText() {
		return tooltipText;
	}
	public void setTooltipText(String tooltip) {
		this.tooltipText = tooltip;
	}
}