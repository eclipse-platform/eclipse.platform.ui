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
public interface IParagraphSegment {
	
	public void advanceLocator(GC gc, int wHint, Locator loc, Hashtable objectTable);
	
	public void paint(GC gc, int width, Locator loc, Hashtable objectTable, boolean selected);
}