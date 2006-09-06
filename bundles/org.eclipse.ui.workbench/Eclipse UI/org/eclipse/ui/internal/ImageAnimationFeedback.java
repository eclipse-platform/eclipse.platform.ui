/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 *
 */
public class ImageAnimationFeedback extends DefaultAnimationFeedback {
	private class ImageCanvas extends Canvas {
		private Image image;
		/**
		 * @param parent
		 * @param style
		 */
		public ImageCanvas(Composite parent, int style, Image image) {
			super(parent, style);
			this.image = image;
			
			addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					paintImage(e.gc);
				}
			});
		}
		/**
		 * @param gc
		 */
		protected void paintImage(GC gc) {
			gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, getBounds().width, getBounds().height);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Widget#dispose()
		 */
		public void dispose() {
			super.dispose();
			image.dispose();
		}
	}
	
	private Display display;
	private Shell theShell;
	
	private List startRects = new ArrayList();
	private List endRects = new ArrayList();
	private List controls = new ArrayList();
	
	private Image backingStore;
	
	/**
	 * Creates an animation effect where the interpolated rectangles are displayed using Canvas
	 * controls that show an image of the bits that were originally occupied by the various
	 * 'start' rectangles. 
	 */
	public ImageAnimationFeedback() {}

    /**
	 * @param parentShell
	 */
	public void initialize(Shell parentShell, Rectangle startRect, Rectangle endRect) {
		display = parentShell.getDisplay();

		Rectangle psRect = parentShell.getBounds();
		theShell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP);
		theShell.setBounds(parentShell.getBounds());

		addStartRect(startRect);
		addEndRect(endRect);
		
		// Capture the background image		
		backingStore = new Image(theShell.getDisplay(), psRect);
		GC gc = new GC(display);
    	gc.copyArea(backingStore, psRect.x, psRect.y);
		gc.dispose();
		
		theShell.setBackgroundImage(backingStore);
		theShell.setVisible(true);
		display.update();
	}
	
	public void addStartRect(Rectangle rect) {
		if (rect != null) {
			Rectangle start = Geometry.toControl(theShell, rect);
			startRects.add(start);
			
			Image image = new Image(display, rect.width, rect.height);
			GC gc = new GC(display);
			gc.copyArea(image, rect.x, rect.y);
			gc.dispose();
			
			ImageCanvas canvas = new ImageCanvas(theShell, SWT.BORDER | SWT.NO_BACKGROUND, image); 
			controls.add(canvas);
		}
	}
	
	public void addEndRect(Rectangle rect) {
		if (rect != null) {
			Rectangle end = Geometry.toControl(theShell, rect);
			endRects.add(end);
		}
	}
	
	public void renderStep(double amount) {
		// Move the controls to the new interpolation position
		Iterator startIter = startRects.iterator();
		Iterator endIter = endRects.iterator();
		Iterator ctrlIter = controls.iterator();
		while (startIter.hasNext()) {
			Rectangle start = (Rectangle) startIter.next();
			Rectangle end = (Rectangle) endIter.next();
			ImageCanvas canvas = (ImageCanvas) ctrlIter.next();

			// Get the bounds of the interpolated rect
			Rectangle curRect = RectangleAnimation.interpolate(start, end, amount);
			canvas.setBounds(curRect);
		}
        
        display.update();
	}

	public void jobInit() {
	}

	/**
	 * 
	 */
	public void dispose() {
		backingStore.dispose();
		for (Iterator ctrlIter = controls.iterator(); ctrlIter.hasNext();) {
			ImageCanvas canvas = (ImageCanvas) ctrlIter.next();
			canvas.dispose();
		}
		
		theShell.setVisible(false);
		theShell.dispose();
	}
}
