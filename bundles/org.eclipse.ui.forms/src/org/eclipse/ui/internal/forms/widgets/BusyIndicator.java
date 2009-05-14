/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Mucke - fix for Bug 156456 
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

public final class BusyIndicator extends Canvas {

	private static final int MARGIN = 0;	
	private static final int IMAGE_COUNT = 8;
	private static final int MILLISECONDS_OF_DELAY = 180; 
	private Image[] imageCache;
	private Image image;
	
	private Display dpy;
	private Runnable timer;
	private boolean busy;
	private int imageIndex;
	
	/**
	 * BusyWidget constructor comment.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param style
	 *            int
	 */
	public BusyIndicator(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		
		dpy = getDisplay();
		timer = new Runnable() {
			public void run () {
				if (isDisposed()) return;
				redraw();
				if (!BusyIndicator.this.busy) return;
				update();
				if (isDisposed()) return;
				imageIndex = (imageIndex + 1) % IMAGE_COUNT;
				dpy.timerExec(MILLISECONDS_OF_DELAY, this);
			}
		};

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				clearImages();
			}
		});
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
//		checkWidget();
		Point size = new Point(0, 0);
		if (image != null) {
			Rectangle ibounds = image.getBounds();
			size.x = ibounds.width;
			size.y = ibounds.height;
		}
		if (isBusy()) {
			Rectangle bounds = getImage(0).getBounds();
			size.x = Math.max(size.x, bounds.width);
			size.y = Math.max(size.y, bounds.height);
		}
		size.x += MARGIN + MARGIN;
		size.y += MARGIN + MARGIN;
		return size;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#forceFocus()
	 */
	public boolean forceFocus() {
		return false;
	}

	/**
	 * Return the image or <code>null</code>.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Returns true if it is currently busy.
	 * 
	 * @return boolean
	 */
	public boolean isBusy() {
		return busy;
	}

	/*
	 * Process the paint event
	 */
	void onPaint(PaintEvent event) {
		Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0)
			return;

		Image activeImage;
		if (isBusy()) {
			activeImage = getImage(imageIndex);
		} else {
			clearImages();
			activeImage = image;
		}

		if (activeImage != null) {
			GC gc = event.gc;
			Rectangle ibounds = activeImage.getBounds();
			gc.drawImage(activeImage, rect.width / 2 - ibounds.width / 2,
					rect.height / 2 - ibounds.height / 2);
		}
	}

	/**
	 * Sets the indicators busy count up (true) or down (false) one.
	 * 
	 * @param busy
	 *            boolean
	 */
	public synchronized void setBusy(boolean busy) {
		if (this.busy == busy) return;
		this.busy = busy;
		imageIndex = 0;
		dpy.asyncExec(timer);
	}

	/**
	 * Set the image. The value <code>null</code> clears it.
	 */
	public void setImage(Image image) {
		if (image != this.image && !isDisposed()) {
			this.image = image;
			redraw();
		}
	}
	
	private ImageDescriptor createImageDescriptor(String relativePath) {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.forms"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, new Path(relativePath),null);
		if (url == null) return null;
		try {
			url = FileLocator.resolve(url);
			return ImageDescriptor.createFromURL(url);
		} catch (IOException e) {
			return null;
		}
	}

	private Image getImage(int index) {
		if (imageCache == null) {
			imageCache = new Image[IMAGE_COUNT];
		}
		if (imageCache[index] == null){
			ImageDescriptor descriptor = createImageDescriptor("$nl$/icons/progress/ani/" + (index + 1) + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
		    imageCache[index] = descriptor.createImage();
		}
		return imageCache[index];
	}
	
	private void clearImages() {
		if (imageCache != null) {
			for (int index = 0; index < IMAGE_COUNT; index++) {
				if (imageCache[index] != null && !imageCache[index].isDisposed()) {
					imageCache[index].dispose();
					imageCache[index] = null;
				}
			}
		}
	}

}
