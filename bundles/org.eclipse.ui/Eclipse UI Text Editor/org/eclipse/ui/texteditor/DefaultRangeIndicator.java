/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;

 
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


/**
 * Specialized annotation to indicate a particular range of text lines.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * This class is instantiated automatically by <code>AbstractTextEditor</code>.
 * </p>
 */
public class DefaultRangeIndicator extends Annotation {

	private static PaletteData fgPaletteData;
	private Image fImage;
	
	/**
	 * Creates a new range indicator.
	 */
	public DefaultRangeIndicator() {
		super();
		setLayer(0);
	}
	
	/*
	 * @see Annotation#paint
	 */
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {

		Point canvasSize= canvas.getSize();

		int x= 0;		
		int y= bounds.y;
		int w= canvasSize.x;
		int h= bounds.height;
		int b= 1;

		if (y + h > canvasSize.y)
			h= canvasSize.y - y;
		
		if (y < 0) {
			h= h + y;
			y= 0;				
		}
		
		if (h <= 0)
			return;

		Image image = getImage(canvas);
		gc.drawImage(image, 0, 0, w, h, x, y, w, h);
		
		gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		gc.fillRectangle(x, bounds.y, w, b);
		gc.fillRectangle(x, bounds.y + bounds.height - b, w, b);
	}

	private Image getImage(Control control) {
		if (fImage == null) {
				fImage= createImage(control.getDisplay(), control.getSize());

				control.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (fImage != null && !fImage.isDisposed()) {
							fImage.dispose();
							fImage= null;
						}
					}
				});
		} else {
			Rectangle imageRectangle= fImage.getBounds();
			Point controlSize= control.getSize();
			
			if (imageRectangle.width < controlSize.x || imageRectangle.height < controlSize.y) {
				fImage.dispose();
				fImage= createImage(control.getDisplay(), controlSize);
			}
		}
		
		return fImage;
	}
	
	private static Image createImage(Display display, Point size) {

		int width= size.x;
		int height= size.y;

		if (fgPaletteData == null)
			fgPaletteData= createPalette(display);
		
		ImageData imageData= new ImageData(width, height, 1, fgPaletteData);

		for (int y= 0; y < height; y++)
			for (int x= 0; x < width; x++)
				imageData.setPixel(x, y, (x + y) % 2);

		return new Image(display, imageData);
	}

	private static PaletteData createPalette(Display display) {
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
		
		RGB rgbs[]= new RGB[] {
			new RGB(c1.getRed(), c1.getGreen(), c1.getBlue()),
			new RGB(c2.getRed(), c2.getGreen(), c2.getBlue())};

		return new PaletteData(rgbs);
	}
}