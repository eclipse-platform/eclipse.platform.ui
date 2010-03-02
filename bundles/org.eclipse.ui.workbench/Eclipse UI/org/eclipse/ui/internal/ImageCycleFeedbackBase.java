/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Base class for Cyclic animations.
 * 
 * @since 3.3
 * 
 */
public abstract class ImageCycleFeedbackBase extends AnimationFeedbackBase {
	protected Image[] images;
	protected Image stoppedImage;
	private Image offScreenImage;
	private GC offScreenImageGC;
	private int imageDataIndex;
	private Image image;
	private ImageData imageData;
	protected Display display;
	protected Color background;

	/**
	 * @param parentShell
	 */
	public ImageCycleFeedbackBase(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param parentShell
	 * @param images :
	 *            an array of images
	 */
	public ImageCycleFeedbackBase(Shell parentShell, Image[] images) {
		super(parentShell);
		this.images = images;
	}

	/**
	 * Set the image during progress without caching.
	 * 
	 * @param image
	 */
	public abstract void showImage(Image image);

	/**
	 * Save initial Image which would be stoppedImage
	 * 
	 */
	public abstract void saveStoppedImage();

	/**
	 * Set the stopped Image upon animation completion
	 * 
	 * @param image
	 */
	public abstract void setStoppedImage(Image image);

	public void dispose() {
		// TODO Auto-generated method stub
		if (stoppedImage == null || stoppedImage.isDisposed())
			return;
		setStoppedImage(stoppedImage);

		if (offScreenImageGC != null && !offScreenImageGC.isDisposed())
			offScreenImageGC.dispose();

		if (offScreenImage != null && !offScreenImage.isDisposed())
			offScreenImage.dispose();
	}

	public boolean jobInit(AnimationEngine engine) {
		return super.jobInit(engine);
	}

	public void renderStep(AnimationEngine engine) {
		// TODO Auto-generated method stub
		if (offScreenImage == null) {
			offScreenImage = getOffscreenImage();
		}

		try {
			imageDataIndex = (imageDataIndex + 1) % images.length;
			image = images[imageDataIndex];
			imageData = image.getImageData();

			offScreenImageGC.drawImage(image, 0, 0, imageData.width,
					imageData.height, imageData.x, imageData.y,
					imageData.width, imageData.height);

			final Image finalImage = image;

			display.syncExec(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					showImage(finalImage);

				}
			});

			/*
			 * Sleep for the specified delay time (adding commonly-used
			 * slow-down fudge factors).
			 */
			// try {
			// Thread.sleep(30);
			// } catch (InterruptedException e) {
			// }
			if (images == null)
				return;
		} catch (SWTException ex) {
			IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH,
					ex);
			StatusManager.getManager().handle(status);
		}
	}

	private Image getOffscreenImage() {
		saveStoppedImage();
		imageDataIndex = 0;
		image = images[imageDataIndex];
		imageData = image.getImageData();
		/*
		 * Create an off-screen image to draw on, and fill it with the shell
		 * background.
		 */
		offScreenImage = new Image(display, imageData.width, imageData.height);

		offScreenImageGC = new GC(offScreenImage);
		offScreenImageGC.setBackground(background);
		offScreenImageGC.fillRectangle(0, 0, imageData.width, imageData.height);

		/*
		 * Create the first image and draw it on the off-screen image.
		 */

		offScreenImageGC.drawImage(image, 0, 0, imageData.width,
				imageData.height, imageData.x, imageData.y, imageData.width,
				imageData.height);

		return offScreenImage;
	}

}
