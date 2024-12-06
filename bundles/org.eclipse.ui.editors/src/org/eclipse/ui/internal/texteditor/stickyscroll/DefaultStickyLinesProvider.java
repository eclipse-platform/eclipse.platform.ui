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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * This class provides sticky lines for the given source code in the source viewer. The
 * implementation is completely based on indentation and therefore works by default for several
 * languages.
 */
public class DefaultStickyLinesProvider implements IStickyLinesProvider {

	private final static int IGNORE_LINE_INDENTATION= -1;

	private final static String TAB= "\t"; //$NON-NLS-1$

	private StickyLinesProperties fProperties;

	@Override
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber, StickyLinesProperties properties) {
		this.fProperties= properties;
		LinkedList<IStickyLine> stickyLines= new LinkedList<>();

		StyledText textWidget= sourceViewer.getTextWidget();
		int textWidgetLineNumber= mapLineNumberToWidget(sourceViewer, lineNumber);
		try {
			int startIndetation= getStartIndentation(textWidgetLineNumber, textWidget);

			for (int i= textWidgetLineNumber, previousIndetation= startIndetation; i >= 0; i--) {
				String line= textWidget.getLine(i);
				int indentation= getIndentation(line);

				if (indentation == IGNORE_LINE_INDENTATION) {
					continue;
				}

				if (indentation < previousIndetation) {
					previousIndetation= indentation;
					stickyLines.addFirst(new StickyLine(mapLineNumberToViewer(sourceViewer, i), sourceViewer));
				}
			}
		} catch (IllegalArgumentException e) {
			stickyLines.clear();
		}

		return stickyLines;
	}

	private int getStartIndentation(int startFromLine, StyledText styledText) {
		int indentation= getIndentation(styledText.getLine(startFromLine));
		if (indentation != IGNORE_LINE_INDENTATION) {
			return indentation;
		} else {
			int nextContentLine= getIndentation(getNextContentLine(startFromLine, styledText));
			int previousContentLine= getIndentation(getPreviousContentLine(startFromLine, styledText));
			return Math.max(nextContentLine, previousContentLine);
		}
	}

	private String getNextContentLine(int startFromLine, StyledText styledText) {
		for (int i= startFromLine + 1; i < styledText.getLineCount(); i++) {
			String line= styledText.getLine(i);
			if (!line.isBlank()) {
				return line;
			}
		}
		return null;
	}

	private String getPreviousContentLine(int startFromLine, StyledText styledText) {
		for (int i= startFromLine - 1; i >= 0; i--) {
			String line= styledText.getLine(i);
			if (!line.isBlank()) {
				return line;
			}
		}
		return null;
	}

	private int getIndentation(String line) {
		if (line == null || line.isBlank()) {
			return IGNORE_LINE_INDENTATION;
		}
		String tabAsSpaces= String.join("", Collections.nCopies(fProperties.tabWith(), " ")); //$NON-NLS-1$ //$NON-NLS-2$

		line= line.replace(TAB, tabAsSpaces);
		return line.length() - line.stripLeading().length();
	}

	private int mapLineNumberToWidget(ISourceViewer sourceViewer, int line) {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.modelLine2WidgetLine(line);
		}
		return line;
	}

	private int mapLineNumberToViewer(ISourceViewer sourceViewer, int line) {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.widgetLine2ModelLine(line);
		}
		return line;
	}

}
