/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;


import org.eclipse.swt.graphics.GC;
import java.util.Hashtable;

/**
 * @version 	1.0
 * @author
 */
public interface IParagraph {
	public int getIndent();
	public IParagraphSegment [] getSegments();
	public void addSegment(IParagraphSegment segment);
	public boolean getAddVerticalSpace();
	public ITextSegment findSegmentAt(int x, int y);
	public void paint(GC gc, int width, Locator loc, int lineHeight, Hashtable objectTable, IHyperlinkSegment selectedLink);
}
