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

import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Default implementation of {@link IStickyLine}. Information about the text and style ranges are
 * calculated from the given text widget.
 */
public class StickyLine implements IStickyLine {

	protected int lineNumber;

	protected String text;

	protected ISourceViewer sourceViewer;

	public StickyLine(int lineNumber, ISourceViewer sourceViewer) {
		this.lineNumber= lineNumber;
		this.sourceViewer= sourceViewer;
	}

	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public String getText() {
		if (text == null) {
			StyledText textWidget= sourceViewer.getTextWidget();
			text= textWidget.getLine(getWidgetLineNumber());
		}
		return text;
	}

	@Override
	public StyleRange[] getStyleRanges() {
		StyledText textWidget= sourceViewer.getTextWidget();
		int offsetAtLine= textWidget.getOffsetAtLine(getWidgetLineNumber());
		StyleRange[] styleRanges= textWidget.getStyleRanges(offsetAtLine, getText().length());
		for (StyleRange styleRange : styleRanges) {
			styleRange.start= styleRange.start - offsetAtLine;
		}
		return styleRanges;
	}

	private int getWidgetLineNumber() {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.modelLine2WidgetLine(lineNumber);
		}
		return lineNumber;
	}

}
