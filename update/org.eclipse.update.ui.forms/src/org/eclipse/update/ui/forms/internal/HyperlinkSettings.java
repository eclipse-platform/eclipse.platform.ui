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
package org.eclipse.update.ui.forms.internal;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

public class HyperlinkSettings {
	public static final int UNDERLINE_NEVER = 1;
	public static final int UNDERLINE_ROLLOVER = 2;
	public static final int UNDERLINE_ALWAYS = 3;

	protected boolean hyperlinkCursorUsed = true;
	protected int hyperlinkUnderlineMode = UNDERLINE_ALWAYS;
	protected Color background;
	protected Color foreground;
	protected Color activeBackground;
	protected Color activeForeground;
	protected static Cursors cursors = new Cursors();

	static class Cursors {
		Cursor hyperlinkCursor;
		Cursor busyCursor;
		Cursor textCursor;
		int counter = 0;

		public void allocate() {
			if (counter == 0) {
				hyperlinkCursor =
					new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
				busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
				textCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_IBEAM);
			}
			counter++;
		}
		public void dispose() {
			counter--;
			if (counter == 0) {
				hyperlinkCursor.dispose();
				busyCursor.dispose();
				textCursor.dispose();
				hyperlinkCursor = null;
				busyCursor = null;
				textCursor = null;
			}
		}
	}

	public HyperlinkSettings() {
		cursors.allocate();
	}

	public void dispose() {
		if (cursors != null)
			cursors.dispose();
	}

	public Color getActiveBackground() {
		return activeBackground;
	}
	public Color getActiveForeground() {
		return activeForeground;
	}
	public Color getBackground() {
		return background;
	}
	public Cursor getBusyCursor() {
		return cursors.busyCursor;
	}
	public Cursor getTextCursor() {
		return cursors.textCursor;
	}
	public Color getForeground() {
		return foreground;
	}
	public Cursor getHyperlinkCursor() {
		return cursors.hyperlinkCursor;
	}
	public int getHyperlinkUnderlineMode() {
		return hyperlinkUnderlineMode;
	}

	public boolean isHyperlinkCursorUsed() {
		return hyperlinkCursorUsed;
	}

	public void setActiveBackground(Color newActiveBackground) {
		activeBackground = newActiveBackground;
	}
	public void setActiveForeground(Color newActiveForeground) {
		activeForeground = newActiveForeground;
	}
	public void setBackground(Color newBackground) {
		background = newBackground;
	}
	public void setForeground(Color newForeground) {
		foreground = newForeground;
	}
	public void setHyperlinkCursorUsed(boolean newHyperlinkCursorUsed) {
		hyperlinkCursorUsed = newHyperlinkCursorUsed;
	}
	public void setHyperlinkUnderlineMode(int newHyperlinkUnderlineMode) {
		hyperlinkUnderlineMode = newHyperlinkUnderlineMode;
	}

}
