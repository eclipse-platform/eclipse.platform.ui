/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
/**
 * An animated image to show busy status of the Web browser.
 */
public class BusyIndicator extends Canvas {
	protected Image[] images;
	protected Image image;

	protected Thread busyThread;
	protected boolean stop;

	/**
	 * BusyWidget constructor comment.
	 * @param parent org.eclipse.swt.widgets.Composite
	 * @param style int
	 */
	public BusyIndicator(Composite parent, int style) {
		super(parent, style);
	
		images = ImageResource.getBusyImages();
	
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
	
		image = images[0];
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(25, 25);
	}
	
	/**
	 * Creates a thread to animate the image.
	 */
	protected synchronized void createBusyThread() {
		if (busyThread != null)
			return;
	
		stop = false;
		busyThread = new Thread() {
			protected int count;
			public void run() {
				try {
					count = 1;
					while (!stop) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (!stop) {
									if (count < 13)
										setImage(images[count]);
									count++;
									if (count > 12)
										count = 1;
								}
							}
						});
						try {
							sleep(125);
						} catch (Exception e) {
							// ignore
						}
					}
					if (busyThread == null)
						Display.getDefault().syncExec(new Thread() {
							public void run() {
								setImage(images[0]);
							}
						});
				} catch (Exception e) {
					Trace.trace(Trace.WARNING, "Busy error", e); //$NON-NLS-1$
				}
			}
		};
	
		busyThread.setPriority(Thread.NORM_PRIORITY + 2);
		busyThread.setDaemon(true);
		busyThread.start();
	}
	
	public void dispose() {
		stop = true;
		busyThread = null;
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
		Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0)
			return;
	
		GC gc = event.gc;
		if (image != null)
			gc.drawImage(image, 2, 2);
	}

	/**
	 * Sets the indicators busy count up (true) or down (false) one.
	 *
	 * @param busy boolean
	 */
	public synchronized void setBusy(boolean busy) {
		if (busy) {
			if (busyThread == null)
				createBusyThread();
		} else {
			if (busyThread != null) {
				stop = true;
				busyThread = null;
			}
		}
	}

	/**
	 * Set the image.
	 * The value <code>null</code> clears it.
	 */
	public void setImage(Image image) {
		if (image != this.image && !isDisposed()) {
			this.image = image;
			redraw();
		}
	}
}