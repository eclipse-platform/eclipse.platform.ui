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
	public List<StickyLine> getStickyLines(ISourceViewer sourceViewer, StickyLinesProperties properties) {
		if (sourceViewer.getTopIndex() == 0) {
			return Collections.emptyList();
		}

		this.fProperties= properties;
		LinkedList<StickyLine> stickyLines= new LinkedList<>();

		try {
			StyledText textWidget= sourceViewer.getTextWidget();
			int startLine= textWidget.getTopIndex();

			calculateStickyLinesForLineNumber(stickyLines, sourceViewer, startLine);
			calculateStickyLinesUnderStickyLineControl(stickyLines, sourceViewer, startLine);
		} catch (IllegalArgumentException e) {
			stickyLines.clear();
		}

		return stickyLines;
	}

	private void calculateStickyLinesForLineNumber(LinkedList<StickyLine> stickyLines, ISourceViewer sourceViewer, int lineNumber) {
		StyledText textWidget= sourceViewer.getTextWidget();
		int startIndetation= getStartIndentation(lineNumber, textWidget);

		for (int i= lineNumber, previousIndetation= startIndetation; i >= 0; i--) {
			String line= textWidget.getLine(i);
			int indentation= getIndentation(line);

			if (indentation == IGNORE_LINE_INDENTATION) {
				continue;
			}

			if (indentation < previousIndetation) {
				previousIndetation= indentation;
				stickyLines.addFirst(new StickyLine(line, mapLineNumberToSourceViewerLine(i, sourceViewer)));
			}
		}
	}

	private void calculateStickyLinesUnderStickyLineControl(LinkedList<StickyLine> stickyLines, ISourceViewer sourceViewer, int startLine) {
		int firstBelowControl= startLine + stickyLines.size();
		StyledText textWidget= sourceViewer.getTextWidget();
		int lineCount= textWidget.getLineCount();

		for (int i= startLine; i < firstBelowControl && i < lineCount; i++) {

			String line= textWidget.getLine(i);
			int indentation= getIndentation(line);
			if (indentation == IGNORE_LINE_INDENTATION) {
				continue;
			}

			while (!stickyLines.isEmpty() && indentation <= getLastStickyLineIndentation(stickyLines) && i < firstBelowControl) {
				stickyLines.removeLast();
				firstBelowControl--;
			}

			String nextContentLine= getNextContentLine(i, textWidget);
			if (getIndentation(nextContentLine) > indentation && i < firstBelowControl) {
				stickyLines.addLast(new StickyLine(line, mapLineNumberToSourceViewerLine(i, sourceViewer)));
				firstBelowControl++;
				continue;
			}
		}
	}

	private int getLastStickyLineIndentation(LinkedList<StickyLine> stickyLines) {
		String text= stickyLines.getLast().text();
		return getIndentation(text);
	}

	private int mapLineNumberToSourceViewerLine(int lineNumber, ISourceViewer sourceViewer) {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.widgetLine2ModelLine(lineNumber);
		}
		return lineNumber;
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

}
