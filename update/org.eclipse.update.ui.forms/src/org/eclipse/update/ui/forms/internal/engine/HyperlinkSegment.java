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
	private String listenerId;
	private HyperlinkSettings settings;
	
	public HyperlinkSegment(String text, HyperlinkSettings settings) {
		super(text);
		this.settings = settings;
		underline = true;
	}

	/*
	 * @see IHyperlinkSegment#getListener(Hashtable)
	 */
	public IHyperlinkListener getListener(Hashtable objectTable) {
		if (listenerId==null) return null;
		Object obj = objectTable.get(listenerId);
		if (obj==null) return null;
		if (obj instanceof IHyperlinkListener) return (IHyperlinkListener)obj;
		return null;
	}

	/*
	 * @see IObjectReference#getObjectId()
	 */
	public String getObjectId() {
		return listenerId;
	}
	
	void setListenerId(String id) {
		this.listenerId = id;
	}
	public void paint(GC gc, int width, Locator locator, Hashtable objectTable, boolean selected) {
		setColor(settings.getForeground());
		super.paint(gc, width, locator, objectTable, selected);
	}
}
