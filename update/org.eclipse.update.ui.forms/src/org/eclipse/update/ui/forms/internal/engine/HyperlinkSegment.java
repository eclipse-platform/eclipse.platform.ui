/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Hashtable;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.GC;

/**
 * @version 	1.0
 * @author
 */
public class HyperlinkSegment
	extends TextSegment
	implements IHyperlinkSegment {
	private String actionId;
	private HyperlinkSettings settings;
	
	public HyperlinkSegment(String text, HyperlinkSettings settings, String fontId) {
		super(text, fontId);
		this.settings = settings;
		underline = true;
	}
	
	/*
	 * @see IHyperlinkSegment#getListener(Hashtable)
	 */
	public HyperlinkAction getAction(Hashtable objectTable) {
		if (actionId==null) return null;
		Object obj = objectTable.get(actionId);
		if (obj==null) return null;
		if (obj instanceof HyperlinkAction) return (HyperlinkAction)obj;
		return null;
	}
	
	/*
	 * @see IObjectReference#getObjectId()
	 */
	public String getObjectId() {
		return actionId;
	}
	
	void setActionId(String id) {
		this.actionId = id;
	}
	public void paint(GC gc, int width, Locator locator, Hashtable objectTable, boolean selected) {
		setColor(settings.getForeground());
		super.paint(gc, width, locator, objectTable, selected);
	}
	
	public void repaint(GC gc, boolean hover) {
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();
		for (int i=0; i<areaRectangles.size(); i++) {
			AreaRectangle areaRectangle = (AreaRectangle)areaRectangles.get(i);
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			int textX = rect.x + 1;
			gc.drawString(text, textX, rect.y, true);
			if (underline || hover) {
				int lineY = rect.y + lineHeight - descent + 1;
				gc.drawLine(textX, lineY, textX+extent.x, lineY);
			}
		}
	}
}
