/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.JFaceResources;
import java.util.Hashtable;
import org.eclipse.swt.SWT;
import java.text.BreakIterator;

/**
 * @version 	1.0
 * @author
 */
public class TextSegment extends ParagraphSegment implements ITextSegment {
	private Color color;
	private String fontId;
	private String text;
	protected boolean underline;
	
	public TextSegment(String text) {
		this.text = text;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Font getFont() {
		if (fontId==null)
			return JFaceResources.getDefaultFont();
		else
		  return JFaceResources.getFontRegistry().get(fontId);
	}
	
	public String getText() {
		return text;
	}
	
	void setText(String text) {
		this.text= text;
	}
	
	void setColor(Color color) {
		this.color = color;
	}
	
	void setFontId(String fontId) {
		this.fontId = fontId;
	}
	public void advanceLocator(GC gc, int wHint, Locator locator, Hashtable objectTable) {
		Font oldFont = null;
		if (fontId!=null) {
			oldFont = gc.getFont();
			gc.setFont(getFont());
		}
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		
		if (wHint==SWT.DEFAULT) {
			Point extent = gc.textExtent(text);
			locator.x += extent.x;
			locator.width = extent.x;
			locator.height = lineHeight;
			return;
		}
		
		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		
		int saved = 0;
		int last = 0;
		
		int width = 0;

		for (int loc = wb.first();
			     loc != BreakIterator.DONE;
			     loc = wb.next()) {
		   String word = text.substring(saved, loc);
		   Point extent = gc.textExtent(word);
		   width = Math.max(width, extent.x);

		   if (locator.x +extent.x> wHint) {
		   		// overflow
		   		locator.x = 0;
		   		saved = last;
		   		locator.y += extent.y;
		   }
		   last = loc;
		}
		locator.width = width;
		locator.height = lineHeight;
		locator.rowHeight = Math.max(locator.rowHeight, lineHeight);
		if (oldFont!=null) {
			gc.setFont(oldFont);
		}
	}

	public void paint(GC gc, int width, Locator locator, Hashtable objectTable, boolean selected) {
		Font oldFont = null;
		Color oldColor = null;
		
		if (fontId!=null) {
			oldFont = gc.getFont();
			gc.setFont(getFont());
		}
		if (color!=null) {
			oldColor = gc.getForeground();
			gc.setForeground(color);
		}
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();
		
		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		
		int saved = 0;
		int last = 0;
		
		for (int loc = wb.first();
			     loc != BreakIterator.DONE;
			     loc = wb.next()) {
		   String word = text.substring(saved, loc);
		   Point extent = gc.textExtent(word);

		   if (locator.x + extent.x > width) {
		   		// overflow
		   		int x = 0;
		   		int y = locator.y;
   				String prevLine = text.substring(saved, last);
	   			gc.drawString(prevLine, x, y, true);
	   			
	   			Point prevExtent = null;
	   			
	   			if (underline || selected) 
	   			   prevExtent = gc.textExtent(prevLine);
	   			if (underline) {
   					int lineY = y + lineHeight - descent + 1;
	   				gc.drawLine(x, lineY, prevExtent.x, lineY);
	   			}
	   			if (selected) {
	   				int fheight = lineHeight - descent + 3;
	   				int fwidth = prevExtent.x+2;
	   				if (color!=null) 
	   				   	gc.setForeground(oldColor);
	   				gc.drawFocus(x-1, y, fwidth, fheight);
	   				if (color!=null)
   						gc.setForeground(color);
	   			}
		   		locator.x = 0;
		   		saved = last;
		   		locator.y += extent.y;
		   }
		   last = loc;
		}
		// paint the last line
		String lastLine = text.substring(saved, last);
		gc.drawString(lastLine, locator.x, locator.y, true);
		Point lastExtent = gc.textExtent(lastLine);
		if (underline) {
			int lineY = locator.y + lineHeight - descent + 1;
			gc.drawLine(locator.x, lineY, locator.x+lastExtent.x, lineY);
		}
		if (selected) {
			int fheight = lineHeight - descent + 3;
			int fwidth = lastExtent.x+2;
			if (color!=null) 
			   	gc.setForeground(oldColor);
			gc.drawFocus(locator.x-1, locator.y, fwidth, fheight);
		}
		locator.x += lastExtent.x;
		locator.rowHeight = Math.max(locator.rowHeight, lineHeight);
		if (oldFont!=null) {
			gc.setFont(oldFont);
		}
		if (oldColor!=null) {
			gc.setForeground(oldColor);
		}
	}
}