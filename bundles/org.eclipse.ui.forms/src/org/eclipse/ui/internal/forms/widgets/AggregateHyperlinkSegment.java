/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.forms.widgets;

import java.util.*;
import java.util.Hashtable;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AggregateHyperlinkSegment extends ParagraphSegment implements IHyperlinkSegment {
	private String href;
	private boolean wordWrapAllowed=true;
	private Vector segments=new Vector();
	private HyperlinkSettings settings;
	
	public AggregateHyperlinkSegment(HyperlinkSettings settings) {
		this.settings = settings;
	}

	public void add(TextHyperlinkSegment segment) {
		segment.setHref(href);
		segment.setWordWrapAllowed(wordWrapAllowed);
		segments.add(segment);
	}
	public void add(ImageHyperlinkSegment segment) {
		segment.setHref(href);
		segment.setWordWrapAllowed(wordWrapAllowed);
		segments.add(segment);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.ParagraphSegment#advanceLocator(org.eclipse.swt.graphics.GC, int, org.eclipse.ui.internal.forms.widgets.Locator, java.util.Hashtable, boolean)
	 */
	public boolean advanceLocator(GC gc, int wHint, Locator loc,
			Hashtable objectTable, boolean computeHeightOnly) {
		boolean newLine=false;
		for (int i=0; i<segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment)segments.get(i);
			if (segment.advanceLocator(gc, wHint, loc, objectTable, computeHeightOnly))
				newLine = true;
		}
		return newLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.ParagraphSegment#paint(org.eclipse.swt.graphics.GC, int, org.eclipse.ui.internal.forms.widgets.Locator, java.util.Hashtable, boolean, org.eclipse.ui.internal.forms.widgets.SelectionData)
	 */
	public void paint(GC gc, int width, Locator loc, Hashtable resourceTable,
			boolean selected, SelectionData selData) {
		for (int i=0; i<segments.size(); i++) {
			ParagraphSegment segment = (ParagraphSegment)segments.get(i);
			segment.paint(gc, width, loc, resourceTable, selected, selData);
		}
	}
	/**
	 * @return Returns the href.
	 */
	public String getHref() {
		return href;
	}
	/**
	 * @param href The href to set.
	 */
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * @return Returns the wrapAllowed.
	 */
	public boolean isWordWrapAllowed() {
		return wordWrapAllowed;
	}
	/**
	 * @param wrapAllowed The wrapAllowed to set.
	 */
	public void setWordWrapAllowed(boolean wrapAllowed) {
		this.wordWrapAllowed = wrapAllowed;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#repaint(org.eclipse.swt.graphics.GC, boolean)
	 */
	public void repaint(GC gc, boolean hover) {
		for (int i=0; i<segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment)segments.get(i);
			segment.repaint(gc, hover);
		}
	}
	public String getText() {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment)segments.get(i);
			buf.append(segment.getText());
		}
		return buf.toString();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#paintFocus(org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Color, org.eclipse.swt.graphics.Color, boolean)
	 */
	public void paintFocus(GC gc, Color bg, Color fg, boolean selected) {
		for (int i=0; i<segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment)segments.get(i);
			segment.paintFocus(gc, bg, fg, selected);
		}		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#getBounds()
	 */
	public Rectangle getBounds() {
		Rectangle bounds = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
		//TODO this is wrong
		for (int i=0; i<segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment)segments.get(i);
			Rectangle sbounds = segment.getBounds();
			bounds.x = Math.min(bounds.x, sbounds.x);
			bounds.y = Math.min(bounds.y, sbounds.y);
			bounds.width = Math.max(bounds.width, sbounds.width);
			bounds.height = Math.max(bounds.height, sbounds.height);
		}
		return bounds;
	}
	public boolean contains(int x, int y) {
		for (int i=0; i<segments.size(); i++) {
			IHyperlinkSegment segment = (IHyperlinkSegment)segments.get(i);
			if (segment.contains(x, y))
				return true;
		}
		return false;
	}
}