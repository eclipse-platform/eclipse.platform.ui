/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 291349
 *******************************************************************************/
package org.eclipse.ui.forms;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.forms.widgets.FormsResources;

/**
 * Manages color and underline mode settings for a group of hyperlinks. The
 * class is extended by HyperlinkGroup but is otherwise not intended to be
 * subclassed.
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HyperlinkSettings {
	/**
	 * Underline mode to be used when hyperlinks should not be underlined.
	 */
	public static final int UNDERLINE_NEVER = 1;
	/**
	 * Underline mode to be used when hyperlinks should only be underlined on
	 * mouse hover.
	 */
	public static final int UNDERLINE_HOVER = 2;
	/**
	 * Underline mode to be used when hyperlinks should always be underlined.
	 */
	public static final int UNDERLINE_ALWAYS = 3;
	private int hyperlinkUnderlineMode = UNDERLINE_ALWAYS;
	private Color background;
	private Color foreground;
	private Color activeBackground;
	private Color activeForeground;
	private Cursor busyCursor;
	private Cursor textCursor;
	private Cursor hyperlinkCursor;

	/**
	 * The constructor.
	 *
	 * @param display
	 *            the display to use when creating colors.
	 */
	public HyperlinkSettings(Display display) {
		initializeDefaultForegrounds(display);
		initializeDefaultCursors();
	}

	/**
	 * Initializes the hyperlink foregrounds from the JFace defaults set for the
	 * entire workbench.
	 *
	 * @see JFaceColors
	 * @param display
	 *            the display to use when creating colors
	 */
	public void initializeDefaultForegrounds(Display display) {
		Color fg = JFaceColors.getHyperlinkText(display);
		Color afg = JFaceColors.getActiveHyperlinkText(display);
		if (fg == null) {
			fg = display.getSystemColor(SWT.COLOR_LINK_FOREGROUND);
		}
		setForeground(fg);
		setActiveForeground(afg);
	}

	/**
	 * Initializes the hyperlink cursors from the Forms defaults set for the
	 * entire workbench.
	 *
	 * @since 3.7
	 */
	public void initializeDefaultCursors() {
		setBusyCursor(FormsResources.getBusyCursor());
		setTextCursor(FormsResources.getTextCursor());
		setHyperlinkCursor(FormsResources.getHandCursor());
	}

	/**
	 * Returns the background to use for the active hyperlink.
	 *
	 * @return active hyperlink background
	 */
	public Color getActiveBackground() {
		return activeBackground;
	}

	/**
	 * Returns the foreground to use for the active hyperlink.
	 *
	 * @return active hyperlink foreground
	 */
	public Color getActiveForeground() {
		return activeForeground;
	}

	/**
	 * Returns the background to use for the normal hyperlink.
	 *
	 * @return normal hyperlink background
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Returns the cursor to use when the hyperlink is active. This cursor will
	 * be shown before hyperlink listeners have been notified of hyperlink
	 * activation and hidden when the notification method returns.
	 *
	 * @return the busy cursor
	 */
	public Cursor getBusyCursor() {
		return busyCursor;
	}

	/**
	 * Returns the cursor to use when over text.
	 *
	 * @return the text cursor
	 */
	public Cursor getTextCursor() {
		return textCursor;
	}

	/**
	 * Returns the foreground to use for the normal hyperlink.
	 *
	 * @return the normal hyperlink foreground
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Returns the cursor to use when hovering over the hyperlink.
	 *
	 * @return the hyperlink cursor
	 */
	public Cursor getHyperlinkCursor() {
		return hyperlinkCursor;
	}

	/**
	 * Returns the underline mode to be used for all the hyperlinks in this
	 * group.
	 *
	 * @return one of UNDERLINE_NEVER, UNDERLINE_ALWAYS, UNDERLINE_HOVER
	 */
	public int getHyperlinkUnderlineMode() {
		return hyperlinkUnderlineMode;
	}

	/**
	 * Sets the new active hyperlink background for all the links.
	 *
	 * @param newActiveBackground
	 *            the new active background
	 */
	public void setActiveBackground(Color newActiveBackground) {
		activeBackground = newActiveBackground;
	}

	/**
	 * Sets the new active hyperlink foreground for all the links.
	 *
	 * @param newActiveForeground
	 *            the new active foreground
	 */
	public void setActiveForeground(Color newActiveForeground) {
		activeForeground = newActiveForeground;
	}

	/**
	 * Sets the new hyperlink background for all the links.
	 *
	 * @param newBackground
	 *            the new hyperlink background
	 */
	public void setBackground(Color newBackground) {
		background = newBackground;
	}

	/**
	 * Sets the new hyperlink foreground for all the links.
	 *
	 * @param newForeground
	 *            the new hyperlink foreground
	 */
	public void setForeground(Color newForeground) {
		foreground = newForeground;
	}

	/**
	 * Sets the new hyperlink underline mode for all the links in this group.
	 *
	 * @param mode
	 *            one of <code>UNDERLINE_NEVER</code>,
	 *            <code>UNDERLINE_HOVER</code> and <code>UNDERLINE_ALWAYS</code>
	 *            .
	 */
	public void setHyperlinkUnderlineMode(int mode) {
		hyperlinkUnderlineMode = mode;
	}

	/**
	 * Sets the new cursor to use when the hyperlink is active for all the
	 * links.
	 *
	 * @param newBusyCursor
	 *            the new busy cursor
	 *
	 * @since 3.7
	 */
	public void setBusyCursor(Cursor newBusyCursor) {
		busyCursor = newBusyCursor;
	}

	/**
	 * Sets the new cursor to use when over text for all the links.
	 *
	 * @param newTextCursor
	 *            the new text cursor
	 *
	 * @since 3.7
	 */
	public void setTextCursor(Cursor newTextCursor) {
		textCursor = newTextCursor;
	}

	/**
	 * Sets the new cursor to use when hovering over the hyperlink for all the
	 * links.
	 *
	 * @param newHyperlinkCursor
	 *            the new hyperlink cursor
	 *
	 * @since 3.7
	 */
	public void setHyperlinkCursor(Cursor newHyperlinkCursor) {
		hyperlinkCursor = newHyperlinkCursor;
	}

}
