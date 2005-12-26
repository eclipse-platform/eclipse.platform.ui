package org.eclipse.ui.internal.forms.widgets;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class BusyIndicator extends Canvas {
	private static final int MARGIN = 2;

	private ImageData[] progressData;

	protected ImageLoader loader;

	protected Image image;

	protected Image animationImage;

	protected Thread busyThread;

	protected boolean stop;

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

		loadProgressImage();

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
	}

	private void loadProgressImage() {
		InputStream is = BusyIndicator.class
				.getResourceAsStream("progress.gif"); //$NON-NLS-1$
		if (is != null) {
			loader = new ImageLoader();
			try {
				progressData = loader.load(is);
			} catch (IllegalArgumentException e) {
			}
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = new Point(0, 0);
		if (image != null) {
			Rectangle ibounds = image.getBounds();
			size.x = ibounds.width;
			size.y = ibounds.height;
		}
		if (loader != null) {
			size.x = Math.max(size.x, loader.logicalScreenWidth);
			size.y = Math.max(size.y, loader.logicalScreenHeight);
		}
		size.x += MARGIN + MARGIN;
		size.y += MARGIN + MARGIN;
		return size;
	}

	/**
	 * Creates a thread to animate the image.
	 */
	protected synchronized void createBusyThread() {
		if (busyThread != null)
			return;

		stop = false;
		busyThread = new Thread() {
			private Image timage;

			public void run() {
				try {
					/*
					 * Create an off-screen image to draw on, and fill it with
					 * the shell background.
					 */
					Image offScreenImage = new Image(getDisplay(),
							loader.logicalScreenWidth,
							loader.logicalScreenHeight);
					final GC offScreenImageGC = new GC(offScreenImage);
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							drawBackground(offScreenImageGC, 0, 0, loader.logicalScreenWidth, loader.logicalScreenHeight);
						}
					});
					//if (getBackground()!=null) 
					//	offScreenImageGC.setBackground(getBackground());
					//offScreenImageGC.fillRectangle(0, 0,
					//		loader.logicalScreenWidth,
					//		loader.logicalScreenHeight);

					try {
						/*
						 * Create the first image and draw it on the off-screen
						 * image.
						 */
						int imageDataIndex = 0;
						ImageData imageData = progressData[imageDataIndex];
						if (timage != null && !timage.isDisposed())
							timage.dispose();
						timage = new Image(getDisplay(), imageData);
						offScreenImageGC.drawImage(timage, 0, 0,
								imageData.width, imageData.height, imageData.x,
								imageData.y, imageData.width, imageData.height);

						/*
						 * Now loop through the images, creating and drawing
						 * each one on the off-screen image before drawing it on
						 * the shell.
						 */
						int repeatCount = loader.repeatCount;
						while (loader.repeatCount == 0 || repeatCount > 0) {
							if (stop || isDisposed())
								break;
							switch (imageData.disposalMethod) {
							case SWT.DM_FILL_BACKGROUND:
								/*
								 * Fill with the background color before
								 * drawing.
								 */
								offScreenImageGC.fillRectangle(imageData.x,
										imageData.y, imageData.width,
										imageData.height);
								break;
							case SWT.DM_FILL_PREVIOUS:
								/* Restore the previous image before drawing. */
								offScreenImageGC.drawImage(timage, 0, 0,
										imageData.width, imageData.height,
										imageData.x, imageData.y,
										imageData.width, imageData.height);
								break;
							}

							imageDataIndex = (imageDataIndex + 1)
									% progressData.length;
							imageData = progressData[imageDataIndex];
							timage.dispose();
							timage = new Image(getDisplay(), imageData);
							offScreenImageGC.drawImage(timage, 0, 0,
									imageData.width, imageData.height,
									imageData.x, imageData.y, imageData.width,
									imageData.height);

							/* Draw the off-screen image to the shell. */
							animationImage = offScreenImage;
							getDisplay().syncExec(new Runnable() {
								public void run() {
									if (!isDisposed())
										redraw();
								}
							});
							/*
							 * Sleep for the specified delay time (adding
							 * commonly-used slow-down fudge factors).
							 */
							try {
								int ms = imageData.delayTime * 10;
								if (ms < 20)
									ms += 30;
								if (ms < 30)
									ms += 10;
								Thread.sleep(ms);
							} catch (InterruptedException e) {
							}

							/*
							 * If we have just drawn the last image, decrement
							 * the repeat count and start again.
							 */
							if (imageDataIndex == progressData.length - 1)
								repeatCount--;
						}
					} catch (SWTException ex) {
						System.out
								.println("There was an error animating the GIF");
					} finally {
						if (offScreenImage != null
								&& !offScreenImage.isDisposed())
							offScreenImage.dispose();
						if (offScreenImageGC != null
								&& !offScreenImageGC.isDisposed())
							offScreenImageGC.dispose();
						if (timage != null && !timage.isDisposed())
							timage.dispose();
					}
					if (busyThread == null)
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								animationImage = null;
								redraw();
							}
						});
				} catch (Exception e) {
					// Trace.trace(Trace.WARNING, "Busy error", e);
					// //$NON-NLS-1$
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
				stop = true;
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
}
