/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc., IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=22712
 *     Anton Leherbauer (Wind River Systems) - [painting] Long lines take too long to display when "Show Whitespace Characters" is enabled - https://bugs.eclipse.org/bugs/show_bug.cgi?id=196116
 *     Anton Leherbauer (Wind River Systems) - [painting] Whitespace characters not drawn when scrolling to right slowly - https://bugs.eclipse.org/bugs/show_bug.cgi?id=206633
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * A painter for drawing visible characters for (invisible) whitespace
 * characters.
 *
 * @since 3.3
 */
public class WhitespaceCharacterPainter implements IPainter, PaintListener {

	private static final char SPACE_SIGN= '\u00b7';
	private static final char IDEOGRAPHIC_SPACE_SIGN= '\u00b0';
	private static final char TAB_SIGN= '\u00bb';
	private static final char CARRIAGE_RETURN_SIGN= '\u00a4';
	private static final char LINE_FEED_SIGN= '\u00b6';

	/** Indicates whether this painter is active. */
	private boolean fIsActive= false;
	/** The source viewer this painter is attached to. */
	private ITextViewer fTextViewer;
	/** The viewer's widget. */
	private StyledText fTextWidget;
	/** Tells whether the advanced graphics sub system is available. */
	private final boolean fIsAdvancedGraphicsPresent;
	/**
	 * Tells whether the text widget was created with the full selection style bit or not.
	 * @since 3.7
	 */
	private final boolean fIsFullSelectionStyle;
	/** @since 3.7 */
	private boolean fShowLeadingSpaces= true;
	/** @since 3.7 */
	private boolean fShowEnclosedSpace= true;
	/** @since 3.7 */
	private boolean fShowTrailingSpaces= true;
	/** @since 3.7 */
	private boolean fShowLeadingIdeographicSpaces= true;
	/** @since 3.7 */
	private boolean fShowEnclosedIdeographicSpaces= true;
	/** @since 3.7 */
	private boolean fShowTrailingIdeographicSpaces= true;
	/** @since 3.7 */
	private boolean fShowLeadingTabs= true;
	/** @since 3.7 */
	private boolean fShowEnclosedTabs= true;
	/** @since 3.7 */
	private boolean fShowTrailingTabs= true;
	/** @since 3.7 */
	private boolean fShowCarriageReturn= true;
	/** @since 3.7 */
	private boolean fShowLineFeed= true;
	/** @since 3.7 */
	private int fAlpha= 80;

	/**
	 * Creates a new painter for the given text viewer.
	 *
	 * @param textViewer  the text viewer the painter should be attached to
	 */
	public WhitespaceCharacterPainter(ITextViewer textViewer) {
		super();
		fTextViewer= textViewer;
		fTextWidget= textViewer.getTextWidget();
		GC gc= new GC(fTextWidget);
		gc.setAdvanced(true);
		fIsAdvancedGraphicsPresent= gc.getAdvanced();
		gc.dispose();
		fIsFullSelectionStyle= (fTextWidget.getStyle() & SWT.FULL_SELECTION) != SWT.NONE;
	}

	/**
	 * Creates a new painter for the given text viewer and the painter options.
	 * 
	 * @param viewer the text viewer the painter should be attached to
	 * @param showLeadingSpaces if <code>true</code>, show leading Spaces
	 * @param showEnclosedSpaces if <code>true</code>, show enclosed Spaces
	 * @param showTrailingSpaces if <code>true</code>, show trailing Spaces
	 * @param showLeadingIdeographicSpaces if <code>true</code>, show leading Ideographic Spaces
	 * @param showEnclosedIdeographicSpaces if <code>true</code>, show enclosed Ideographic Spaces
	 * @param showTrailingIdeographicSpace if <code>true</code>, show trailing Ideographic Spaces
	 * @param showLeadingTabs if <code>true</code>, show leading Tabs
	 * @param showEnclosedTabs if <code>true</code>, show enclosed Tabs
	 * @param showTrailingTabs if <code>true</code>, show trailing Tabs
	 * @param showCarriageReturn if <code>true</code>, show Carriage Returns
	 * @param showLineFeed if <code>true</code>, show Line Feeds
	 * @param alpha the alpha value
	 * @since 3.7
	 */
	public WhitespaceCharacterPainter(ITextViewer viewer, boolean showLeadingSpaces, boolean showEnclosedSpaces, boolean showTrailingSpaces, boolean showLeadingIdeographicSpaces,
			boolean showEnclosedIdeographicSpaces, boolean showTrailingIdeographicSpace, boolean showLeadingTabs,
			boolean showEnclosedTabs, boolean showTrailingTabs, boolean showCarriageReturn, boolean showLineFeed, int alpha) {
		this(viewer);
		fShowLeadingSpaces= showLeadingSpaces;
		fShowEnclosedSpace= showEnclosedSpaces;
		fShowTrailingSpaces= showTrailingSpaces;
		fShowLeadingIdeographicSpaces= showLeadingIdeographicSpaces;
		fShowEnclosedIdeographicSpaces= showEnclosedIdeographicSpaces;
		fShowTrailingIdeographicSpaces= showTrailingIdeographicSpace;
		fShowLeadingTabs= showLeadingTabs;
		fShowEnclosedTabs= showEnclosedTabs;
		fShowTrailingTabs= showTrailingTabs;
		fShowCarriageReturn= showCarriageReturn;
		fShowLineFeed= showLineFeed;
		fAlpha= alpha;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		fTextViewer= null;
		fTextWidget= null;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {
		IDocument document= fTextViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addPaintListener(this);
			redrawAll();
		} else if (reason == CONFIGURATION || reason == INTERNAL) {
			redrawAll();
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removePaintListener(this);
			if (redraw) {
				redrawAll();
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		// no need for a position manager
	}

	/*
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null) {
			handleDrawRequest(event.gc, event.x, event.y, event.width, event.height);
		}
	}

	/*
	 * Draw characters in view range.
	 */
	private void handleDrawRequest(GC gc, int x, int y, int w, int h) {
		int startLine= fTextWidget.getLineIndex(y);
		int endLine= fTextWidget.getLineIndex(y + h - 1);
		if (startLine <= endLine && startLine < fTextWidget.getLineCount()) {
			
			// avoid painting into the margins:
			Rectangle clipping= gc.getClipping();
			Rectangle clientArea= fTextWidget.getClientArea();
			int leftMargin= fTextWidget.getLeftMargin();
			int rightMargin= fTextWidget.getRightMargin();
			clientArea.x+= leftMargin;
			clientArea.width-= leftMargin + rightMargin;
			clipping.intersect(clientArea);
			gc.setClipping(clientArea);
			if (fIsAdvancedGraphicsPresent) {
				int alpha= gc.getAlpha();
				gc.setAlpha(fAlpha);
				drawLineRange(gc, startLine, endLine, x, w);
				gc.setAlpha(alpha);
			} else {
				drawLineRange(gc, startLine, endLine, x, w);
			}
			gc.setClipping(clipping);
		}
	}

	/**
	 * Draw the given line range.
	 *
	 * @param gc the GC
	 * @param startLine first line number
	 * @param endLine last line number (inclusive)
	 * @param x the X-coordinate of the drawing range
	 * @param w the width of the drawing range
	 */
	private void drawLineRange(GC gc, int startLine, int endLine, int x, int w) {
		final int viewPortWidth= fTextWidget.getClientArea().width;
		for (int line= startLine; line <= endLine; line++) {
			int lineOffset= fTextWidget.getOffsetAtLine(line);
			// line end offset including line delimiter
			int lineEndOffset;
			if (line < fTextWidget.getLineCount() - 1) {
				lineEndOffset= fTextWidget.getOffsetAtLine(line + 1);
			} else {
				lineEndOffset= fTextWidget.getCharCount();
			}
			// line length excluding line delimiter
			int lineLength= lineEndOffset - lineOffset;
			while (lineLength > 0) {
				char c= fTextWidget.getTextRange(lineOffset + lineLength - 1, 1).charAt(0);
				if (c != '\r' && c != '\n') {
					break;
				}
				--lineLength;
			}
			// compute coordinates of last character on line
			Point endOfLine= fTextWidget.getLocationAtOffset(lineOffset + lineLength);
			if (x - endOfLine.x > viewPortWidth) {
				// line is not visible
				continue;
			}
			// Y-coordinate of line
			int y= fTextWidget.getLinePixel(line);
			// compute first visible char offset
			int startOffset;
			try {
				startOffset= fTextWidget.getOffsetAtLocation(new Point(x, y)) - 1;
				if (startOffset - 2 <= lineOffset) {
					startOffset= lineOffset;
				}
			} catch (IllegalArgumentException iae) {
				startOffset= lineOffset;
			}
			// compute last visible char offset
			int endOffset;
			if (x + w >= endOfLine.x) {
				// line end is visible
				endOffset= lineEndOffset;
			} else {
				try {
					endOffset= fTextWidget.getOffsetAtLocation(new Point(x + w - 1, y)) + 1;
					if (endOffset + 2 >= lineEndOffset) {
						endOffset= lineEndOffset;
					}
				} catch (IllegalArgumentException iae) {
					endOffset= lineEndOffset;
				}
			}
			// draw character range
			if (endOffset > startOffset) {
				drawCharRange(gc, startOffset, endOffset, lineOffset, lineEndOffset);
			}
		}
	}

	private boolean isWhitespaceCharacter(char c) {
		return c == ' ' || c == '\u3000' || c == '\t' || c == '\r' || c == '\n';
	}

	/**
	 * Draw characters of content range.
	 * 
	 * @param gc the GC
	 * @param startOffset inclusive start index of the drawing range
	 * @param endOffset exclusive end index of the drawing range
	 * @param lineOffset inclusive start index of the line
	 * @param lineEndOffset exclusive end index of the line
	 */
	private void drawCharRange(GC gc, int startOffset, int endOffset, int lineOffset, int lineEndOffset) {
		StyledTextContent content= fTextWidget.getContent();
		String lineText= content.getTextRange(lineOffset, lineEndOffset - lineOffset);
		int startOffsetInLine= startOffset - lineOffset;
		int endOffsetInLine= endOffset - lineOffset;

		int textBegin= -1;
		for (int i= 0; i < lineText.length(); ++i) {
			if (!isWhitespaceCharacter(lineText.charAt(i))) {
				textBegin= i;
				break;
			}
		}
		boolean isEmptyLine= textBegin == -1;
		int textEnd= lineText.length() - 1;
		if (!isEmptyLine) {
			for (int i= lineText.length() - 1; i >= 0; --i) {
				if (!isWhitespaceCharacter(lineText.charAt(i))) {
					textEnd= i;
					break;
				}
			}
		}

		StyleRange styleRange= null;
		Color fg= null;
		StringBuffer visibleChar= new StringBuffer(10);
		for (int textOffset= startOffsetInLine; textOffset <= endOffsetInLine; ++textOffset) {
			int delta= 0;
			boolean eol= false;
			if (textOffset < endOffsetInLine) {
				delta= 1;
				char c= lineText.charAt(textOffset);
				switch (c) {
					case ' ':
						if (isEmptyLine) {
							if (fShowLeadingSpaces || fShowEnclosedSpace || fShowTrailingSpaces) {
								visibleChar.append(SPACE_SIGN);
							}
						} else if (textOffset < textBegin) {
							if (fShowLeadingSpaces) {
								visibleChar.append(SPACE_SIGN);
							}
						} else if (textOffset < textEnd) {
							if (fShowEnclosedSpace) {
								visibleChar.append(SPACE_SIGN);
							}
						} else {
							if (fShowTrailingSpaces) {
								visibleChar.append(SPACE_SIGN);
							}
						}
						// 'continue' would improve performance but may produce drawing errors
						// for long runs of space if width of space and dot differ
						break;
					case '\u3000': // ideographic whitespace
						if (isEmptyLine) {
							if (fShowLeadingIdeographicSpaces || fShowEnclosedIdeographicSpaces || fShowTrailingIdeographicSpaces) {
								visibleChar.append(IDEOGRAPHIC_SPACE_SIGN);
							}
						} else if (textOffset < textBegin) {
							if (fShowLeadingIdeographicSpaces) {
								visibleChar.append(IDEOGRAPHIC_SPACE_SIGN);
							}
						} else if (textOffset < textEnd) {
							if (fShowEnclosedIdeographicSpaces) {
								visibleChar.append(IDEOGRAPHIC_SPACE_SIGN);
							}
						} else {
							if (fShowTrailingIdeographicSpaces) {
								visibleChar.append(IDEOGRAPHIC_SPACE_SIGN);
							}
						}
						// 'continue' would improve performance but may produce drawing errors
						// for long runs of space if width of space and dot differ
						break;
					case '\t':
						if (isEmptyLine) {
							if (fShowLeadingTabs || fShowEnclosedTabs || fShowTrailingTabs) {
								visibleChar.append(TAB_SIGN);
							}
						} else if (textOffset < textBegin) {
							if (fShowLeadingTabs) {
								visibleChar.append(TAB_SIGN);
							}
						} else if (textOffset < textEnd) {
							if (fShowEnclosedTabs) {
								visibleChar.append(TAB_SIGN);
							}
						} else {
							if (fShowTrailingTabs) {
								visibleChar.append(TAB_SIGN);
							}
						}
						break;
					case '\r':
						if (fShowCarriageReturn) {
							visibleChar.append(CARRIAGE_RETURN_SIGN);
						}
						if (textOffset >= endOffsetInLine - 1 || lineText.charAt(textOffset + 1) != '\n') {
							eol= true;
							break;
						}
						continue;
					case '\n':
						if (fShowLineFeed) {
							visibleChar.append(LINE_FEED_SIGN);
						}
						eol= true;
						break;
					default:
						delta= 0;
						break;
				}
			}
			if (visibleChar.length() > 0) {
				int widgetOffset= startOffset + textOffset - startOffsetInLine - visibleChar.length() + delta;
				if (!eol || !isFoldedLine(content.getLineAtOffset(widgetOffset))) {
					/*
					 * Block selection is drawn using alpha and no selection-inverting
					 * takes place, we always draw as 'unselected' in block selection mode.
					 */
					if (!fTextWidget.getBlockSelection() && fIsFullSelectionStyle && isOffsetSelected(fTextWidget, widgetOffset)) {
						fg= fTextWidget.getSelectionForeground();
					} else if (styleRange == null || styleRange.start + styleRange.length <= widgetOffset) {
						styleRange= fTextWidget.getStyleRangeAtOffset(widgetOffset);
						if (styleRange == null || styleRange.foreground == null) {
							fg= fTextWidget.getForeground();
						} else {
							fg= styleRange.foreground;
						}
					}
					draw(gc, widgetOffset, visibleChar.toString(), fg);
				}
				visibleChar.delete(0, visibleChar.length());
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if <code>offset</code> is selection in <code>widget</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @param widget the widget
	 * @param offset the offset
	 * @return <code>true</code> if <code>offset</code> is selection, <code>false</code> otherwise
	 * @since 3.5
	 */
	private static final boolean isOffsetSelected(StyledText widget, int offset) {
		Point selection= widget.getSelection();
		return offset >= selection.x && offset < selection.y;
	}

	/**
	 * Check if the given widget line is a folded line.
	 *
	 * @param widgetLine  the widget line number
	 * @return <code>true</code> if the line is folded
	 */
	private boolean isFoldedLine(int widgetLine) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)fTextViewer;
			int modelLine= extension.widgetLine2ModelLine(widgetLine);
			int widgetLine2= extension.modelLine2WidgetLine(modelLine + 1);
			return widgetLine2 == -1;
		}
		return false;
	}

	/**
	 * Redraw all of the text widgets visible content.
	 */
	private void redrawAll() {
		fTextWidget.redraw();
	}

	/**
	 * Draw string at widget offset.
	 *
	 * @param gc the GC
	 * @param offset the widget offset
	 * @param s the string to be drawn
	 * @param fg the foreground color
	 */
	private void draw(GC gc, int offset, String s, Color fg) {
		// Compute baseline delta (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=165640)
		int baseline= fTextWidget.getBaseline(offset);
		FontMetrics fontMetrics= gc.getFontMetrics();
		int fontBaseline= fontMetrics.getAscent() + fontMetrics.getLeading();
		int baslineDelta= baseline - fontBaseline;

		Point pos= fTextWidget.getLocationAtOffset(offset);
		gc.setForeground(fg);
		gc.drawString(s, pos.x, pos.y + baslineDelta, true);
	}

}
