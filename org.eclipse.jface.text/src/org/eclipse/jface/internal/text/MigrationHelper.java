/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;


/**
 * FIXME
 * 
 * This class is here to help transitioning to the
 * new StyledText APIs.
 * 
 * <p>
 * DO NOT USE OUTSIDE OF Platform Text.
 * This class will be deleted or renamed once the migration is complete.
 * </p>
 * 
 * @since 3.2
 */
public final class MigrationHelper {
	
	private MigrationHelper() {
		// Do not instantiate
	}
	
	private static final  boolean USE_OLD_API= Boolean.getBoolean("org.eclipse.jface.internal.text.useOldSTAPI"); //$NON-NLS-1$
	
	/*
	 * XXX: This needs to be disabled for mixed font support.
	 */
	private static final  boolean DISABLE_CHECKING= Boolean.getBoolean("org.eclipse.jface.internal.text.disableChecking"); //$NON-NLS-1$
	
	public static int checkLineHeightValue(int newAPIValue, StyledText textWidget, int lineCount) {
		int oldAPIValue= textWidget.getLineHeight() * lineCount;
		if (USE_OLD_API)
			return oldAPIValue;
		
		Assert.isTrue(oldAPIValue == newAPIValue);
		return newAPIValue;	
	}
	
	public static int checkValue(int newAPIValue, int oldAPIValue) {
		if (USE_OLD_API)
			return oldAPIValue;
		
		Assert.isTrue(DISABLE_CHECKING || oldAPIValue == newAPIValue);
		return newAPIValue;	
	}
	
	public static int getValue(int newAPIValue, int oldAPIValue) {
		if (USE_OLD_API)
			return oldAPIValue;
		
		return newAPIValue;	
	}
	
	public static int checkValue(int newAPIValue, int oldAPIValue, int tollerance) {
		if (USE_OLD_API)
			return oldAPIValue;
		
		Assert.isTrue(Math.abs(oldAPIValue - newAPIValue) <= tollerance);
		return newAPIValue;	
	}
	
	/**
	 * Computes the line height for the given line range.
	 * 
	 * @param textWidget the <code>StyledText</code> widget
	 * @param startLine the start line
	 * @param endLine the end line (exclusive)
	 * @param lineCount the line count used by the old API
	 * @return the height of all lines starting with <code>startLine</code> and ending above <code>endLime</code>
	 * @since 3.2
	 */
	public static int computeLineHeight(StyledText textWidget, int startLine, int endLine, int lineCount) {
		
		int height= 0;
		startLine= Math.max(startLine, 0);
		endLine= Math.min(endLine, textWidget.getLineCount());
		for (int i= startLine; i < endLine; i++) {
			int offset= textWidget.getOffsetAtLine(i);
			height= height + textWidget.getLineHeight(offset);
		}
		
		// FASTER:
		int fasterHeight= getLinePixel(textWidget, endLine) - getLinePixel(textWidget, startLine);
		height= checkValue(height, fasterHeight);
		
		return checkValue(height, lineCount * textWidget.getLineHeight());
	}

	public static int getPartialTopIndex(ITextViewer textViewer, StyledText textWidget) {
		
		if (textViewer instanceof ITextViewerExtension5) {
			int top= getPartialTopIndex(textWidget);
			
			int oldTop= textWidget.getTopIndex();
			if ((textWidget.getTopPixel() % textWidget.getLineHeight()) != 0)
				oldTop--;
			top= MigrationHelper.checkValue(top, oldTop);
			
			ITextViewerExtension5 extension= (ITextViewerExtension5)textViewer;
			return extension.widgetLine2ModelLine(top);
			
		}
		
		int top= textViewer.getTopIndex();
		if (!isTopIndexTop(textWidget))
			top--;
		
		int oldTop= textViewer.getTopIndex();
		if ((textWidget.getTopPixel() % textWidget.getLineHeight()) != 0)
			oldTop--;
		
		return MigrationHelper.checkValue(top, oldTop);
	}
	
	public static boolean isTopIndexTop(StyledText styledText) {
		// in the end use
		//return styledText.getLinePixel(topIndex) <= 0;
		return getPartialLineHidden(styledText) <= 0;
	}
	
	public static int getPartialLineHidden(StyledText textWidget) {
		int topIndex= textWidget.getTopIndex();
		
		int res1= getLinePixel(textWidget, topIndex);
		int res2= textWidget.getLocationAtOffset(textWidget.getOffsetAtLine(topIndex)).y;
		
		res1= checkValue(res1, res2);
		
		if (res1 != 0)
			res1= -getLinePixel(textWidget, topIndex - 1);

		return MigrationHelper.checkValue(res1, textWidget.getTopPixel() % textWidget.getLineHeight());
	}
	
	/**
	 * Returns the number of lines in the view port.
	 * 
	 * @param textWidget 
	 * @return the number of lines visible in the view port <code>-1</code> if there's no client area
	 */
	public static int getVisibleLinesInViewport(StyledText textWidget) {
		if (textWidget != null) {
			Rectangle clArea= textWidget.getClientArea();
			if (!clArea.isEmpty()) {
				
				Rectangle trim= textWidget.computeTrim(0, 0, 0, 0);
				int height= clArea.height - trim.height - 1;
				
				int first= getLineIndex(textWidget, 0);
				int last= getLineIndex(textWidget, height);
				
				return MigrationHelper.getValue(last - first, clArea.height / textWidget.getLineHeight());
			}
		}
		return -1;
	}

	/**
	 * Returns the the last, possibly partially, visible line in the view port.
	 *
	 * @param textViewer the text viewer
	 * @return the last, possibly partially, visible line in the view port
	 */
	public static int getBottomIndex(ITextViewer textViewer) {
		StyledText textWidget= textViewer.getTextWidget();

		if (textWidget.getClientArea().isEmpty())
			return -1;
		
		int oldBottom= textViewer.getBottomIndex();
		if (((textWidget.getTopPixel() + textWidget.getClientArea().height) % textWidget.getLineHeight()) != 0)
			oldBottom++;
		
		Rectangle trim= textWidget.computeTrim(0, 0, 0, 0);
		int height= textWidget.getClientArea().height - trim.height;
		int last= getLineIndex(textWidget, height);
		int offset= textWidget.getOffsetAtLine(last);
		int bottom= textViewer.getBottomIndex();
		
		int y= getLinePixel(textWidget, last);
		if (last == textWidget.getLineCount() && y > height || last < textWidget.getLineCount() && y + textWidget.getLineHeight(offset) > height)
			bottom++;
		
		return checkValue(bottom, oldBottom, 1);
	
	}

	/*
	 * @see StyledText#getLinePixel(int)
	 */
	public static int getLinePixel(StyledText textWidget, int line) {
		int y;
		try {
			 y= textWidget.getLocationAtOffset(textWidget.getOffsetAtLine(line)).y;
		} catch (IllegalArgumentException ex) {
			y= textWidget.getLocationAtOffset(textWidget.getCharCount()).y;
		}
		if (line == textWidget.getLineCount())
			y= y + textWidget.getLineHeight(textWidget.getOffsetAtLine(line - 1));
//		return checkValue(y, textWidget.getLinePixel(line));
		return y;
	}
	
	/*
	 * @see StyledText#getLineIndent(int)
	 */
	public static int getLineIndex(StyledText textWidget, int y) {
		int line;
		try {
			line= textWidget.getLineAtOffset(textWidget.getOffsetAtLocation(new Point(0, y)));
		} catch (IllegalArgumentException ex) {
			line= textWidget.getLineAtOffset(textWidget.getCharCount());
		}
//		return checkValue(line, textWidget.getLineIndex(y));
		return line;
	}

	/*
	 * @see StyledText#getPartialTopIndex()
	 */
	public static int getPartialTopIndex(StyledText textWidget) {
		int top= textWidget.getTopIndex();
		if (!isTopIndexTop(textWidget))
			top--;
		
//		return checkValue(top, textWidget.getPartialTopIndex());
		return top;
	}

	/**
	 * @param textWidget the text widget
	 * @return the line height
	 * @deprecated will be removed
	 */
	public static int getLineHeight(StyledText textWidget) {
		return textWidget.getLineHeight();
	}

}
