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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import org.eclipse.ui.internal.misc.Assert;


/**
 * ImageAnimationProcessor is the class that processes animation when images
 * are being shown.
 */
class ImageAnimationProcessor implements IAnimationProcessor {

	private static final String RUNNING_ICON = "running.gif"; //$NON-NLS-1$
	private static final String BACKGROUND_ICON = "back.gif"; //$NON-NLS-1$

	private ImageData[] animatedData;
	private ImageData[] disabledData;
	private AnimationManager manager;
	private static String DISABLED_IMAGE_NAME = "ANIMATION_DISABLED_IMAGE"; //$NON-NLS-1$
	private static String ANIMATED_IMAGE_NAME = "ANIMATION_ANIMATED_IMAGE"; //$NON-NLS-1$
	private ImageLoader runLoader = new ImageLoader();
	Color background;

	Job animateJob;
	Job clearJob;

	List items = Collections.synchronizedList(new ArrayList());


	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param AnimationManager
	 *            The manager that this will be processing with.
	 *  
	 */
	ImageAnimationProcessor(AnimationManager animationManager) {

		manager = animationManager;
		URL iconsRoot = Platform.getPlugin(PlatformUI.PLUGIN_ID).find(
				new Path(ProgressManager.PROGRESS_FOLDER));
		ProgressManager progressManager = ProgressManager.getInstance();

		try {
			URL runningRoot = new URL(iconsRoot, RUNNING_ICON);
			URL backRoot = new URL(iconsRoot, BACKGROUND_ICON);
			animatedData = progressManager.getImageData(runningRoot, runLoader);
			if (animatedData != null)
				JFaceResources.getImageRegistry().put(ANIMATED_IMAGE_NAME,
						progressManager.getImage(animatedData[0]));
			disabledData = progressManager.getImageData(backRoot, runLoader);
			if (disabledData != null)
				JFaceResources.getImageRegistry().put(DISABLED_IMAGE_NAME,
						progressManager.getImage(disabledData[0]));
		} catch (MalformedURLException exception) {
			ProgressManagerUtil.logException(exception);
		}
	}

	/**
	 * Run the animation loop in the receiver.
	 * 
	 * @param monitor
	 *            The monitor to check for cancellation.
	 */
	private void animateLoop(IProgressMonitor monitor) {

		// Create an off-screen image to draw on, and a GC to draw with.
		// Both are disposed after the animation.
		if (items.size() == 0)
			return;
		if (!PlatformUI.isWorkbenchRunning())
			return;
		IconAnimationItem[] animationItems = getAnimationItems();
		Display display = PlatformUI.getWorkbench().getDisplay();
		ImageData[] imageDataArray = getImageData();
		ImageData imageData = imageDataArray[0];
		Image image = ProgressManager.getInstance().getImage(imageData);
		int imageDataIndex = 0;
		ImageLoader loader = getLoader();
		if (display.isDisposed()) {
			monitor.setCanceled(true);
			manager.setAnimated(false);
			return;
		}
		Image offScreenImage = new Image(display, loader.logicalScreenWidth,
				loader.logicalScreenHeight);
		GC offScreenImageGC = new GC(offScreenImage);
		try {
			// Fill the off-screen image with the background color of the
			// canvas.
			offScreenImageGC.setBackground(background);
			offScreenImageGC.fillRectangle(0, 0, loader.logicalScreenWidth,
					loader.logicalScreenHeight);
			// Draw the current image onto the off-screen image.
			offScreenImageGC.drawImage(image, 0, 0, imageData.width,
					imageData.height, imageData.x, imageData.y,
					imageData.width, imageData.height);
			if (loader.repeatCount > 0) {
				while (manager.isAnimated() && !monitor.isCanceled()) {
					if (display.isDisposed()) {
						monitor.setCanceled(true);
						continue;
					}
					if (imageData.disposalMethod == SWT.DM_FILL_BACKGROUND) {
						// Fill with the background color before drawing.
						Color bgColor = null;
						int backgroundPixel = loader.backgroundPixel;
						if (backgroundPixel != -1) {
							// Fill with the background color.
							RGB backgroundRGB = imageData.palette
									.getRGB(backgroundPixel);
							bgColor = new Color(null, backgroundRGB);
						}
						try {
							offScreenImageGC.setBackground(bgColor != null
									? bgColor
									: background);
							offScreenImageGC.fillRectangle(imageData.x,
									imageData.y, imageData.width,
									imageData.height);
						} finally {
							if (bgColor != null)
								bgColor.dispose();
						}
					} else if (imageData.disposalMethod == SWT.DM_FILL_PREVIOUS) {
						// Restore the previous image before drawing.
						offScreenImageGC.drawImage(image, 0, 0,
								imageData.width, imageData.height, imageData.x,
								imageData.y, imageData.width, imageData.height);
					}
					// Get the next image data.
					imageDataIndex = (imageDataIndex + 1)
							% imageDataArray.length;
					imageData = imageDataArray[imageDataIndex];
					image.dispose();
					image = new Image(display, imageData);
					// Draw the new image data.
					offScreenImageGC.drawImage(image, 0, 0, imageData.width,
							imageData.height, imageData.x, imageData.y,
							imageData.width, imageData.height);
					boolean refreshItems = false;
					for (int i = 0; i < animationItems.length; i++) {
						IconAnimationItem item = animationItems[i];
						if (item.imageCanvasGC.isDisposed()) {
							refreshItems = true;
							continue;
						} else {
							// Draw the off-screen image to the screen.
							item.imageCanvasGC.drawImage(offScreenImage, 0, 0);
						}
					}
					if (refreshItems)
						animationItems = getAnimationItems();
					// Sleep for the specified delay time before drawing again.
					try {
						Thread.sleep(visibleDelay(imageData.delayTime * 10));
					} catch (InterruptedException e) {
						//If it is interrupted end quietly
					}
				}
			}
		} finally {
			image.dispose();
			offScreenImage.dispose();
			offScreenImageGC.dispose();
		}


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#animationFinished()
	 */
	public void animationFinished() {
		AnimationItem[] animationItems = getAnimationItems();
		for (int i = 0; i < animationItems.length; i++) {
			AnimationItem item = animationItems[i];
			item.animationDone();
		}

	}

	/**
	 * Return the loader currently in use.
	 * 
	 * @return ImageLoader
	 */
	ImageLoader getLoader() {
		return runLoader;
	}

	/**
	 * Get the current ImageData for the receiver.
	 * 
	 * @return ImageData[]
	 */
	ImageData[] getImageData() {
		if (manager.isAnimated()) {
			return animatedData;
		} else
			return disabledData;
	}
	/**
	 * Get the current Image for the receiver.
	 * 
	 * @return Image
	 */
	Image getImage() {
		if (manager.isAnimated()) {
			return JFaceResources.getImageRegistry().get(ANIMATED_IMAGE_NAME);
		} else
			return JFaceResources.getImageRegistry().get(DISABLED_IMAGE_NAME);
	}

	/**
	 * Get the bounds of the image being displayed here.
	 * 
	 * @return Rectangle
	 */
	public Rectangle getImageBounds() {
		return JFaceResources.getImageRegistry().get(DISABLED_IMAGE_NAME)
				.getBounds();
	}

	/**
	 * Return the specified number of milliseconds. If the specified number of
	 * milliseconds is too small to see a visual change, then return a higher
	 * number.
	 * 
	 * @param ms
	 *            The suggested delay
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
	 * Get the animation items currently registered for the receiver.
	 * 
	 * @return IconAnimationItem
	 */
	private IconAnimationItem[] getAnimationItems() {
		IconAnimationItem[] animationItems = new IconAnimationItem[items.size()];
		items.toArray(animationItems);
		return animationItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#addItem(org.eclipse.ui.internal.progress.AnimationItem)
	 */
	public void addItem(final AnimationItem item) {

		Assert.isTrue(item instanceof IconAnimationItem);

		items.add(item);
		if (background == null)
			background = AnimationManager.getItemBackgroundColor(item
					.getControl());

		item.getControl().addDisposeListener(new DisposeListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				items.remove(item);
			}
		});


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#animationStarted()
	 */
	public void animationStarted() {

		AnimationItem[] animationItems = getAnimationItems();
		for (int i = 0; i < animationItems.length; i++) {
			animationItems[i].animationStart();
		}
		getAnimateJob().schedule();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#hasItems()
	 */
	public boolean hasItems() {
		return !(items.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#itemsInactiveRedraw()
	 */
	public void itemsInactiveRedraw() {
		IconAnimationItem[] animationItems = getAnimationItems();
		for (int i = 0; i < animationItems.length; i++) {
			animationItems[i].getControl().redraw();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		if (animatedData == null)
			return 0;
		else
			return animatedData[0].width;
	}

	/**
	 * Return the animation job.
	 * 
	 * @return Job
	 */

	private Job getAnimateJob() {
		if (animateJob == null) {
			animateJob = new Job(ProgressMessages
					.getString("AnimateJob.JobName")) {//$NON-NLS-1$
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public IStatus run(IProgressMonitor monitor) {
					try {
						animateLoop(monitor);
						return Status.OK_STATUS;
					} catch (SWTException exception) {
						return ProgressManagerUtil.exceptionStatus(exception);
					}
				}
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
				 */
				public boolean shouldSchedule() {
					return PlatformUI.isWorkbenchRunning();
				}
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
				 */
				public boolean shouldRun() {
					return PlatformUI.isWorkbenchRunning();
				}
			};
			animateJob.setSystem(true);
			animateJob.setPriority(Job.DECORATE);
			animateJob.addJobChangeListener(new JobChangeAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void done(IJobChangeEvent event) {
					//Only schedule the job if we are showing anything
					if (manager.isAnimated() && hasItems())
						animateJob.schedule();
					else {
						//Clear the image
						if (clearJob == null)
							createClearJob();
						clearJob.schedule();
					}
				}
			});
		}
		return animateJob;
	}

	/**
	 * Create the clear job if we haven't yet.
	 * 
	 * @return Job. The job to clear the items.
	 */
	void createClearJob() {
		clearJob = new UIJob(ProgressMessages
				.getString("AnimationItem.RedrawJob")) {//$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				itemsInactiveRedraw();
				return Status.OK_STATUS;
			}
		};
		clearJob.setSystem(true);
		clearJob.setPriority(Job.DECORATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IAnimationProcessor#isProcessorJob(org.eclipse.core.runtime.jobs.Job)
	 */
	public boolean isProcessorJob(Job job) {
		return job == clearJob || job == animateJob;
	}

}
