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
import java.util.HashSet;
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
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The AnimationManager is the class that keeps track of the animation items
 * to update.
 */
class AnimationManager {

	private static final String RUNNING_ICON = "running.gif"; //$NON-NLS-1$
	private static final String BACKGROUND_ICON = "back.gif"; //$NON-NLS-1$

	private static AnimationManager singleton;

	private ImageData[] animatedData;
	private ImageData[] disabledData;

	private static String DISABLED_IMAGE_NAME = "ANIMATION_DISABLED_IMAGE"; //$NON-NLS-1$
	private static String ANIMATED_IMAGE_NAME = "ANIMATION_ANIMATED_IMAGE"; //$NON-NLS-1$

	Color background;

	private ImageLoader runLoader = new ImageLoader();
	boolean animated = false;
	Job animateJob;
	Job clearJob;
	private IJobProgressManagerListener listener;

	List items = Collections.synchronizedList(new ArrayList());

	public static AnimationManager getInstance() {
		if (singleton == null)
			singleton = new AnimationManager();
		return singleton;
	}

	AnimationManager() {
		URL iconsRoot =
			Platform.getPlugin(PlatformUI.PLUGIN_ID).find(
				new Path(ProgressManager.PROGRESS_FOLDER));
		ProgressManager manager = ProgressManager.getInstance();

		try {
			URL runningRoot = new URL(iconsRoot, RUNNING_ICON);
			URL backRoot = new URL(iconsRoot, BACKGROUND_ICON);

			animatedData = manager.getImageData(runningRoot, runLoader);
			if (animatedData != null)
				JFaceResources.getImageRegistry().put(
					ANIMATED_IMAGE_NAME,
					manager.getImage(animatedData[0]));

			disabledData = manager.getImageData(backRoot, runLoader);
			if (disabledData != null)
				JFaceResources.getImageRegistry().put(
					DISABLED_IMAGE_NAME,
					manager.getImage(disabledData[0]));

			listener = getProgressListener();
			ProgressManager.getInstance().addListener(listener);
		} catch (MalformedURLException exception) {
			ProgressManagerUtil.logException(exception);
		}
	}

	/**
	 * Add an items to the list
	 * @param item
	 */
	void addItem(final AnimationItem item) {
		items.add(item);
		if (background == null)
			background = item.getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		item.getControl().addDisposeListener(new DisposeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				AnimationManager.this.items.remove(item);
			}
		});
	}

	/**
	 * Get the current ImageData for the receiver.
	 * @return ImageData[]
	 */
	ImageData[] getImageData() {
		if (animated) {
			return animatedData;
		} else
			return disabledData;
	}

	/**
	 * Get the current Image for the receiver.
	 * @return Image
	 */
	Image getImage() {

		if (animated) {
			return JFaceResources.getImageRegistry().get(ANIMATED_IMAGE_NAME);
		} else
			return JFaceResources.getImageRegistry().get(DISABLED_IMAGE_NAME);
	}

	/**
	 * Return whether or not the current state is animated.
	 * @return boolean
	 */
	boolean isAnimated() {
		return animated;
	}

	/**
	 * Set whether or not the receiver is animated.
	 * @param boolean
	 */
	void setAnimated(final boolean bool) {

		animated = bool;
		if (bool) {
			ImageData[] imageDataArray = getImageData();
			if (isAnimated() && imageDataArray.length > 1) {
				getAnimateJob().schedule();
			}
		}
	}

	/**
	 * Dispose the images in the receiver.
	 */
	void dispose() {
		setAnimated(false);
		ProgressManager.getInstance().removeListener(listener);
	}

	/**
	 * Loop through all of the images in a multi-image file
	 * and display them one after another.
	 * @param monitor The monitor supplied to the job
	 */
	void animateLoop(IProgressMonitor monitor) {
		// Create an off-screen image to draw on, and a GC to draw with.
		// Both are disposed after the animation.

		if (items.size() == 0)
			return;

		if (!PlatformUI.isWorkbenchRunning())
			return;

		AnimationItem[] animationItems = getAnimationItems();

		Display display = PlatformUI.getWorkbench().getDisplay();

		ImageData[] imageDataArray = getImageData();
		ImageData imageData = imageDataArray[0];
		Image image = ProgressManager.getInstance().getImage(imageData);
		int imageDataIndex = 0;

		ImageLoader loader = getLoader();

		if (display.isDisposed()) {
			monitor.setCanceled(true);
			setAnimated(false);
			return;
		}

		Image offScreenImage =
			new Image(display, loader.logicalScreenWidth, loader.logicalScreenHeight);
		GC offScreenImageGC = new GC(offScreenImage);

		try {

			// Fill the off-screen image with the background color of the canvas.
			offScreenImageGC.setBackground(background);
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
							RGB backgroundRGB = imageData.palette.getRGB(backgroundPixel);
							bgColor = new Color(null, backgroundRGB);
						}
						try {
							offScreenImageGC.setBackground(bgColor != null ? bgColor : background);
							offScreenImageGC.fillRectangle(
								imageData.x,
								imageData.y,
								imageData.width,
								imageData.height);
						} finally {
							if (bgColor != null)
								bgColor.dispose();
						}
					} else if (imageData.disposalMethod == SWT.DM_FILL_PREVIOUS) {
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
					imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
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
					boolean refreshItems = false;
					for (int i = 0; i < animationItems.length; i++) {
						AnimationItem item = animationItems[i];
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
			animationDone();
		}
	}

	/**
	 * Get the animation items currently registered for the receiver.
	 * @return
	 */
	AnimationItem[] getAnimationItems() {
		AnimationItem[] animationItems = new AnimationItem[items.size()];
		items.toArray(animationItems);
		return animationItems;
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
		return JFaceResources.getImageRegistry().get(DISABLED_IMAGE_NAME).getBounds();
	}

	private IJobProgressManagerListener getProgressListener() {
		return new IJobProgressManagerListener() {

			HashSet jobs = new HashSet();

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#add(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void add(JobInfo info) {
				incrementJobCount(info);

			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refresh(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void refresh(JobInfo info) {

				int state = info.getJob().getState();
				if (state == Job.RUNNING)
					add(info);
				else
					remove(info);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshAll()
			 */
			public void refreshAll() {
				ProgressManager manager = ProgressManager.getInstance();
				jobs.clear();
				setAnimated(false);
				JobInfo[] currentInfos = manager.getJobInfos(showsDebug());
				for (int i = 0; i < currentInfos.length; i++) {
					JobInfo info = currentInfos[i];
					if (manager.isNonDisplayableJob(info.getJob(), showsDebug()))
						continue;
					add(currentInfos[i]);
				}

			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#remove(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void remove(JobInfo info) {
				if (jobs.contains(info.getJob())) {
					decrementJobCount(info.getJob());
				}

			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#showsDebug()
			 */
			public boolean showsDebug() {
				return false;
			}

			private void incrementJobCount(JobInfo info) {
				//Don't count the animate job itself
				if (isNotTracked(info))
					return;

				if (jobs.isEmpty())
					setAnimated(true);
				jobs.add(info.getJob());
			}

			private void decrementJobCount(Job job) {

				jobs.remove(job);
				if (jobs.isEmpty())
					setAnimated(false);
			}

			/** 
			 * If this is one of our jobs or not running then don't bother.
			 */
			private boolean isNotTracked(JobInfo info) {

				//We always track errors
				Job job = info.getJob();
				return job.getState() != Job.RUNNING || job == clearJob || job == animateJob;
			}
		};
	}

	private Job getAnimateJob() {
		if (animateJob == null) {
				animateJob = new Job(ProgressMessages.getString("AnimateJob.JobName")) {//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
				public IStatus run(IProgressMonitor monitor) {
					try {
						animationStarted();
						animateLoop(monitor);
						return Status.OK_STATUS;
					} catch (SWTException exception) {
						return ProgressManagerUtil.exceptionStatus(exception);
					}
				}

				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
				 */
				public boolean shouldSchedule() {
					return PlatformUI.isWorkbenchRunning();
				}
			};
			animateJob.setSystem(true);
			animateJob.setPriority(Job.DECORATE);
			animateJob.addJobChangeListener(new JobChangeAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void done(IJobChangeEvent event) {

					//Only schedule the job if we are showing anything
					if (isAnimated() && items.size() > 0)
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
	 * @return
	 */
	void createClearJob() {
			clearJob = new UIJob(ProgressMessages.getString("AnimationItem.RedrawJob")) {//$NON-NLS-1$
	/* (non-Javadoc)
	* @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	*/
			public IStatus runInUIThread(IProgressMonitor monitor) {
				AnimationItem[] animationItems = getAnimationItems();
				for (int i = 0; i < animationItems.length; i++)
					if (!animationItems[i].getControl().isDisposed())
						animationItems[i].getControl().redraw();
				return Status.OK_STATUS;
			}
		};
		clearJob.setSystem(true);
		clearJob.setPriority(Job.DECORATE);
	}

	/**
	 * Return the loader currently in use.
	 * @return ImageLoader
	 */
	ImageLoader getLoader() {
		return runLoader;
	}

	/**
	 * The animation is done. Get the items to clean up.
	 */
	private void animationDone() {

		UIJob animationDoneJob = new WorkbenchJob(ProgressMessages.getString("AnimationManager.AnimationCleanUp")) { //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				
				AnimationItem[] animationItems = getAnimationItems();
				for (int i = 0; i < animationItems.length; i++) {
					AnimationItem item = animationItems[i];
					item.animationDone();
				}
				return Status.OK_STATUS;
			}
		};
		animationDoneJob.setSystem(true);
		animationDoneJob.schedule();
		
	}

	/**
	 * The animation has started. Get the items to do any s
	 * other start behaviour.
	 */
	private void animationStarted() {
		
		UIJob animationDoneJob = new WorkbenchJob(ProgressMessages.getString("AnimationManager.AnimationStart")) { //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				
				AnimationItem[] animationItems = getAnimationItems();
				for (int i = 0; i < animationItems.length; i++) {
					AnimationItem item = animationItems[i];
					item.animationStart();
				}
				return Status.OK_STATUS;
			}
		};
		animationDoneJob.setSystem(true);
		animationDoneJob.schedule();

	}
	
	/**
	 * Get the preferred width for widgets displaying the 
	 * animation.
	 * @return int. Return 0 if there is no image data.
	 */
	int getPreferredWidth(){
		if(animatedData == null)
			return 0;
		else
			return animatedData[0].width;
		
	}

}
