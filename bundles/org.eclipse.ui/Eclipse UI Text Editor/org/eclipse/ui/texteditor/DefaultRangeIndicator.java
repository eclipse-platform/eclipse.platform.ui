package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.source.Annotation;


/**
 * Specialized annotation to indicate a particular range of text lines.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * This class is instantiated automatically by <code>AbstractTextEditor</code>.
 * </p>
 */
public class DefaultRangeIndicator extends Annotation {

	static ImageData fgData;
	static RGB fgRGBs[]= new RGB[2];
	
	/**
	 * Creates a new range indicator.
	 */
	public DefaultRangeIndicator() {
		super();
		setLayer(0);
	}
	/**
	 * Creates the pattern that fills the source line range.
	 *
	 * @param width the width of the pattern
	 */
	void createPattern(int width) {
		fgData= new ImageData(width, 64, 1, new PaletteData(fgRGBs));
		
		for (int y= 0; y < fgData.height; y++)
			for (int x= 0; x < width; x++)
				if (x % 2 == y % 2)
					fgData.setPixel(x, y, 1);
	}
	/*
	 * @see Annotation#paint
	 */
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {
		
		Point d= canvas.getSize();
		Display display= canvas.getDisplay();
						
		int x= 0;
		int y= bounds.y+1;
		int w= d.x;
		int h= bounds.height;
		
		int b= 1;
		
		if (fgData == null || w > fgData.width)
			createPattern(w);
		
		Color c1;
		Color c2;
		if (false) {
			// range lighter
			c1= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			c2= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		} else {
			// range darker
			c1= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			c2= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		}
		
		fgRGBs[0]= new RGB(c1.getRed(), c1.getGreen(), c1.getBlue());
		fgRGBs[1]= new RGB(c2.getRed(), c2.getGreen(), c2.getBlue());
		Image im= new Image(display, fgData);
						
		int end= y+h;
		for (int yy= y; yy < end; yy+= fgData.height) {
			int hh= fgData.height;
			if (yy + hh > end)
				hh= end - yy;
			gc.drawImage(im, 0, 0, w, hh, x, yy, w, hh);
		}
		
		im.dispose();
			
		gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		gc.fillRectangle(x, y, w, b);
		gc.fillRectangle(x, end-b, w, b);
	}
}
