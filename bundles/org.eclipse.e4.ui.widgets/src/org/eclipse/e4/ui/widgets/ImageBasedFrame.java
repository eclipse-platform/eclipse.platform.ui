/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ImageBasedFrame extends Canvas {
	private Control framedControl;
	private boolean draggable = true;
	private boolean vertical = true;
	
	private int w1;
	private int w2;
	private int w3;
	private int h1;
	private int h2;
	private int h3;
	private Image imageCache;

	private Image handle;
	private int handleWidth;
	private int handleHeight;
	
	public ImageBasedFrame(Composite parent, Control toWrap, boolean hasHandle) {
		super(parent, SWT.NONE);
		
		this.framedControl = toWrap;
		setUpImages(parent.getDisplay(), "Some Image Path");
		
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				drawFrame(e);
			}
		});
		
		addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event event) {
				ImageBasedFrame frame = (ImageBasedFrame) event.widget;
				frame.setCursor(null);
			}
		});
		
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (!draggable)
					return;

				// Compute the display location for the handle
				Rectangle handleRect = new Rectangle(0,0,0,0);
				
				if (!vertical) {
					handleRect.x = w1;
					handleRect.y = h1;
					handleRect.width = handle.getBounds().width;
					handleRect.height = framedControl.getSize().y;
				} else {
					handleRect.x = w1;
					handleRect.y = h1;
					handleRect.width = framedControl.getSize().x;
					handleRect.height = handle.getBounds().height;
				}
				
				ImageBasedFrame frame = (ImageBasedFrame) e.widget;
				if (handleRect.contains(e.x, e.y)) {
					frame.setCursor(frame.getDisplay().getSystemCursor(
							SWT.CURSOR_SIZEALL));
				} else {
					frame.setCursor(null);
				}
			}
		});
		
		toWrap.setParent(this);
		if (vertical) {
			toWrap.setLocation(w1, h1 + handleHeight);
		} else {
			toWrap.setLocation(w1 + handleWidth, h1);
		}
		setSize(computeSize(-1, -1));

	}

	@Override
	public void dispose() {
		if (imageCache != null && !imageCache.isDisposed())
			imageCache.dispose();
		if (handle != null && !handle.isDisposed())
			handle.dispose();
		super.dispose();
	}
	
	private void setUpImages(Display display, String string) {
		// KLUDGE !! should come from CSS
		imageCache = new Image(display, 5,5);
		GC gc = new GC(imageCache);
		gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, 5, 5);
		gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		gc.fillRectangle(2, 0, 1, 2);
		gc.fillRectangle(2, 3, 1, 2);
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
		gc.fillRectangle(0, 2, 2, 1);
		gc.fillRectangle(3, 2, 2, 1);
		gc.dispose();
		
		handle = new Image(display, 2, 1);
		gc = new GC(handle);
		gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
		gc.fillRectangle(0, 0, 2, 1);
		gc.dispose();
		
		w1 = 2; w2 = 1; h1 = 2; h2 = 1; 
		w3 = imageCache.getBounds().width - (w1+w2);
		h3 = imageCache.getBounds().height - (h1+h2);
		
		if (vertical) {
			imageCache = rotateImage(display, imageCache);
			
			// Adjust the size markers for the rotation
			int tmp;
			tmp = w1; w1 = h1; h1 = tmp;
			tmp = w2; w2 = h2; h2 = tmp;
			tmp = w3; w3 = h3; h3 = tmp;
			
			// Rotate the handle (if any)
			if (handle != null)
				handle = rotateImage(display, handle);
		}
		
		// Compute the size of the handle in the 'offset' dimension
		handleWidth = (handle != null && !vertical) ? handle.getBounds().width : 0;
		handleHeight = (handle != null && vertical) ? handle.getBounds().height : 0;

	}

	private Image rotateImage(Display display, Image image) {
		// rotate 90 degrees
		Image rotatedImage = new Image(display, image.getBounds().height, image.getBounds().width);
		GC gc = new GC(rotatedImage);
		Transform t = new Transform(display);
		int w = image.getBounds().width;
		int offset = (w+1) % 2;
		t.translate(w - offset, 0);
		t.rotate(90);
		gc.setTransform(t);
		gc.drawImage(image, 0, 0);
		gc.dispose();
		t.dispose();

		// Get rid of the original
		image.dispose();
		
		// Return the new one
		return rotatedImage;
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		if (vertical) {
			int width = w1 + framedControl.getSize().x + w3;
			int height = h1 + handleHeight + framedControl.getSize().y + h3;
			return new Point(width, height);
		} else {
			int width = w1 + handleWidth + framedControl.getSize().x + w3;
			int height = h1 + framedControl.getSize().y + h3;
			return new Point(width, height);
		}
	}

	protected void drawFrame(PaintEvent e) {
		Point inner = framedControl.getSize();
		int handleWidth = (handle != null && !vertical) ? handle.getBounds().width : 0;
		int handleHeight = (handle != null && vertical) ? handle.getBounds().height : 0;
		
		Rectangle srcRect = new Rectangle(0,0,0,0);
		Rectangle dstRect = new Rectangle(0,0,0,0);
		
		// Top Left
		srcRect.x = 0; srcRect.y = 0; srcRect.width = w1; srcRect.height = h1;
		dstRect.x = 0; dstRect.y = 0; dstRect.width = w1; dstRect.height = h1;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Top Rail
		srcRect.x = w1; srcRect.y = 0; srcRect.width = w2; srcRect.height = h1;
		dstRect.x = w1; dstRect.y = 0; dstRect.width = inner.x + handleWidth; dstRect.height = h1;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// handle (if vertical)
		srcRect.x = 0; srcRect.y = 0; srcRect.width = handle.getBounds().width; srcRect.height = handle.getBounds().height;
		dstRect.x = w1; dstRect.y = h1; dstRect.width = inner.x + handleWidth; dstRect.height = handle.getBounds().height;
		e.gc.drawImage(handle, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Top Right
		srcRect.x = w1 + w2; srcRect.y = 0; srcRect.width = w3; srcRect.height = h1;
		dstRect.x = w1 + handleWidth + inner.x; dstRect.y = 0; dstRect.width = w3; dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Left Rail
		srcRect.x = 0; srcRect.y = h1; srcRect.width = w1; srcRect.height = h2;
		dstRect.x = 0; dstRect.y = h1; dstRect.width = w1; dstRect.height = inner.y + handleHeight;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Handle (if horizontal)
		if (handleWidth > 0) {
			srcRect.x = 0; srcRect.y = 0; srcRect.width = handle.getBounds().width; srcRect.height = handle.getBounds().height;
			dstRect.x = w1; dstRect.y = h1; dstRect.width = handleWidth; dstRect.height = inner.y;
			e.gc.drawImage(handle, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
					dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		}
		
		// Right Rail
		srcRect.x = w1 + w2; srcRect.y = h1; srcRect.width = w3; srcRect.height = h2;
		dstRect.x = w1 + handleWidth + inner.x; dstRect.y = h1; dstRect.width = w3; dstRect.height = inner.y + handleHeight;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Bottom Left
		srcRect.x = 0; srcRect.y = h1 + h2; srcRect.width = w1; srcRect.height = h3;
		dstRect.x = 0; dstRect.y = h1 + handleHeight + inner.y; dstRect.width = w1; dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Bottom Rail
		srcRect.x = w1; srcRect.y = h1 + h2; srcRect.width = w2; srcRect.height = h3;
		dstRect.x = w1; dstRect.y = h1 + handleHeight + inner.y; dstRect.width = handleWidth + inner.x; dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
		
		// Bottom right
		srcRect.x = w1 + w2; srcRect.y = h1 + h2; srcRect.width = w3; srcRect.height = h3;
		dstRect.x = w1 + handleWidth + inner.x; dstRect.y = h1 + handleHeight + inner.y; dstRect.width = w3; dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width, srcRect.height, 
				dstRect.x, dstRect.y, dstRect.width, dstRect.height);
	}
	
	public Image getImageCache() {
		return imageCache;
	}
	
	public Image getHandleImage() {
		return handle;
	}
}
