/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Hashtable;

import org.eclipse.swt.graphics.GC;
import org.eclipse.update.ui.forms.internal.IHyperlinkListener;

/**
 * @version 	1.0
 * @author
 */
public interface IHyperlinkSegment extends ITextSegment, IObjectReference {
	HyperlinkAction getAction(Hashtable objectTable);
	
	boolean contains(int x, int y);
	
	public void repaint(GC gc, boolean hover);
}