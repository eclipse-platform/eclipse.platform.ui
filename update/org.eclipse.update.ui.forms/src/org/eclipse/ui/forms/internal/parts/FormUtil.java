/*
 * Created on Nov 26, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.internal.parts;

import java.text.BreakIterator;

import org.eclipse.swt.graphics.*;

/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class FormUtil {
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
	public static void paintWrapText(
		GC gc,
		Point size,
		String text,
		int marginWidth,
		int marginHeight) {
		paintWrapText(gc, size, text, marginWidth, marginHeight, false);
	}
	public static void paintWrapText(
		GC gc,
		Point size,
		String text,
		int marginWidth,
		int marginHeight,
		boolean underline) {
		BreakIterator wb = BreakIterator.getWordInstance();
		wb.setText(text);
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();

		int saved = 0;
		int last = 0;
		int y = marginHeight;
		int width = size.x - marginWidth * 2;

		for (int loc = wb.first();
			loc != BreakIterator.DONE;
			loc = wb.next()) {
			String line = text.substring(saved, loc);
			Point extent = gc.textExtent(line);
			if (extent.x > width) {
				// overflow
				String prevLine = text.substring(saved, last);
				gc.drawString(prevLine, marginWidth, y, true);
				if (underline) {
					Point prevExtent = gc.textExtent(prevLine);
					int lineY = y + lineHeight - descent + 1;
					gc.drawLine(marginWidth, lineY, prevExtent.x, lineY);
				}

				saved = last;
				y += lineHeight;
			}
			last = loc;
		}
		// paint the last line
		String lastLine = text.substring(saved, last);
		gc.drawString(lastLine, marginWidth, y, true);
		if (underline) {
			int lineY = y + lineHeight - descent + 1;
			Point lastExtent = gc.textExtent(lastLine);
			gc.drawLine(marginWidth, lineY, marginWidth + lastExtent.x, lineY);
		}
	}

}
