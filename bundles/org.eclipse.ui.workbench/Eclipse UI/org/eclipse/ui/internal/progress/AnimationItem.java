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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.internal.progress.ProgressMessages;

public class AnimationItem {

	IWorkbenchWindow window;
	private static final String PROGRESS_FOLDER = "icons/full/progress/"; //$NON-NLS-1$
	private static final String RUNNING_ICON = "running.gif"; //$NON-NLS-1$
	private static final String BACKGROUND_ICON = "back.gif"; //$NON-NLS-1$
	
	private ImageData[] animatedData;
	private ImageData[] disabledData;
	private Image animatedImage;
	private Image disabledImage;
	Canvas imageCanvas;
	GC imageCanvasGC;
	private ImageLoader loader = new ImageLoader();
	boolean animated = false;
	private Job animateJob;
	private IJobChangeListener listener;

	/**
	 * Create a new instance of the receiver.
	 * @param animatedPath
	 * @param disabledPath
	 */

	public AnimationItem(IWorkbenchWindow workbenchWindow) {

		this.window = workbenchWindow;
		//Get the progress manager started if it hasn't already
		JobProgressManager.getInstance();
		URL iconsRoot =
			Platform.getPlugin(PlatformUI.PLUGIN_ID).find(
				new Path(PROGRESS_FOLDER));

		try {
			URL runningRoot = new URL(iconsRoot, RUNNING_ICON);
			URL backRoot = new URL(iconsRoot, BACKGROUND_ICON);
			animatedData = getImageData(runningRoot);
			if (animatedData != null)
				animatedImage = getImage(animatedData[0]);

			disabledData = getImageData(backRoot);
			if (disabledData != null)
				disabledImage = getImage(disabledData[0]);

			listener = getJobListener();
			Platform.getJobManager().addJobChangeListener(listener);
		} catch (MalformedURLException exception) {
			ProgressUtil.logException(exception);
		}
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
	ImageData[] getImageData(URL fileSystemPath) {
		try {
			InputStream stream = fileSystemPath.openStream();
			ImageData[] result = loader.load(stream);
			stream.close();
			return result;
		} catch (FileNotFoundException exception) {
			ProgressUtil.logException(exception);
			return null;
		} catch (IOException exception) {
			ProgressUtil.logException(exception);
			return null;
		}
	}

	/**
	 * Create the canvas that will display the image.
	 * @param parent
	 */
	public void createControl(Composite parent) {

		// Canvas to show the image.
		imageCanvas = new Canvas(parent, SWT.NONE);
		imageCanvas.setBackground(
			parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		imageCanvas.setToolTipText(ProgressMessages.getString("AnimationItem.HoverHelp")); //$NON-NLS-1$

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

		imageCanvas.addMouseListener(new MouseListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent arg0) {
				ProgressUtil.openProgressView(window);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent arg0) {
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent arg0) {

			}
		});

		imageCanvas
			.getAccessible()
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getValue(org.eclipse.swt.accessibility.AccessibleControlEvent)
			 */
			public void getValue(AccessibleControlEvent arg0) {
				if (animated)
					arg0.result = ProgressMessages.getString("AnimationItem.InProgressStatus"); //$NON-NLS-1$
				else
					arg0.result = ProgressMessages.getString("AnimationItem.NotRunningStatus"); //$NON-NLS-1$
			}
		});
		
		imageCanvas.addHelpListener(new HelpListener(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.HelpListener#helpRequested(org.eclipse.swt.events.HelpEvent)
			 */
			public void helpRequested(HelpEvent e) {
				// XXX Auto-generated method stub

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
	ImageData[] getImageData() {
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
	void paintImage(
		PaintEvent event,
		Image image,
		ImageData imageData) {

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

		animated = bool;
		if (bool) {
			Image image = getImage();
			ImageData[] imageDataArray = getImageData();
			if (isAnimated() && image != null && imageDataArray.length > 1) {
				getAnimateJob().schedule();
			}
		}
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
		Platform.getJobManager().removeJobChangeListener(listener);
	}

	/**
	 * Loop through all of the images in a multi-image file
	 * and display them one after another.
	 * @param monitor The monitor supplied to the job
	 */
	void animateLoop(IProgressMonitor monitor) {
		// Create an off-screen image to draw on, and a GC to draw with.
		// Both are disposed after the animation.

		if (getControl().isDisposed())
			return;
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
			final boolean[] disposed = { false };
			// Use syncExec to get the background color of the imageCanvas.
			display.syncExec(new Runnable() {
				public void run() {
					if (imageCanvas.isDisposed()) {
						disposed[0] = true;
						return;
					}

					backgrounds[0] = imageCanvas.getBackground();
				}
			});

			//If we never got the background then leave.
			if (disposed[0])
				return;
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
			image.dispose();
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

	/**
	 * Add a listener to the job manager for the receiver.
	 *
	 */
	private IJobChangeListener getJobListener() {
		return new JobChangeAdapter() {

			HashSet jobs = new HashSet();

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void aboutToRun(IJobChangeEvent event) {
				incrementJobCount(event.getJob());
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				if (jobs.contains(event.getJob())) {
					decrementJobCount(event.getJob());
				}
			}

			private void incrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job.isSystem())
					return;
				if (jobs.size() == 0)
					setAnimated(true);
				jobs.add(job);
			}

			private void decrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job.isSystem())
					return;
				jobs.remove(job);
				if(jobs.isEmpty())
					setAnimated(false);
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
						animateLoop(monitor);
						return Status.OK_STATUS;
					} catch (SWTException exception) {
						return ProgressUtil.exceptionStatus(exception);
					}
				}
			};
			animateJob.setSystem(true);
			animateJob.setPriority(Job.DECORATE);
			animateJob.addJobChangeListener(new JobChangeAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void done(IJobChangeEvent event) {
					//Clear the image
						UIJob clearJob = new UIJob(ProgressMessages.getString("AnimationItem.RedrawJob")) {//$NON-NLS-1$
						/* (non-Javadoc)
	 					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
						 */
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if (!imageCanvas.isDisposed())
								imageCanvas.redraw();
							return Status.OK_STATUS;
						}
					};
					clearJob.setSystem(true);
					clearJob.schedule();
				}
			});
		}
		return animateJob;
	}
}
