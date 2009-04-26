/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;


/**
 * A painter the draws the background of the caret line in a configured color.
 * <p>
 * Clients usually instantiate and configure object of this class.</p>
 * <p>
 * This class is not intended to be subclassed.</p>
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CursorLinePainter implements IPainter, LineBackgroundListener {

	/** The viewer the painter works on */
	private final ITextViewer fViewer;
	/** The cursor line back ground color */
	private Color fHighlightColor;
	/** The paint position manager for managing the line coordinates */
	private IPaintPositionManager fPositionManager;

	/** Keeps track of the line to be painted */
	private Position fCurrentLine= new Position(0, 0);
	/** Keeps track of the line to be cleared */
	private Position fLastLine= new Position(0, 0);
	/** Keeps track of the line number of the last painted line */
	private int fLastLineNumber= -1;
	/** Indicates whether this painter is active */
	private boolean fIsActive;

	/**
	 * Creates a new painter for the given source viewer.
	 *
	 * @param textViewer the source viewer for which to create a painter
	 */
	public CursorLinePainter(ITextViewer textViewer) {
		fViewer= textViewer;
	}

	/**
	 * Sets the color in which to draw the background of the cursor line.
	 *
	 * @param highlightColor the color in which to draw the background of the cursor line
	 */
	public void setHighlightColor(Color highlightColor) {
		fHighlightColor= highlightColor;
	}

	/*
	 * @see LineBackgroundListener#lineGetBackground(LineBackgroundEvent)
	 */
	public void lineGetBackground(LineBackgroundEvent event) {
		// don't use cached line information because of asynchronous painting

		StyledText textWidget= fViewer.getTextWidget();
		if (textWidget != null) {

			int caret= textWidget.getCaretOffset();
			int length= event.lineText.length();

			if (event.lineOffset <= caret && caret <= event.lineOffset + length && !hasMultiLineSelection(textWidget))
				event.lineBackground= fHighlightColor;
		}
	}

	/**
	 * Updates all the cached information about the lines to be painted and to be cleared. Returns <code>true</code>
	 * if the line number of the cursor line has changed.
	 *
	 * @return <code>true</code> if cursor line changed
	 */
	private boolean updateHighlightLine() {
		try {

			IDocument document= fViewer.getDocument();
			int modelCaret= getModelCaret();
			int lineNumber= document.getLineOfOffset(modelCaret);

			// redraw if the current line number is different from the last line number we painted
			// initially fLastLineNumber is -1
			if (lineNumber != fLastLineNumber || !fCurrentLine.overlapsWith(modelCaret, 0)) {

				fLastLine.offset= fCurrentLine.offset;
				fLastLine.length= fCurrentLine.length;
				fLastLine.isDeleted= fCurrentLine.isDeleted;

				if (fCurrentLine.isDeleted) {
					fCurrentLine.isDeleted= false;
					fPositionManager.managePosition(fCurrentLine);
				}

				fCurrentLine.offset= document.getLineOffset(lineNumber);
				if (lineNumber == document.getNumberOfLines() - 1)
					fCurrentLine.length= document.getLength() - fCurrentLine.offset;
				else
					fCurrentLine.length= document.getLineOffset(lineNumber + 1) - fCurrentLine.offset;

				fLastLineNumber= lineNumber;
				return true;

			}

		} catch (BadLocationException e) {
		}

		return false;
	}

	/**
	 * Returns the location of the caret as offset in the source viewer's
	 * input document.
	 *
	 * @return the caret location
	 */
	private int getModelCaret() {
		int widgetCaret= fViewer.getTextWidget().getCaretOffset();
		if (fViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fViewer;
			return extension.widgetOffset2ModelOffset(widgetCaret);
		}
		IRegion visible= fViewer.getVisibleRegion();
		return widgetCaret + visible.getOffset();
	}

	/**
	 * Assumes the given position to specify offset and length of a line to be painted.
	 *
	 * @param position the specification of the line  to be painted
	 */
	private void drawHighlightLine(Position position) {

		// if the position that is about to be drawn was deleted then we can't
		if (position.isDeleted())
			return;

		int widgetOffset= 0;
		if (fViewer instanceof ITextViewerExtension5) {

			ITextViewerExtension5 extension= (ITextViewerExtension5) fViewer;
			widgetOffset= extension.modelOffset2WidgetOffset(position.getOffset());
			if (widgetOffset == -1)
				return;

		} else {

			IRegion visible= fViewer.getVisibleRegion();
			widgetOffset= position.getOffset() - visible.getOffset();
			if (widgetOffset < 0 || visible.getLength() < widgetOffset )
				return;
		}

		StyledText textWidget= fViewer.getTextWidget();
		// check for https://bugs.eclipse.org/bugs/show_bug.cgi?id=64898
		// this is a guard against the symptoms but not the actual solution
		if (0 <= widgetOffset && widgetOffset <= textWidget.getCharCount()) {
			Point upperLeft= textWidget.getLocationAtOffset(widgetOffset);
			int width= textWidget.getClientArea().width + textWidget.getHorizontalPixel();
			int height= textWidget.getLineHeight(widgetOffset);
			textWidget.redraw(0, upperLeft.y, width, height, false);
		}
	}

	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;

			/* on turning off the feature one has to paint the currently
			 * highlighted line with the standard background color
			 */
			if (redraw)
				drawHighlightLine(fCurrentLine);

			fViewer.getTextWidget().removeLineBackgroundListener(this);

			if (fPositionManager != null)
				fPositionManager.unmanagePosition(fCurrentLine);

			fLastLineNumber= -1;
			fCurrentLine.offset= 0;
			fCurrentLine.length= 0;
		}
	}

	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		if (fViewer.getDocument() == null) {
			deactivate(false);
			return;
		}

		StyledText textWidget= fViewer.getTextWidget();

		// check selection
		if (hasMultiLineSelection(textWidget)) {
			deactivate(true);
			return;
		}

		// initialization
		if (!fIsActive) {
			textWidget.addLineBackgroundListener(this);
			fPositionManager.managePosition(fCurrentLine);
			fIsActive= true;
		}

		//redraw line highlight only if it hasn't been drawn yet on the respective line
		if (updateHighlightLine()) {
			// clear last line
			drawHighlightLine(fLastLine);
			// draw new line
			drawHighlightLine(fCurrentLine);
		}
	}

	/**
	 * Returns <code>true</code> if the widget has a selection spanning multiple lines,
	 * <code>false</code> otherwise.
	 * 
	 * @param textWidget the text widget to check
	 * @return <code>true</code> if <code>textWidget</code> has a multiline selection,
	 *         <code>false</code> otherwise
	 * @since 3.5
	 */
	private boolean hasMultiLineSelection(StyledText textWidget) {
		Point selection= textWidget.getSelection();
		try {
			int startLine= textWidget.getLineAtOffset(selection.x);
			int endLine= textWidget.getLineAtOffset(selection.y);
			return startLine != endLine;
		} catch (IllegalArgumentException e) {
			// ignore - apparently, the widget has a stale selection
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=273721
			return false;
		}
	}

	/*
	 * @see IPainter#setPositionManager(IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		fPositionManager = manager;
	}
}
