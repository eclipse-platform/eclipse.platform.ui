/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chriss Gross (schtoo@schtoo.com) - fix for 61670
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public interface IHyperlinkSegment extends IFocusSelectable {
	String getHref();
	String getText();
	void paintFocus(GC gc, Color bg, Color fg, boolean selected, Rectangle repaintRegion);
	boolean contains(int x, int y);
	boolean intersects(Rectangle rect);
}
