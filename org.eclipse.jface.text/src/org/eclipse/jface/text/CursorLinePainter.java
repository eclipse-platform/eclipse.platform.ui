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

package org.eclipse.jface.text;


import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * A painter the draws the background of the cursor line in a configured color.
 * Clients usually instantiate and configure object of this class.<p>
 * This class is not intended to be subclassed.
 * 
 * @since 2.1
 */
public class CursorLinePainter implements IPainter, LineBackgroundListener {

	/** The viewer the painter works on */
	private final ISourceViewer fViewer;
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
	 * @param sourceViewer the source viewer for which to create a painter
	 */
	public CursorLinePainter(ISourceViewer sourceViewer) {
		fViewer= sourceViewer;
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
		// don't use cached line information because of asynch painting

		StyledText textWidget= fViewer.getTextWidget();
		if (textWidget != null) {
			
			int caret= textWidget.getCaretOffset();
			int length= event.lineText.length();

			if (event.lineOffset <= caret && caret <= event.lineOffset + length)
				event.lineBackground= fHighlightColor;
			else
				event.lineBackground= textWidget.getBackground();
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
			int lineNumber= document.getLineOfOffset(getModelCaret());
						
			// redraw if the current line number is different from the last line number we painted
			// initially fLastLineNumber is -1
			if (lineNumber != fLastLineNumber) {
				
				fLastLine.offset= fCurrentLine.offset;
				fLastLine.length= fCurrentLine.length;
				fLastLine.isDeleted= fCurrentLine.isDeleted;

				fCurrentLine.isDeleted= false;
				fCurrentLine.offset= document.getLineOffset(lineNumber);
				if (lineNumber == document.getNumberOfLines() - 1)
					fCurrentLine.length= document.getLength() - fCurrentLine.offset;
				else
					fCurrentLine.length=	document.getLineOffset(lineNumber + 1) - fCurrentLine.offset;
				
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
		if (fViewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) fViewer;
			return extension.modelOffset2WidgetOffset(widgetCaret);
		} else {
			IRegion visible= fViewer.getVisibleRegion();
			return widgetCaret + visible.getOffset();
		}
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
		if (fViewer instanceof ITextViewerExtension3) {
			
			ITextViewerExtension3 extension= (ITextViewerExtension3) fViewer;
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
		Point upperLeft= textWidget.getLocationAtOffset(widgetOffset);
		int width= textWidget.getClientArea().width + textWidget.getHorizontalPixel();
		int height= textWidget.getLineHeight();
		textWidget.redraw(0, upperLeft.y, width, height, false);
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
		
		// check selection
		Point selection= fViewer.getTextWidget().getSelectionRange();
		if (selection.y > 0) {
			deactivate(true);
			return;
		}
		
		// initialization
		if (!fIsActive) {
			fViewer.getTextWidget().addLineBackgroundListener(this);
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

	/*
	 * @see IPainter#setPositionManager(IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		fPositionManager = manager;
	}
}
