/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Hashtable;
import org.eclipse.update.ui.forms.internal.*;
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
	private boolean hoover;

	
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
	
	public void setHoover(boolean value) {
		this.hoover = value;
	}
	public boolean isHoover() {
		return hoover;
	}
	
	/*
		public boolean isSelectable() {
		return true;
	}
	*/

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
		setColor(hoover? settings.getActiveForeground() : settings.getForeground());
		super.paint(gc, width, locator, objectTable, selected);
	}
}
