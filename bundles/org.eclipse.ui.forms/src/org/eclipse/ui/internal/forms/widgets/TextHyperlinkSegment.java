/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @version 1.0
 * @author
 */
public class TextHyperlinkSegment extends TextSegment implements
		IHyperlinkSegment {
	private String href;

	private String tooltipText;

	//private static final String LINK_FG = "c.___link_fg";

	private HyperlinkSettings settings;

	public TextHyperlinkSegment(String text, HyperlinkSettings settings,
			String fontId) {
		super(text, fontId);
		this.settings = settings;
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

	/*
	 * public void paint(GC gc, int width, Locator locator, Hashtable
	 * resourceTable, boolean selected, SelectionData selData) {
	 * resourceTable.put(LINK_FG, settings.getForeground());
	 * setColorId(LINK_FG); super.paint(gc, width, locator, resourceTable,
	 * selected, selData); }
	 */

	public void paint(GC gc, boolean hover, Hashtable resourceTable,
			boolean selected, SelectionData selData, Rectangle repaintRegion) {
		boolean rolloverMode = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_HOVER;
		underline = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_ALWAYS;
		Color savedFg = gc.getForeground();
		Color newFg = hover ? settings.getActiveForeground() : settings.getForeground();
		if (newFg!=null)
			gc.setForeground(newFg); 
		super.paint(gc, hover, resourceTable, selected, rolloverMode, selData, repaintRegion);
		gc.setForeground(savedFg);
	}
	
	protected void drawText(GC gc, String s, int clipX, int clipY) {
		gc.drawText(s, clipX, clipY, false);
	}

	public String getTooltipText() {
		return tooltipText;
	}

	public void setTooltipText(String tooltip) {
		this.tooltipText = tooltip;
	}
	
	public boolean isSelectable() {
		return true;
	}

	public boolean isFocusSelectable(Hashtable resourceTable) {
		return true;
	}

	public boolean setFocus(Hashtable resourceTable, boolean direction) {
		return true;
	}
}