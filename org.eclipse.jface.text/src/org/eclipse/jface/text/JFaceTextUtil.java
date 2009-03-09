/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.internal.text.SelectionProcessor;

import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

/**
 * A collection of JFace Text functions.
 * <p>
 * This class is neither intended to be instantiated nor subclassed.
 * </p>
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class JFaceTextUtil {

	private JFaceTextUtil() {
		// Do not instantiate
	}

	/**
	 * Computes the line height for the given line range.
	 *
	 * @param textWidget the <code>StyledText</code> widget
	 * @param startLine the start line
	 * @param endLine the end line (exclusive)
	 * @param lineCount the line count used by the old API
	 * @return the height of all lines starting with <code>startLine</code> and ending above <code>endLime</code>
	 */
	public static int computeLineHeight(StyledText textWidget, int startLine, int endLine, int lineCount) {
		return getLinePixel(textWidget, endLine) - getLinePixel(textWidget, startLine);
	}

	/**
	 * Returns the last fully visible line of the widget. The exact semantics of "last fully visible
	 * line" are:
	 * <ul>
	 * <li>the last line of which the last pixel is visible, if any
	 * <li>otherwise, the only line that is partially visible
	 * </ul>
	 *
	 * @param widget the widget
	 * @return the last fully visible line
	 */
	public static int getBottomIndex(StyledText widget) {
		int lastPixel= computeLastVisiblePixel(widget);

		// bottom is in [0 .. lineCount - 1]
		int bottom= widget.getLineIndex(lastPixel);

		// bottom is the first line - no more checking
		if (bottom == 0)
			return bottom;

		int pixel= widget.getLinePixel(bottom);
		// bottom starts on or before the client area start - bottom is the only visible line
		if (pixel <= 0)
			return bottom;

		int offset= widget.getOffsetAtLine(bottom);
		int height= widget.getLineHeight(offset);

		// bottom is not showing entirely - use the previous line
		if (pixel + height - 1 > lastPixel)
			return bottom - 1;

		// bottom is fully visible and its last line is exactly the last pixel
		return bottom;
	}

	/**
	 * Returns the index of the first (possibly only partially) visible line of the widget
	 *
	 * @param widget the widget
	 * @return the index of the first line of which a pixel is visible
	 */
	public static int getPartialTopIndex(StyledText widget) {
		// see StyledText#getPartialTopIndex()
		int top= widget.getTopIndex();
		int pixels= widget.getLinePixel(top);

		// FIXME remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=123770 is fixed
		if (pixels == -widget.getLineHeight(widget.getOffsetAtLine(top))) {
			top++;
			pixels= 0;
		}

		if (pixels > 0)
			top--;

		return top;
	}

	/**
	 * Returns the index of the last (possibly only partially) visible line of the widget
	 *
	 * @param widget the text widget
	 * @return the index of the last line of which a pixel is visible
	 */
	public static int getPartialBottomIndex(StyledText widget) {
		// @see StyledText#getPartialBottomIndex()
		int lastPixel= computeLastVisiblePixel(widget);
		int bottom= widget.getLineIndex(lastPixel);
		return bottom;
	}

	/**
	 * Returns the last visible pixel in the widget's client area.
	 *
	 * @param widget the widget
	 * @return the last visible pixel in the widget's client area
	 */
	private static int computeLastVisiblePixel(StyledText widget) {
		int caHeight= widget.getClientArea().height;
		int lastPixel= caHeight - 1;
		// XXX: what if there is a margin? can't take trim as this includes the scrollbars which are not part of the client area
//		if ((textWidget.getStyle() & SWT.BORDER) != 0)
//			lastPixel -= 4;
		return lastPixel;
	}

	/**
	 * Returns the line index of the first visible model line in the viewer. The line may be only
	 * partially visible.
	 *
	 * @param viewer the text viewer
	 * @return the first line of which a pixel is visible, or -1 for no line
	 */
	public static int getPartialTopIndex(ITextViewer viewer) {
		StyledText widget= viewer.getTextWidget();
		int widgetTop= getPartialTopIndex(widget);
		return widgetLine2ModelLine(viewer, widgetTop);
	}

	/**
	 * Returns the last, possibly partially, visible line in the view port.
	 *
	 * @param viewer the text viewer
	 * @return the last, possibly partially, visible line in the view port
	 */
	public static int getPartialBottomIndex(ITextViewer viewer) {
		StyledText textWidget= viewer.getTextWidget();
		int widgetBottom= getPartialBottomIndex(textWidget);
		return widgetLine2ModelLine(viewer, widgetBottom);
	}

	/**
	 * Returns the range of lines that is visible in the viewer, including any partially visible
	 * lines.
	 *
	 * @param viewer the viewer
	 * @return the range of lines that is visible in the viewer, <code>null</code> if no lines are
	 *         visible
	 */
	public static ILineRange getVisibleModelLines(ITextViewer viewer) {
		int top= getPartialTopIndex(viewer);
		int bottom= getPartialBottomIndex(viewer);
		if (top == -1 || bottom == -1)
			return null;
		return new LineRange(top, bottom - top + 1);
	}

	/**
	 * Converts a widget line into a model (i.e. {@link IDocument}) line using the
	 * {@link ITextViewerExtension5} if available, otherwise by adapting the widget line to the
	 * viewer's {@link ITextViewer#getVisibleRegion() visible region}.
	 *
	 * @param viewer the viewer
	 * @param widgetLine the widget line to convert.
	 * @return the model line corresponding to <code>widgetLine</code> or -1 to signal that there
	 *         is no corresponding model line
	 */
	public static int widgetLine2ModelLine(ITextViewer viewer, int widgetLine) {
		int modelLine;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			modelLine= extension.widgetLine2ModelLine(widgetLine);
		} else {
			try {
				IRegion r= viewer.getVisibleRegion();
				IDocument d= viewer.getDocument();
				modelLine= widgetLine + d.getLineOfOffset(r.getOffset());
			} catch (BadLocationException x) {
				modelLine= widgetLine;
			}
		}
		return modelLine;
	}

	/**
	 * Converts a model (i.e. {@link IDocument}) line into a widget line using the
	 * {@link ITextViewerExtension5} if available, otherwise by adapting the model line to the
	 * viewer's {@link ITextViewer#getVisibleRegion() visible region}.
	 *
	 * @param viewer the viewer
	 * @param modelLine the model line to convert.
	 * @return the widget line corresponding to <code>modelLine</code> or -1 to signal that there
	 *         is no corresponding widget line
	 */
	public static int modelLineToWidgetLine(ITextViewer viewer, final int modelLine) {
		int widgetLine;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			widgetLine= extension.modelLine2WidgetLine(modelLine);
		} else {
			IRegion region= viewer.getVisibleRegion();
			IDocument document= viewer.getDocument();
			try {
				int visibleStartLine= document.getLineOfOffset(region.getOffset());
				int visibleEndLine= document.getLineOfOffset(region.getOffset() + region.getLength());
				if (modelLine < visibleStartLine || modelLine > visibleEndLine)
					widgetLine= -1;
				else
				widgetLine= modelLine - visibleStartLine;
			} catch (BadLocationException x) {
				// ignore and return -1
				widgetLine= -1;
			}
		}
		return widgetLine;
	}


	/**
	 * Returns the number of hidden pixels of the first partially visible line. If there is no
	 * partially visible line, zero is returned.
	 *
	 * @param textWidget the widget
	 * @return the number of hidden pixels of the first partial line, always &gt;= 0
	 */
	public static int getHiddenTopLinePixels(StyledText textWidget) {
		int top= getPartialTopIndex(textWidget);
		return -textWidget.getLinePixel(top);
	}

	/*
	 * @see StyledText#getLinePixel(int)
	 */
	public static int getLinePixel(StyledText textWidget, int line) {
		return textWidget.getLinePixel(line);
	}

	/*
	 * @see StyledText#getLineIndex(int)
	 */
	public static int getLineIndex(StyledText textWidget, int y) {
		int lineIndex= textWidget.getLineIndex(y);
		return lineIndex;
	}

	/**
	 * Returns <code>true</code> if the widget displays the entire contents, i.e. it cannot
	 * be vertically scrolled.
	 *
	 * @param widget the widget
	 * @return <code>true</code> if the widget displays the entire contents, i.e. it cannot
	 *         be vertically scrolled, <code>false</code> otherwise
	 */
	public static boolean isShowingEntireContents(StyledText widget) {
		if (widget.getTopPixel() != 0) // more efficient shortcut
			return false;

		int lastVisiblePixel= computeLastVisiblePixel(widget);
		int lastPossiblePixel= widget.getLinePixel(widget.getLineCount());
		return lastPossiblePixel <= lastVisiblePixel;
	}

	/**
	 * Determines the graphical area covered by the given text region in
	 * the given viewer.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @param textViewer the text viewer containing the region
	 * @return the graphical extend of the given region in the given viewer
	 *
	 * @since 3.4
	 */
	public static Rectangle computeArea(IRegion region, ITextViewer textViewer) {
		int start= 0;
		int end= 0;
		IRegion widgetRegion= modelRange2WidgetRange(region, textViewer);
		if (widgetRegion != null) {
			start= widgetRegion.getOffset();
			end= start + widgetRegion.getLength();
		}

		StyledText styledText= textViewer.getTextWidget();
		Rectangle bounds;
		if (end > 0 && start < end)
			bounds= styledText.getTextBounds(start, end - 1);
		else {
			Point loc= styledText.getLocationAtOffset(start);
			bounds= new Rectangle(loc.x, loc.y, getAverageCharWidth(textViewer.getTextWidget()), styledText.getLineHeight(start));
		}

		return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * Translates a given region of the text viewer's document into
	 * the corresponding region of the viewer's widget.
	 *
	 * @param region the document region
	 * @param textViewer the viewer containing the region
	 * @return the corresponding widget region
	 *
	 * @since 3.4
	 */
	private static IRegion modelRange2WidgetRange(IRegion region, ITextViewer textViewer) {
		if (textViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) textViewer;
			return extension.modelRange2WidgetRange(region);
		}

		IRegion visibleRegion= textViewer.getVisibleRegion();
		int start= region.getOffset() - visibleRegion.getOffset();
		int end= start + region.getLength();
		if (end > visibleRegion.getLength())
			end= visibleRegion.getLength();

		return new Region(start, end - start);
	}

	/**
	 * Returns the average character width of the given control's font.
	 *
	 * @param control the control to calculate the average char width for
	 * @return the average character width of the controls font
	 *
	 * @since 3.4
	 */
	public static int getAverageCharWidth(Control control) {
		GC gc= new GC(control);
		gc.setFont(control.getFont());
		int increment= gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		return increment;
	}

	/**
	 * Returns <code>true</code> if the text covered by <code>selection</code> does not contain any
	 * characters in the given viewer. Note the difference to {@link ITextSelection#isEmpty()},
	 * which returns <code>true</code> only for invalid selections.
	 * 
	 * @param viewer the viewer
	 * @param selection the selection
	 * @return <code>true</code> if <code>selection</code> does not contain any text,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException if accessing the document failed
	 * @since 3.5
	 */
	public static boolean isEmpty(ITextViewer viewer, ITextSelection selection) throws BadLocationException {
		return new SelectionProcessor(viewer).isEmpty(selection);
	}

	/**
	 * Returns the text regions covered by the given selection in the given viewer.
	 * 
	 * @param viewer the viewer
	 * @param selection the selection
	 * @return the text regions corresponding to <code>selection</code>
	 * @throws BadLocationException if accessing the document failed
	 * @since 3.5
	 */
	public static IRegion[] getCoveredRanges(ITextViewer viewer, ITextSelection selection) throws BadLocationException {
		return new SelectionProcessor(viewer).getRanges(selection);
	}

	/**
	 * Returns the offset in the given viewer that corresponds to the current cursor location.
	 * 
	 * @param viewer the viewer
	 * @return the offset for the current cursor location or -1 if not available
	 * @since 3.5
	 */
	public static int getOffsetForCursorLocation(ITextViewer viewer) {

		try {
			StyledText text= viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return -1;

			Display display= text.getDisplay();
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);

			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			Point p= text.getLocationAtOffset(widgetOffset);
			if (p.x > relativePosition.x)
				widgetOffset--;

			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5)viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			}

			return widgetOffset + viewer.getVisibleRegion().getOffset();

		} catch (IllegalArgumentException e) {
			return -1;
		}
	}
}
