/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.progress;

import java.io.*;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class AnimatedCanvas {

	private ImageData[] animatedData;
	private ImageData[] disabledData;
	private Image animatedImage;
	private Image disabledImage;
	private Canvas imageCanvas;
	private GC imageCanvasGC;
	private ImageLoader loader = new ImageLoader();
	private boolean animated = false;
	private Job animateJob;
	
	/**
	 * Create a new instance of the receiver.
	 * @param animatedPath
	 * @param disabledPath
	 */

	public AnimatedCanvas(URL animatedPath, URL disabledPath) {

		animatedData = getImageData(animatedPath);
		if (animatedData != null)
			animatedImage = getImage(animatedData[0]);

		disabledData = getImageData(disabledPath);
		if (disabledData != null)
			disabledImage = getImage(disabledData[0]);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 * @param source
	 * @return Image
	 */
	private Image getImage(ImageData source) {
		ImageData mask = source.getTransparencyMask();
		return new Image(null, source, mask);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 * @param fileSystemPath The URL for the file system to the image.
	 * @return ImageData[]
	 */
	private ImageData[] getImageData(URL fileSystemPath) {
		try {
			InputStream stream = fileSystemPath.openStream();
			ImageData[] result = loader.load(stream);
			stream.close();
			return result;
		} catch (FileNotFoundException exception) {
			return null;
		} catch (IOException exception) {
			return null;
		}
	}

	/**
	 * Create the canvas that will display the image.
	 * @param parent
	 */
	void createCanvas(Composite parent) {

		// Canvas to show the image.
		imageCanvas = new Canvas(parent, SWT.NONE);
		imageCanvas.setBackground(
			parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		imageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paintImage(event, getImage(), getImageData()[0]);
			}
		});

		imageCanvasGC = new GC(imageCanvas);
		imageCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageCanvasGC.dispose();
			}
		});

	}
	
	/**
	 * Return the current image for the receiver.
	 * @return Image
	 */
	Image getImage() {
		if (animated)
			return animatedImage;
		else
			return disabledImage;
	}

	/**
	 * Get the current ImageData for the receiver.
	 * @return ImageData[]
	 */
	private ImageData[] getImageData() {
		if (animated)
			return animatedData;
		else
			return disabledData;
	}

	/**
	 * Paint the image in the canvas.
	 * @param event The PaintEvent that generated this call.
	 * @param image The image to display
	 * @param imageData The array of ImageData. Required to show an animation.
	 */
	private void paintImage(PaintEvent event, Image image, ImageData imageData) {

		Image paintImage = image;

		int w = imageData.width;
		int h = imageData.height;
		event.gc.drawImage(
			paintImage,
			0,
			0,
			imageData.width,
			imageData.height,
			imageData.x,
			imageData.y,
			w,
			h);
	}
	/**
	 * Return whether or not the current state is animated.
	 * @return boolean
	 */
	private boolean isAnimated() {
		return animated;
	}

	/**
	 * Set whether or not the receiver is animated.
	 * @param boolean
	 */
	void setAnimated(final boolean bool) {
		if (getControl().isDisposed())
			return;
		getControl().getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if (imageCanvas.isDisposed())
					return;
				boolean started = animated;
				animated = bool;
				imageCanvas.redraw();
				if (!started && bool)
					animate();

			}
		});

	}

	/**
	 * Get the SWT control for the receiver.
	 * @return Control
	 */
	public Control getControl() {
		return imageCanvas;
	}

	/**
	 * Dispose the images in the receiver.
	 */
	void dispose() {
		animatedImage.dispose();
		disabledImage.dispose();
	}

	/**
	 * Animate the image in the canvas if required.
	 */
	private void animate() {
		Image image = getImage();
		ImageData[] imageDataArray = getImageData();
		if (isAnimated() && image != null && imageDataArray.length > 1) {
			//Clear out the old job
			if (animateJob != null)
				animateJob.cancel();
				
			animateJob = new AnimateJob() {
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public IStatus run(IProgressMonitor monitor) {
					try {
						animateLoop(monitor);
						return Status.OK_STATUS;
					} catch (final SWTException e) {
						return new Status(
							IStatus.ERROR,
							WorkbenchPlugin
								.getDefault()
								.getDescriptor()
								.getUniqueIdentifier(),
							IStatus.ERROR,
							ProgressMessages.getString("AnimatedCanvas.JobInvocationError"), //$NON-NLS-1$
							e);
					}
				}
			};
			
			animateJob.schedule();
		}
	}
	/**
	 * Loop through all of the images in a multi-image file
	 * and display them one after another.
	 * @param monitor The monitor supplied to the job
	 */
	private void animateLoop(IProgressMonitor monitor) {
		// Create an off-screen image to draw on, and a GC to draw with.
		// Both are disposed after the animation.

		Display display = imageCanvas.getDisplay();
		ImageData[] imageDataArray = getImageData();
		ImageData imageData = imageDataArray[0];
		Image image = getImage(imageData);
		int imageDataIndex = 0;
		final Color[] backgrounds = new Color[1];

		Image offScreenImage =
			new Image(
				display,
				loader.logicalScreenWidth,
				loader.logicalScreenHeight);
		GC offScreenImageGC = new GC(offScreenImage);

		try {
			// Use syncExec to get the background color of the imageCanvas.
			display.syncExec(new Runnable() {
				public void run() {
					if (imageCanvas.isDisposed())
						return;

					backgrounds[0] = imageCanvas.getBackground();
				}
			});

			// Fill the off-screen image with the background color of the canvas.
			offScreenImageGC.setBackground(backgrounds[0]);
			offScreenImageGC.fillRectangle(
				0,
				0,
				loader.logicalScreenWidth,
				loader.logicalScreenHeight);

			// Draw the current image onto the off-screen image.
			offScreenImageGC.drawImage(
				image,
				0,
				0,
				imageData.width,
				imageData.height,
				imageData.x,
				imageData.y,
				imageData.width,
				imageData.height);

			if (loader.repeatCount > 0) {
				while (isAnimated() && !monitor.isCanceled()) {

					if (getControl().isDisposed())
						return;
					if (imageData.disposalMethod == SWT.DM_FILL_BACKGROUND) {
						// Fill with the background color before drawing.
						Color bgColor = null;
						int backgroundPixel = loader.backgroundPixel;
						if (backgroundPixel != -1) {
							// Fill with the background color.
							RGB backgroundRGB =
								imageData.palette.getRGB(backgroundPixel);
							bgColor = new Color(null, backgroundRGB);
						}
						try {
							offScreenImageGC.setBackground(
								bgColor != null ? bgColor : backgrounds[0]);
							offScreenImageGC.fillRectangle(
								imageData.x,
								imageData.y,
								imageData.width,
								imageData.height);
						} finally {
							if (bgColor != null)
								bgColor.dispose();
						}
					} else if (
						imageData.disposalMethod == SWT.DM_FILL_PREVIOUS) {
						// Restore the previous image before drawing.
						offScreenImageGC.drawImage(
							image,
							0,
							0,
							imageData.width,
							imageData.height,
							imageData.x,
							imageData.y,
							imageData.width,
							imageData.height);
					}

					// Get the next image data.
					imageDataIndex =
						(imageDataIndex + 1) % imageDataArray.length;
					imageData = imageDataArray[imageDataIndex];
					image.dispose();
					image = new Image(display, imageData);

					// Draw the new image data.
					offScreenImageGC.drawImage(
						image,
						0,
						0,
						imageData.width,
						imageData.height,
						imageData.x,
						imageData.y,
						imageData.width,
						imageData.height);

					// Draw the off-screen image to the screen.
					imageCanvasGC.drawImage(offScreenImage, 0, 0);

					// Sleep for the specified delay time before drawing again.
					try {
						Thread.sleep(visibleDelay(imageData.delayTime * 10));
					} catch (InterruptedException e) {
					}

				}
			}
		} finally {
			offScreenImage.dispose();
			offScreenImageGC.dispose();
		}
	}

	/**
	 * Return the specified number of milliseconds.
	 * If the specified number of milliseconds is too small
	 * to see a visual change, then return a higher number.
	 * @param ms The suggested delay
	 * @return int
	 */
	int visibleDelay(int ms) {
		if (ms < 20)
			return ms + 30;
		if (ms < 30)
			return ms + 10;
		return ms;
	}

	/**
	 * Get the bounds of the image being displayed here.
	 * @return Rectangle
	 */
	public Rectangle getImageBounds() {
		return disabledImage.getBounds();
	}
}
