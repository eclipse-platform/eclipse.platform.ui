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

package org.eclipse.ui.internal.presentations.defaultpresentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.statushandling.StatusManager;

/**
 * @since 3.3
 * 
 */
public class AnimatedTabItem extends CTabItem {

	private boolean showingBusy = false;

	private Image stoppedImage;

	private Image[] images;

	private ImageLoader loader;

	private TimerTask animateTask;

	/**
	 * @param parent
	 * @param style
	 * @param index
	 */
	public AnimatedTabItem(CTabFolder parent, int style, int index) {
		super(parent, style, index);
	}

	/**
	 * Create a new instance of the receiver with the specified parent and
	 * style.
	 * 
	 * @param parent
	 * @param style
	 */
	public AnimatedTabItem(CTabFolder parent, int style) {
		super(parent, style);
	}

	/**
	 * Stop showing the busy cursor.
	 */
	public void stopBusy() {

		if (!showingBusy)
			return;

		showingBusy = false;
		if(animateTask != null){
			animateTask.cancel();
			animateTask = null;
		}
	}

	/**
	 * Start the busy indication.
	 */
	public void startBusy() {

		if (showingBusy)
			return;

		showingBusy = true;

		try {
			if (images == null) {
				loadImages();// If we fail to load do not continue
				if (images == null) {
					showingBusy = false;
					return;
				}
			}

			final Color background = getParent().getBackground();
			final Display display = getParent().getDisplay();

			if (images.length > 1) {

				Timer animateTimer = new Timer();
				if (animateTask == null)
					animateTimer.schedule(getTimerTask(background, display), 0);
			}
		} catch (SWTException ex) {
			IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, ex); 
	    	StatusManager.getManager().handle(status);
		} catch (IOException e1) {
			IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, e1); 
	    	StatusManager.getManager().handle(status);
		}
	}

	/**
	 * Get the timer task for the receiver.
	 * 
	 * @param background
	 * @param display
	 * @return TimerTask
	 */
	private TimerTask getTimerTask(final Color background, final Display display) {

		animateTask = new TimerTask() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.TimerTask#run()
			 */
			public void run() {
				/*
				 * Create an off-screen image to draw on, and fill it with the
				 * shell background.
				 */
				Image offScreenImage = new Image(getDisplay(),
						loader.logicalScreenWidth, loader.logicalScreenHeight);
				GC offScreenImageGC = new GC(offScreenImage);
				offScreenImageGC.setBackground(background);
				offScreenImageGC.fillRectangle(0, 0, loader.logicalScreenWidth,
						loader.logicalScreenHeight);

				try {
					/*
					 * Create the first image and draw it on the off-screen
					 * image.
					 */
					int imageDataIndex = 0;

					Image image = images[imageDataIndex];
					ImageData imageData = image.getImageData();
					

					offScreenImageGC.drawImage(image, 0, 0, imageData.width,
							imageData.height, imageData.x, imageData.y,
							imageData.width, imageData.height);

					/*
					 * Now loop through the images, creating and drawing each
					 * one on the off-screen image before drawing it on the
					 * shell.
					 */
					int repeatCount = loader.repeatCount;
					while (showingBusy && loader.repeatCount == 0
							|| repeatCount > 0) {
						switch (imageData.disposalMethod) {
						case SWT.DM_FILL_BACKGROUND:
							/*
							 * Fill with the background color before drawing.
							 */
							Color bgColor = null;

							offScreenImageGC
									.setBackground(bgColor != null ? bgColor
											: background);
							offScreenImageGC.fillRectangle(imageData.x,
									imageData.y, imageData.width,
									imageData.height);
							if (bgColor != null)
								bgColor.dispose();
							break;
						case SWT.DM_FILL_PREVIOUS:
							/*
							 * Restore the previous image before drawing.
							 */
							offScreenImageGC.drawImage(image, 0, 0,
									imageData.width, imageData.height,
									imageData.x, imageData.y, imageData.width,
									imageData.height);
							break;
						}

						imageDataIndex = (imageDataIndex + 1) % images.length;
						image = images[imageDataIndex];
						imageData = image.getImageData();

						offScreenImageGC.drawImage(image, 0, 0,
								imageData.width, imageData.height, imageData.x,
								imageData.y, imageData.width, imageData.height);

						final Image finalImage = image;
						
						display.syncExec(new Runnable() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see java.lang.Runnable#run()
							 */
							public void run() {
								AnimatedTabItem.this
										.setProgressImage(finalImage);

							}
						});

						/*
						 * Sleep for the specified delay time (adding
						 * commonly-used slow-down fudge factors).
						 */
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
						}

						if(images == null)
							return;
						
						/*
						 * If we have just drawn the last image, decrement the
						 * repeat count and start again.
						 */
						if (imageDataIndex == images.length - 1)
							repeatCount--;
					}
				} catch (SWTException ex) {
					IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, ex); 
			    	StatusManager.getManager().handle(status);
				} finally {
					display.syncExec(new Runnable() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							if (isDisposed() || stoppedImage == null
									|| stoppedImage.isDisposed())
								return;
							setImage(stoppedImage);

						}
					});
					if (offScreenImage != null && !offScreenImage.isDisposed())
						offScreenImage.dispose();
					if (offScreenImageGC != null
							&& !offScreenImageGC.isDisposed())
						offScreenImageGC.dispose();
				}
			}
		};

		return animateTask;
	}

	/**
	 * Load the images from the loader.
	 * 
	 * @throws IOException
	 */
	private void loadImages() throws IOException {
		InputStream urlStream;
		loader = new ImageLoader();

		urlStream = ProgressManagerUtil.getProgressSpinnerLocation()
				.openStream();

		final ImageData[] imageDataArray = loader.load(urlStream);
		images = new Image[imageDataArray.length];
		for (int i = 0; i < imageDataArray.length; i++) {
			images[i] = new Image(getDisplay(), imageDataArray[i]);
		}

		urlStream.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.CTabItem#setImage(org.eclipse.swt.graphics.Image)
	 */
	public void setImage(Image image) {
		super.setImage(image);
		stoppedImage = image;
	}

	/**
	 * Set the image during progress without caching.
	 * 
	 * @param image
	 */
	void setProgressImage(Image image) {
		if (isDisposed())
			return;
		super.setImage(image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.CTabItem#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (animateTask != null)
			animateTask.cancel();
		if (images != null) {
			for (int i = 0; i < images.length; i++) {
				images[i].dispose();
			}
		}
		images = null;
	}
}
