package org.eclipse.update.ui.forms;

/**
 * FormText is a windowless control that
 * draws text in the provided context.
 */

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import java.text.*;

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
	private Point computeTextSize(int wHint, int hHint) {
		Point extent;
		GC gc = new GC(this);
		
		gc.setFont(getFont());
		if ((getStyle() & SWT.WRAP)!=0 && wHint != SWT.DEFAULT) {
			extent = computeWrapSize(gc, wHint);
		}
		else {
			extent = gc.textExtent(getText());
		}
		gc.dispose();
		return extent;
	}
	
	private Point computeWrapSize(GC gc, int width) {
		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();

		int saved = 0;
		int last = 0;
		int height = lineHeight;
		System.out.println("Width hint: "+width);
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
		return new Point(width, height);
		
	}
	private void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = getSize();
		if (backgroundImage!=null) {
	       gc.drawImage(backgroundImage, 0, 0);
		}
	   	gc.setFont(getFont());
	   	if ((getStyle() & SWT.WRAP)!=0) {
	  		BreakIterator wb = BreakIterator.getWordInstance();
			wb.setText(text);
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();

			int saved = 0;
			int last = 0;
			int y = textMarginHeight;
			int width = size.x - textMarginWidth*2;
			for (int loc = wb.first();
			     loc != BreakIterator.DONE;
			     loc = wb.next()) {
		   		String line = text.substring(saved, loc);
		   		Point extent = gc.textExtent(line);
		   		if (extent.x > width) {
		   			// overflow
		   			String prevLine = text.substring(saved, last);
		   			gc.drawString(prevLine, textMarginWidth, y, true);
		   			saved = last;
		   			y += lineHeight;
		   		}
		   		last = loc;
			}
			// paint last line
			String lastLine = text.substring(saved, last);
			gc.drawString(lastLine, textMarginWidth, y, true);
	   	}
	   	else {
		   gc.drawText(getText(), textMarginWidth, textMarginHeight, true);
	   	}
	}
}