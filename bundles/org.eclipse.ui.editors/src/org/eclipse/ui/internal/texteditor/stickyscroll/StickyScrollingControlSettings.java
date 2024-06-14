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
package org.eclipse.ui.internal.texteditor.stickyscroll;

import org.eclipse.swt.graphics.Color;

/**
 * A record representing the settings of a sticky scrolling control. It encapsulates all the
 * configurable items like maximum count of sticky lines that are allowed, color of the line numbers
 * and sticky line hover color.
 * 
 * @param maxCountStickyLines The maximum number of sticky lines that should be shown
 * @param lineNumberColor The color used to display line numbers
 * @param stickyLineHoverColor The color used to display sticky lines while hovering over them
 * @param stickyLineBackgroundColor The color used to display sticky lines back ground color
 * @param stickyLinesSeparatorColor The color used to display sticky lines separator color
 * @param showLineNumbers Specifies if line numbers should be showed for sticky scrolling
 */
public record StickyScrollingControlSettings(
		int maxCountStickyLines,
		Color lineNumberColor,
		Color stickyLineHoverColor,
		Color stickyLineBackgroundColor,
		Color stickyLinesSeparatorColor,
		boolean showLineNumbers) {
}
