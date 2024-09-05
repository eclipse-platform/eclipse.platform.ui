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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @version 1.0
 */
public class Paragraph {
	public static final String[] PROTOCOLS = {"http://", "https://", "ftp://"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private Vector<ParagraphSegment> segments;

	private boolean addVerticalSpace = true;

	public Paragraph(boolean addVerticalSpace) {
		this.addVerticalSpace = addVerticalSpace;
	}

	public int getIndent() {
		return 0;
	}

	public boolean getAddVerticalSpace() {
		return addVerticalSpace;
	}

	public ParagraphSegment[] getSegments() {
		if (segments == null)
			return new ParagraphSegment[0];
		return segments
				.toArray(new ParagraphSegment[segments.size()]);
	}

	public void addSegment(ParagraphSegment segment) {
		if (segments == null)
			segments = new Vector<>();
		segments.add(segment);
	}

	public void parseRegularText(String text, boolean expandURLs, boolean wrapAllowed,
			HyperlinkSettings settings, String fontId) {
		parseRegularText(text, expandURLs, wrapAllowed, settings, fontId, null);
	}

	public void parseRegularText(String text, boolean expandURLs, boolean wrapAllowed,
			HyperlinkSettings settings, String fontId, String colorId) {
		if (text.isEmpty())
			return;
		if (expandURLs) {
			int loc = findUrl(text,0);

			if (loc == -1)
				addSegment(new TextSegment(text, fontId, colorId, wrapAllowed));
			else {
				int textLoc = 0;
				while (loc != -1) {
					addSegment(new TextSegment(text.substring(textLoc, loc),
							fontId, colorId, wrapAllowed));
					boolean added = false;
					for (textLoc = loc; textLoc < text.length(); textLoc++) {
						char c = text.charAt(textLoc);
						if (Character.isSpaceChar(c)) {
							addHyperlinkSegment(text.substring(loc, textLoc),
									settings, fontId);
							added = true;
							break;
						}
					}
					if (!added) {
						// there was no space - just end of text
						addHyperlinkSegment(text.substring(loc), settings,
								fontId);
						break;
					}
					loc = findUrl(text,textLoc);
				}
				if (textLoc < text.length()) {
					addSegment(new TextSegment(text.substring(textLoc), fontId,
							colorId, wrapAllowed));
				}
			}
		} else {
			addSegment(new TextSegment(text, fontId, colorId, wrapAllowed));
		}
	}

	private int findUrl(String text, int startIndex) {
		int[] locs = new int[PROTOCOLS.length];
		for (int i = 0; i < PROTOCOLS.length; i++)
			locs[i] = text.indexOf(PROTOCOLS[i], startIndex);
		Arrays.sort(locs);
		for (int i = 0; i < PROTOCOLS.length; i++)
			if (locs[i] != -1)
				return locs[i];
		return -1;
	}

	private void addHyperlinkSegment(String text, HyperlinkSettings settings,
			String fontId) {
		TextHyperlinkSegment hs = new TextHyperlinkSegment(text, settings,
				fontId);
		hs.setWordWrapAllowed(false);
		hs.setHref(text);
		addSegment(hs);
	}

	protected void computeRowHeights(GC gc, int width, Locator loc,
			int lineHeight, Hashtable<String, Object> resourceTable) {
		ParagraphSegment[] segments = getSegments();
		// compute heights
		Locator hloc = loc.create();
		ArrayList<int[]> heights = new ArrayList<>();
		hloc.heights = heights;
		hloc.rowCounter = 0;
		for (ParagraphSegment segment : segments) {
			segment.advanceLocator(gc, width, hloc, resourceTable, true);
		}
		if (hloc.rowHeight == 0) {
			FontMetrics fm = gc.getFontMetrics();
			hloc.rowHeight = fm.getHeight();
		}
		hloc.collectHeights();
		loc.heights = heights;
		loc.rowCounter = 0;
	}

	public void layout(GC gc, int width, Locator loc, int lineHeight,
			Hashtable<String, Object> resourceTable, IHyperlinkSegment selectedLink) {
		ParagraphSegment[] segments = getSegments();
		//int height;
		if (segments.length > 0) {
			/*
			if (segments[0] instanceof TextSegment
					&& ((TextSegment) segments[0]).isSelectable())
				loc.x += 1;
			*/
			// compute heights
			if (loc.heights == null)
				computeRowHeights(gc, width, loc, lineHeight, resourceTable);
			for (ParagraphSegment segment : segments) {
				boolean doSelect = false;
				if (selectedLink instanceof ParagraphSegment sl && segment.equals(sl))
					doSelect = true;
				segment.layout(gc, width, loc, resourceTable, doSelect);
			}
			loc.heights = null;
			loc.y += loc.rowHeight;
		} else {
			loc.y += lineHeight;
		}
	}

	public void paint(GC gc, Rectangle repaintRegion,
			Hashtable<String, Object> resourceTable, IHyperlinkSegment selectedLink,
			SelectionData selData) {
		ParagraphSegment[] segments = getSegments();

		for (ParagraphSegment segment : segments) {
			if (!segment.intersects(repaintRegion))
				continue;
			boolean doSelect = false;
			if (selectedLink instanceof ParagraphSegment sl && segment.equals(sl))
				doSelect = true;
			segment.paint(gc, false, resourceTable, doSelect, selData, repaintRegion);
		}
	}

	public void computeSelection(GC gc,	Hashtable<String, Object> resourceTable, IHyperlinkSegment selectedLink,
			SelectionData selData) {
		ParagraphSegment[] segments = getSegments();

		for (ParagraphSegment segment : segments) {
			//boolean doSelect = false;
			//if (selectedLink != null && segment.equals(selectedLink))
				//doSelect = true;
			segment.computeSelection(gc, resourceTable, selData);
		}
	}

	public String getAccessibleText() {
		ParagraphSegment[] segments = getSegments();
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		for (ParagraphSegment segment : segments) {
			if (segment instanceof TextSegment) {
				String text = ((TextSegment) segment).getText();
				writer.print(text);
			}
		}
		writer.println();
		swriter.flush();
		return swriter.toString();
	}

	public ParagraphSegment findSegmentAt(int x, int y) {
		if (segments != null) {
			for (ParagraphSegment segment : segments) {
				if (segment.contains(x, y))
					return segment;
			}
		}
		return null;
	}
	public void clearCache(String fontId) {
		if (segments != null) {
			for (ParagraphSegment segment : segments) {
				segment.clearCache(fontId);
			}
		}
	}
}
