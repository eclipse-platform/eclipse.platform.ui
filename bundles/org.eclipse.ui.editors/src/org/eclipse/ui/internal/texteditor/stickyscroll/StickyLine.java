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

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

/**
 * Default implementation of {@link IStickyLine}. Information about the text and style ranges are
 * calculated from the given text widget.
 */
public class StickyLine implements IStickyLine {

	private int lineNumber;

	private String text;

	private StyledText textWidget;

	public StickyLine(int lineNumber, StyledText textWidget) {
		this.lineNumber= lineNumber;
		this.textWidget= textWidget;
	}

	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public String getText() {
		if (text == null) {
			text= textWidget.getLine(lineNumber);
		}
		return text;
	}

	@Override
	public StyleRange[] getStyleRanges() {
		int offsetAtLine= textWidget.getOffsetAtLine(lineNumber);
		StyleRange[] styleRanges= textWidget.getStyleRanges(offsetAtLine, getText().length());
		for (StyleRange styleRange : styleRanges) {
			styleRange.start= styleRange.start - offsetAtLine;
		}
		return styleRanges;
	}

}
