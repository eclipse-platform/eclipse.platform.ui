package org.eclipse.update.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import java.text.*;

/**
 * FormText is a windowless control that
 * draws text in the provided context.
 */
public class FormText extends Canvas {
	private Image backgroundImage;
	private String text="";
	private int textMarginWidth=5;
	private int textMarginHeight=5;

	public FormText(Composite parent, int style) {
		super(parent, style);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public Image getBackgroundImage() {
		return backgroundImage;
	}
	
	public void setBackgroundImage(Image image) {
		this.backgroundImage = image;
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int innerWidth = wHint;
		if (innerWidth!=SWT.DEFAULT)
		   innerWidth -= textMarginWidth*2;
		Point textSize = computeTextSize(innerWidth, hHint);
		int textWidth = textSize.x + 2*textMarginWidth;
		int textHeight = textSize.y + 2*textMarginHeight;
		return new Point(textWidth, textHeight);
	}
	
	public static int computeWrapHeight(GC gc, String text, int width) {
		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();

		int saved = 0;
		int last = 0;
		int height = lineHeight;

		for (int loc = wb.first();
			     loc != BreakIterator.DONE;
			     loc = wb.next()) {
		   String word = text.substring(saved, loc);
		   Point extent = gc.textExtent(word);
		   if (extent.x > width) {
		   		// overflow
		   		saved = last;
		   		height += extent.y;
		   }
		   last = loc;
		}
		return height;
	}
	
	private Point computeTextSize(int wHint, int hHint) {
		Point extent;
		GC gc = new GC(this);
		
		gc.setFont(getFont());
		if ((getStyle() & SWT.WRAP)!=0 && wHint != SWT.DEFAULT) {
			int height = computeWrapHeight(gc, text, wHint);
			extent = new Point(wHint, height);
		}
		else {
			extent = gc.textExtent(getText());
		}
		gc.dispose();
		return extent;
	}
	
	public static void paintWrapText(GC gc, Point size, String text, int marginWidth, int marginHeight) {
  		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();

		int saved = 0;
		int last = 0;
		int y = marginHeight;
		int width = size.x - marginWidth*2;
		for (int loc = wb.first();
		     loc != BreakIterator.DONE;
		     loc = wb.next()) {
	   		String line = text.substring(saved, loc);
	   		Point extent = gc.textExtent(line);
	   		if (extent.x > width) {
	   			// overflow
	   			String prevLine = text.substring(saved, last);
	   			gc.drawString(prevLine, marginWidth, y, true);
	   			saved = last;
	   			y += lineHeight;
	   		}
	   		last = loc;
		}
		// paint the last line
		String lastLine = text.substring(saved, last);
		gc.drawString(lastLine, marginWidth, y, true);
	}
	
	private void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = getSize();
		if (backgroundImage!=null) {
	       gc.drawImage(backgroundImage, 0, 0);
		}
	   	gc.setFont(getFont());
	   	if ((getStyle() & SWT.WRAP)!=0) {
	   		paintWrapText(gc, size, text, textMarginWidth, textMarginHeight);
	   	}
	   	else {
		   gc.drawText(getText(), textMarginWidth, textMarginHeight, true);
	   	}
	}
}