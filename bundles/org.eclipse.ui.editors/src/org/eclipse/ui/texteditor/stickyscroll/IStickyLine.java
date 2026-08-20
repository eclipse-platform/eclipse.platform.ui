/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.stickyscroll;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

/**
 * Representation of a sticky line.
 *
 * @since 3.20
 */
public interface IStickyLine {

	/**
	 * Returns the line number of the sticky line.
	 *
	 * @return the line number of the sticky line
	 */
	int getLineNumber();

	/**
	 * Returns the text of the sticky line.
	 *
	 * @return the text of the sticky line
	 */
	String getText();

	/**
	 * Returns the style ranges of the sticky line.
	 *
	 * @return the style ranges of the sticky line
	 */
	StyleRange[] getStyleRanges();

	/**
	 * Returns the background color of the sticky line.
	 * <p>
	 * The background color is drawn for the full-width of the line. The text
	 * background color if defined in a StyleRange ({@link #getStyleRanges()})
	 * overlays the line background color.
	 * </p>
	 * <p>
	 * {@code null} (the default), if the line has no special background color.
	 * </p>
	 * 
	 * @return the background color of the sticky line or {@code null} for no
	 *         special color
	 * @since 3.22
	 */
	default Color getBackgroundColor() {
		return null;
	}

}
