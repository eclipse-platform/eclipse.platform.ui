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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

public final class BusyIndicator extends Canvas {

	class BusyThread extends Thread {
		Rectangle bounds;
		Display display;
		GC offScreenImageGC;
		Image offScreenImage;
		Image timage;
		boolean stop;
		
		private BusyThread(Rectangle bounds, Display display, GC offScreenImageGC, Image offScreenImage) {
			this.bounds = bounds;
			this.display = display;
			this.offScreenImageGC = offScreenImageGC;
			this.offScreenImage = offScreenImage;
		}
		
		public void run() {
			try {
				/*
				 * Create an off-screen image to draw on, and fill it with
				 * the shell background.
				 */
				FormUtil.setAntialias(offScreenImageGC, SWT.ON);
				display.syncExec(new Runnable() {
					public void run() {
						if (!isDisposed())
							drawBackground(offScreenImageGC, 0, 0,
									bounds.width,
									bounds.height);
					}
				});
				if (isDisposed())
					return;

				/*
				 * Create the first image and draw it on the off-screen
				 * image.
				 */
				int imageDataIndex = 0;
				ImageData imageData;
				synchronized (BusyIndicator.this) {
					timage = getImage(imageDataIndex);
					imageData = timage.getImageData();
					offScreenImageGC.drawImage(timage, 0, 0,
							imageData.width, imageData.height, imageData.x,
							imageData.y, imageData.width, imageData.height);
				}

				/*
				 * Now loop through the images, creating and drawing
				 * each one on the off-screen image before drawing it on
				 * the shell.
				 */
				while (!stop && !isDisposed() && timage != null) {

					/*
					 * Fill with the background color before
					 * drawing.
					 */
					final ImageData fimageData = imageData;
					display.syncExec(new Runnable() {
						public void run() {
							if (!isDisposed()) {
								drawBackground(offScreenImageGC, fimageData.x,
										fimageData.y, fimageData.width,
										fimageData.height);
							}
						}
					});

					synchronized (BusyIndicator.this) {
						imageDataIndex = (imageDataIndex + 1) % IMAGE_COUNT;
						timage = getImage(imageDataIndex);
						imageData = timage.getImageData();
						offScreenImageGC.drawImage(timage, 0, 0,
								imageData.width, imageData.height,
								imageData.x, imageData.y, imageData.width,
								imageData.height);
					}

					/* Draw the off-screen image to the shell. */
					animationImage = offScreenImage;
					display.syncExec(new Runnable() {
						public void run() {
							if (!isDisposed())
								redraw();
						}
					});
					/*
					 * Sleep for the specified delay time 
					 */
					try {
						Thread.sleep(MILLISECONDS_OF_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}


				}
			} catch (Exception e) {
			} finally {
				if (!display.isDisposed()) {
					display.syncExec(new Runnable() {
						public void run() {
							if (offScreenImage != null
									&& !offScreenImage.isDisposed())
								offScreenImage.dispose();
							if (offScreenImageGC != null
									&& !offScreenImageGC.isDisposed())
								offScreenImageGC.dispose();
						}
					});
				}
				clearImages();
			}
			if (busyThread == null && !display.isDisposed())
				display.syncExec(new Runnable() {
					public void run() {
						animationImage = null;
						if (!isDisposed())
							redraw();
					}
				});
		}
		
		public void setStop(boolean stop) {
			this.stop = stop;
		}
	}

	private static final int MARGIN = 0;	
	private static final int IMAGE_COUNT = 8;
	private static final int MILLISECONDS_OF_DELAY = 180; 
	private Image[] imageCache;
	protected Image image;

	protected Image animationImage;

	protected BusyThread busyThread;
	
	/**
	 * BusyWidget constructor comment.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param style
	 *            int
	 */
	public BusyIndicator(Composite parent, int style) {
		super(parent, style);

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
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
	 * Creates a thread to animate the image.
	 */
	protected synchronized void createBusyThread() {
		if (busyThread != null)
			return;

		Rectangle bounds = getImage(0).getBounds();
		Display display = getDisplay();
		Image offScreenImage = new Image(display, bounds.width, bounds.height);
		GC offScreenImageGC = new GC(offScreenImage);
		busyThread = new BusyThread(bounds, display, offScreenImageGC, offScreenImage);
		busyThread.setPriority(Thread.NORM_PRIORITY + 2);
		busyThread.setDaemon(true);
		busyThread.start();
	}

	public void dispose() {
		if (busyThread != null) {
			busyThread.setStop(true);
			busyThread = null;
		}
		super.dispose();
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
		return (busyThread != null);
	}

	/*
	 * Process the paint event
	 */
	protected void onPaint(PaintEvent event) {
		if (animationImage != null && animationImage.isDisposed()) {
			animationImage = null;
		}
		Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0)
			return;

		GC gc = event.gc;
		Image activeImage = animationImage != null ? animationImage : image;
		if (activeImage != null) {
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
		if (busy) {
			if (busyThread == null)
				createBusyThread();
		} else {
			if (busyThread != null) {
				busyThread.setStop(true);
				busyThread = null;
			}
		}
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

	private synchronized Image getImage(int index) {
		if (imageCache == null) {
			imageCache = new Image[IMAGE_COUNT];
		}
		if (imageCache[index] == null){
			ImageDescriptor descriptor = createImageDescriptor("$nl$/icons/progress/ani/" + (index + 1) + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
		    imageCache[index] = descriptor.createImage();
		}
		return imageCache[index];
	}
	
	private synchronized void clearImages() {
		if (busyThread != null  && !isDisposed())
			return;
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
