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
package org.eclipse.ui.forms;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.internal.widgets.*;

/**
 * Manages color and underline mode settings for a group of hyperlinks.
 * 
 * TODO (dejan) - spell out subclass contract
 * TODO (dejan) - mark non-overrideable methods as final
 * @since 3.0
 */
public class HyperlinkSettings {
	public static final int UNDERLINE_NEVER = 1;
	public static final int UNDERLINE_ROLLOVER = 2;
	public static final int UNDERLINE_ALWAYS = 3;

	private int hyperlinkUnderlineMode = UNDERLINE_ALWAYS;
	private Color background;
	private Color foreground;
	private Color activeBackground;
	private Color activeForeground;

	public HyperlinkSettings(Display display) {
		initializeDefaultForegrounds(display);
	}
	
	public void initializeDefaultForegrounds(Display display) {
		setForeground(JFaceColors.getHyperlinkText(display));
		setActiveForeground(JFaceColors.getActiveHyperlinkText(display));
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
		return FormsResources.getBusyCursor();
	}
	public Cursor getTextCursor() {
		return FormsResources.getTextCursor();
	}
	public Color getForeground() {
		return foreground;
	}
	public Cursor getHyperlinkCursor() {
		return FormsResources.getHandCursor();
	}
	public int getHyperlinkUnderlineMode() {
		return hyperlinkUnderlineMode;
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

	public void setHyperlinkUnderlineMode(int mode) {
		hyperlinkUnderlineMode = mode;
	}
}
