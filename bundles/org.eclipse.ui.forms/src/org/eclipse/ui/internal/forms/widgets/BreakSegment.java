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

import org.eclipse.swt.graphics.GC;

/**
 * This segment serves as break within a paragraph. It has no data -
 * just starts a new line and resets the locator.
 */

public class BreakSegment extends ParagraphSegment {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.internal.widgets.ParagraphSegment#advanceLocator(org.eclipse.swt.graphics.GC, int, org.eclipse.ui.forms.internal.widgets.Locator, java.util.Hashtable)
	 */
	public boolean advanceLocator(GC gc, int wHint, Locator locator,
			Hashtable objectTable, boolean computeHeightOnly) {
		if (!computeHeightOnly) {
			locator.x = locator.indent;
			locator.y += locator.rowHeight;
			locator.rowHeight = 0;
		}
		locator.rowCounter++;		
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.internal.widgets.ParagraphSegment#paint(org.eclipse.swt.graphics.GC, int, org.eclipse.ui.forms.internal.widgets.Locator, java.util.Hashtable, boolean)
	 */
	public void paint(GC gc, int width, Locator locator, Hashtable resourceTable,
			boolean selected) {
		locator.resetCaret();
		locator.y += locator.rowHeight;
		locator.rowHeight = 0;
		locator.rowCounter++;		
	}
}
