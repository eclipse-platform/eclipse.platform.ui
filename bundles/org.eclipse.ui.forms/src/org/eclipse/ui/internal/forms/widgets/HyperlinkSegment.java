/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
 * @version 	1.0
 * @author
 */
public class HyperlinkSegment
	extends TextSegment {
	private String href;
	private static final String LINK_FG = "c.___link_fg";
	private HyperlinkSettings settings;
	
	public HyperlinkSegment(String text, HyperlinkSettings settings, String fontId) {
		super(text, fontId);
		this.settings = settings;
		underline = settings.getHyperlinkUnderlineMode()==HyperlinkSettings.UNDERLINE_ALWAYS;
	}

	/*
	 * @see IObjectReference#getObjectId()
	 */
	public String getHref() {
		return href;
	}
	
	void setHref(String href) {
		this.href = href;
	}
	public void paint(GC gc, int width, Locator locator, Hashtable resourceTable, boolean selected) {
		resourceTable.put(LINK_FG, settings.getForeground());
		setColorId(LINK_FG);
		super.paint(gc, width, locator, resourceTable, selected);
	}
	
	public void repaint(GC gc, boolean hover) {
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();
		boolean rolloverMode = settings.getHyperlinkUnderlineMode()==HyperlinkSettings.UNDERLINE_HOVER;
		for (int i=0; i<areaRectangles.size(); i++) {
			AreaRectangle areaRectangle = (AreaRectangle)areaRectangles.get(i);
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			int textX = rect.x + 1;
			gc.drawString(text, textX, rect.y, false);
			if (underline || hover || rolloverMode) {
				int lineY = rect.y + lineHeight - descent + 1;
				Color saved=null;
				if (rolloverMode && !hover) {
					saved = gc.getForeground();
					gc.setForeground(gc.getBackground());
				}
				gc.drawLine(textX, lineY, textX+extent.x, lineY);
				if (saved!=null)
					gc.setForeground(saved);
			}
		}
	}
}
