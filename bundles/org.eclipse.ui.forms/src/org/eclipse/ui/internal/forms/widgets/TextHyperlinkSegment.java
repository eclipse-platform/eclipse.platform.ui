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
	
	public void repaint(GC gc, boolean hover, Hashtable resourceTable, boolean selected, SelectionData selData) {
		boolean rolloverMode = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_HOVER;
		Color savedFg = gc.getForeground();
		gc.setForeground(hover ? settings.getActiveForeground() : settings
				.getForeground());		
		super.repaint(gc, hover, resourceTable, selected, rolloverMode, selData);
		gc.setForeground(savedFg);
	}

	public String getTooltipText() {
		return tooltipText;
	}
	public void setTooltipText(String tooltip) {
		this.tooltipText = tooltip;
	}
}