/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.GC;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IHyperlinkSegment {
	String getHref();
	String getText();
	boolean isWordWrapAllowed();
	void repaint(GC gc, boolean hover, SelectionData selData);
	void paintFocus(GC gc, Color bg, Color fg, boolean selected);
	Rectangle getBounds();
	boolean contains(int x, int y);
}