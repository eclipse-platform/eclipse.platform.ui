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

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @version 	1.0
 * @author
 */
public class Paragraph {
	public static final String HTTP = "http://";
	private Vector segments;
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

	/*
	 * @see IParagraph#getSegments()
	 */
	public ParagraphSegment[] getSegments() {
		if (segments == null)
			return new ParagraphSegment[0];
		return (ParagraphSegment[]) segments.toArray(
			new ParagraphSegment[segments.size()]);
	}

	public void addSegment(ParagraphSegment segment) {
		if (segments == null)
			segments = new Vector();
		segments.add(segment);
	}
	
	public void parseRegularText(String text, boolean expandURLs,
			HyperlinkSettings settings, String fontId) {
		parseRegularText(text, expandURLs, settings, fontId, null);
	}
	
	

	public void parseRegularText(
		String text,
		boolean expandURLs,
		HyperlinkSettings settings,
		String fontId,
		String colorId) {
		if (text.length() == 0)
			return;
		if (expandURLs) {
			int loc = text.indexOf(HTTP);

			if (loc == -1)
				addSegment(new TextSegment(text, fontId, colorId));
			else {
				int textLoc = 0;
				while (loc != -1) {
					addSegment(new TextSegment(text.substring(textLoc, loc), fontId, colorId));
					boolean added = false;
					for (textLoc = loc; textLoc < text.length(); textLoc++) {
						char c = text.charAt(textLoc);
						if (Character.isSpaceChar(c)) {
							addHyperlinkSegment(text.substring(loc, textLoc), settings, fontId);
							added = true;
							break;
						}
					}
					if (!added) {
						// there was no space - just end of text
						addHyperlinkSegment(text.substring(loc), settings, fontId);
						break;
					}
					loc = text.indexOf(HTTP, textLoc);
				}
				if (textLoc < text.length()) {
					addSegment(new TextSegment(text.substring(textLoc), fontId, colorId));
				}
			}
		} else {
			addSegment(new TextSegment(text, fontId, colorId));
		}
	}

	private void addHyperlinkSegment(
		String text,
		HyperlinkSettings settings,
		String fontId) {
		HyperlinkSegment hs = new HyperlinkSegment(text, settings, fontId);
		hs.setWordWrapAllowed(false);
		hs.setHref(text);
		addSegment(hs);
	}
	
	protected void computeRowHeights(GC gc, int width, Locator loc, int lineHeight, Hashtable resourceTable) {
		ParagraphSegment [] segments = getSegments();
		// compute heights
		Locator hloc = loc.create();
		ArrayList heights = new ArrayList();
		hloc.heights = heights;
		hloc.rowCounter = 0;
		for (int j = 0; j<segments.length; j++) {
			ParagraphSegment segment = segments[j];
			segment.advanceLocator(gc, width, hloc, resourceTable, true);
		}
		hloc.collectHeights(false);
		loc.heights = heights;
		loc.rowCounter = 0;
	}

	public void paint(
		GC gc,
		int width,
		Locator loc,
		int lineHeight,
		Hashtable resourceTable,
		HyperlinkSegment selectedLink) {
		ParagraphSegment [] segments = getSegments();
		if (segments.length > 0) {
			if (segments[0] instanceof TextSegment
				&& ((TextSegment) segments[0]).isSelectable())
				loc.x += 1;
			// compute heights
			if (loc.heights==null)
				computeRowHeights(gc, width, loc, lineHeight, resourceTable);
			for (int j = 0; j < segments.length; j++) {
				ParagraphSegment segment = segments[j];
				boolean doSelect = false;
				if (selectedLink != null && segment.equals(selectedLink))
					doSelect = true;
				segment.paint(gc, width, loc, resourceTable, doSelect);
			}
			loc.heights = null;
			loc.y += loc.rowHeight;
		} else {
			loc.y += lineHeight;
		}
	}
	public String getAccessibleText() {
		ParagraphSegment [] segments = getSegments();
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		for (int i = 0; i < segments.length; i++) {
			ParagraphSegment segment = segments[i];
			if (segment instanceof TextSegment) {
				String text = ((TextSegment)segment).getText();
				writer.print(text);
			}
		}
		writer.println();
		swriter.flush();
		return swriter.toString();
	}
	public TextSegment findSegmentAt(int x, int y) {
		if (segments!=null) {
			for (int i=0; i<segments.size(); i++) {
				ParagraphSegment segment = (ParagraphSegment)segments.get(i);
				if (segment instanceof TextSegment) {
					TextSegment textSegment = (TextSegment)segment;
					if (textSegment.contains(x, y))
						return textSegment;
				}
			}
		}
		return null;
	}
}
