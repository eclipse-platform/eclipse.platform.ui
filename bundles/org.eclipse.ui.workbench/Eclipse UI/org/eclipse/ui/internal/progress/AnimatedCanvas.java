package org.eclipse.ui.internal.progress;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
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

	public AnimatedCanvas(String animatedPath, String disabledPath) {
		animatedData = getImageData(animatedPath);
		animatedImage = getImage(animatedData[0]);

		disabledData = getImageData(disabledPath);
		disabledImage = getImage(disabledData[0]);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	private Image getImage(ImageData source) {
		ImageData mask = source.getTransparencyMask();
		return new Image(null, source, mask);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	private ImageData[] getImageData(String relativePath) {
		return loader.load(getClass().getResourceAsStream(relativePath));
	}

	public void createCanvas(Composite parent) {

		// Canvas to show the image.
		imageCanvas = new Canvas(parent, SWT.BORDER);
		imageCanvas.setBackground(
			parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

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

	Image getImage() {
		if (animated)
			return animatedImage;
		else
			return disabledImage;
	}

	private ImageData[] getImageData() {
		if (animated)
			return animatedData;
		else
			return disabledData;
	}

	void paintImage(PaintEvent event, Image image, ImageData imageData) {

		Display display = imageCanvas.getDisplay();
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
	 * @return
	 */
	public boolean isAnimated() {
		return animated;
	}

	/**
	 * @param b
	 */
	public void setAnimated(final boolean b) {
		if (getControl().isDisposed())
			return;
		getControl().getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if(imageCanvas.isDisposed())
					return;
				animated = b;
				imageCanvas.redraw();
				if (b)
					animate();

			}
		});

	}

	public Control getControl() {
		return imageCanvas;
	}

	public void dispose() {
		animatedImage.dispose();
		disabledImage.dispose();
	}

	/*
	 * Called when the Animate button is pressed.
	 */
	void animate() {
		Image image = getImage();
		ImageData[] imageDataArray = getImageData();
		if (isAnimated() && image != null && imageDataArray.length > 1) {
			Job animateJob = new AnimateJob() {
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public IStatus run(IProgressMonitor monitor) {
					try {
						animateLoop();
						return Status.OK_STATUS;
					} catch (final SWTException e) {
						return new Status(
							IStatus.ERROR,
							WorkbenchPlugin
								.getDefault()
								.getDescriptor()
								.getUniqueIdentifier(),
							IStatus.ERROR,
							"Error invoking job",
							e);
					}
				}
			};
			JobManager.getInstance().schedule(animateJob, 0);
		}
	}
	/*
	 * Loop through all of the images in a multi-image file
	 * and display them one after another.
	 */
	void animateLoop() {
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
					if(imageCanvas.isDisposed())
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
				while (isAnimated()) {
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

	/*
	 * Return the specified number of milliseconds.
	 * If the specified number of milliseconds is too small
	 * to see a visual change, then return a higher number.
	 */
	int visibleDelay(int ms) {
		if (ms < 20)
			return ms + 30;
		if (ms < 30)
			return ms + 10;
		return ms;
	}
}
