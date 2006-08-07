/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.rulers;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;


/**
 * Partial copy of JFaceTextUtil in jface.text.
 * 
 * @since 3.3
 */
final class JFaceTextUtil {
	
	private JFaceTextUtil() {
		// Do not instantiate
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
	 * Returns the last visible pixel in the widget's client area.
	 * 
	 * @param widget the widget
	 * @return the last visible pixel in the widget's client area
	 */
	private static int computeLastVisiblePixel(StyledText widget) {
		int caHeight= widget.getClientArea().height;
		int lastPixel= caHeight - 1;
		// XXX what if there is a margin? can't take trim as this includes the scrollbars which are not part of the client area
//		if ((textWidget.getStyle() & SWT.BORDER) != 0)
//			lastPixel -= 4;
		return lastPixel;
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

}
