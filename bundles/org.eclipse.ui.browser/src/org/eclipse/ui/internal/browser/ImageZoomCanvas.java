/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.util.function.BooleanSupplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Shows one image with scrollbars. A click zooms in, a click with the
 * {@link SWT#MOD1} key zooms out, and the mouse wheel with {@link SWT#MOD1}
 * zooms continuously. The point under the mouse stays under the mouse. The
 * context menu returns to fit mode or shows the image at its actual size.
 * <p>
 * There is no keyboard shortcut yet. Reset to fit with Ctrl+0 and zoom with
 * Ctrl+plus and Ctrl+minus need workbench commands, because the workbench
 * already binds some of these keys, and are left for a later change.
 * </p>
 * <p>
 * An image with several frames (an animated GIF) is played.
 * </p>
 */
public class ImageZoomCanvas extends Canvas {

	/** Size of the zoom cursor in points. */
	private static final int CURSOR_SIZE = 20;
	/** The center of the lens, so the cursor points at what is zoomed. */
	private static final int CURSOR_HOTSPOT = 8;

	private final ImageZoomModel model = new ImageZoomModel();
	private final Cursor zoomInCursor;
	private final Cursor zoomOutCursor;

	/** The scale when the current pinch gesture began, 0 if there is none. */
	private double pinchStartScale;

	private Image image;
	private int imageWidth;
	private int imageHeight;
	private String message;

	private ImageData[] frames;
	private int frameIndex;
	private int loopsLeft;
	private int repeatCount;
	private Image snapshot;
	private Runnable animationStep;

	/**
	 * @param parent the parent
	 * @param style  the style, see {@link Canvas}
	 */
	public ImageZoomCanvas(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.DOUBLE_BUFFERED);
		Display display = getDisplay();
		Cursor inCursor = createZoomCursor(display, true);
		Cursor outCursor;
		try {
			outCursor = createZoomCursor(display, false);
		} catch (RuntimeException | Error e) {
			// The Dispose listener is not registered yet.
			inCursor.dispose();
			throw e;
		}
		zoomInCursor = inCursor;
		zoomOutCursor = outCursor;
		setCursor(zoomInCursor);

		ScrollBar horizontal = getHorizontalBar();
		horizontal.setIncrement(20);
		horizontal.addListener(SWT.Selection, e -> redraw());
		ScrollBar vertical = getVerticalBar();
		vertical.setIncrement(20);
		vertical.addListener(SWT.Selection, e -> redraw());

		addListener(SWT.Paint, e -> paint(e.gc));
		addListener(SWT.Resize, e -> updateScrollbars());
		addListener(SWT.MouseDown, e -> {
			if (e.button == 1) {
				boolean zoomOut = (e.stateMask & SWT.MOD1) != 0;
				zoomAround(e.x, e.y, () -> zoomOut ? model.zoomOut(fitScale()) : model.zoomIn(fitScale()));
			}
		});
		addListener(SWT.MouseMove, e -> updateCursor((e.stateMask & SWT.MOD1) != 0));
		addListener(SWT.MouseEnter, e -> updateCursor((e.stateMask & SWT.MOD1) != 0));
		addListener(SWT.KeyDown, e -> {
			if (e.keyCode == SWT.MOD1) {
				updateCursor(true);
			}
		});
		addListener(SWT.KeyUp, e -> {
			if (e.keyCode == SWT.MOD1) {
				updateCursor(false);
			}
		});
		addListener(SWT.Gesture, this::handleGesture);
		addListener(SWT.MouseWheel, e -> {
			if ((e.stateMask & SWT.MOD1) != 0) {
				e.doit = false;
				// Event.count is not the same on every platform (3 per notch on GTK, lines
				// per notch on Windows, at least 1 on macOS), so only the direction is used.
				double notches = Integer.signum(e.count);
				zoomAround(e.x, e.y, () -> model.zoomByWheel(notches, fitScale()));
			}
		});
		createContextMenu();
		addListener(SWT.Dispose, e -> {
			Menu menu = getMenu();
			if (menu != null && !menu.isDisposed()) {
				menu.dispose();
			}
			clearImage();
			zoomInCursor.dispose();
			zoomOutCursor.dispose();
		});
	}

	private void createContextMenu() {
		Menu menu = new Menu(this);
		MenuItem fit = new MenuItem(menu, SWT.PUSH);
		fit.setText(Messages.imageViewerFit);
		fit.addListener(SWT.Selection, e -> {
			model.setFit();
			updateScrollbars();
			redraw();
		});
		MenuItem actualSize = new MenuItem(menu, SWT.PUSH);
		actualSize.setText(Messages.imageViewerActualSize);
		actualSize.addListener(SWT.Selection, e -> {
			Rectangle client = getClientArea();
			zoomAround(client.width / 2, client.height / 2, () -> model.actualSize(fitScale()));
		});
		setMenu(menu);
	}

	/**
	 * Shows a text instead of an image, for example the reason why there is none.
	 * The text is drawn while there is no image.
	 *
	 * @param text the text to show
	 */
	public void setMessage(String text) {
		message = text;
		redraw();
	}

	/**
	 * Shows the given images and returns to fit mode. The canvas creates its own
	 * {@link Image} from them and disposes it when it is disposed or when another
	 * image is set.
	 * <p>
	 * Only a GIF with several images is played. Other formats, such as ICO and
	 * TIFF, also return several images, but these are alternatives and pages. The
	 * largest of them is shown.
	 * </p>
	 *
	 * @param loader the loader that loaded <code>data</code>
	 * @param data   the images, nothing is shown if it is empty
	 */
	public void setImageData(ImageLoader loader, ImageData[] data) {
		clearImage();
		if (data.length == 0) {
			return;
		}
		if (data.length > 1 && data[0].type == SWT.IMAGE_GIF) {
			startAnimation(loader, data);
		} else {
			ImageData largest = data[0];
			for (ImageData candidate : data) {
				if ((long) candidate.width * candidate.height > (long) largest.width * largest.height) {
					largest = candidate;
				}
			}
			image = new Image(getDisplay(), largest);
			Rectangle bounds = image.getBounds();
			imageWidth = bounds.width;
			imageHeight = bounds.height;
		}
		model.setFit();
		updateScrollbars();
		redraw();
	}

	private void startAnimation(ImageLoader loader, ImageData[] data) {
		int width = loader.logicalScreenWidth;
		int height = loader.logicalScreenHeight;
		for (ImageData frame : data) {
			width = Math.max(width, frame.x + frame.width);
			height = Math.max(height, frame.y + frame.height);
		}
		imageWidth = width;
		imageHeight = height;
		image = new Image(getDisplay(), width, height);
		fillBackground(new Rectangle(0, 0, width, height));
		frames = data;
		frameIndex = 0;
		repeatCount = loader.repeatCount;
		loopsLeft = repeatCount;
		renderFrame(frames[0]);
		scheduleNextFrame();
	}

	private void scheduleNextFrame() {
		// Delays of 0 and 10 ms are common in GIF files and are played as 100 ms.
		int delay = frames[frameIndex].delayTime * 10;
		Runnable step = new Runnable() {
			@Override
			public void run() {
				if (!isDisposed() && animationStep == this) {
					advanceFrame();
				}
			}
		};
		animationStep = step;
		getDisplay().timerExec(delay <= 10 ? 100 : delay, step);
	}

	private void advanceFrame() {
		int next = frameIndex + 1;
		if (next == frames.length) {
			// repeat count 0 means forever
			if (repeatCount != 0 && --loopsLeft <= 0) {
				animationStep = null;
				return;
			}
			next = 0;
		}
		ImageData current = frames[frameIndex];
		GC gc = new GC(image);
		try {
			if (current.disposalMethod == SWT.DM_FILL_BACKGROUND) {
				// The canvas background is used on purpose, not the GIF background colour: it is
				// usually unspecified (-1) and browsers clear a disposed frame to transparent.
				gc.setBackground(getBackground());
				gc.fillRectangle(current.x, current.y, current.width, current.height);
			} else if (current.disposalMethod == SWT.DM_FILL_PREVIOUS && snapshot != null) {
				gc.drawImage(snapshot, 0, 0);
			}
		} finally {
			gc.dispose();
		}
		if (next == 0) {
			// A new loop starts on a cleared image. This comes after the disposal of the
			// last frame, because DM_FILL_PREVIOUS would bring back the old content.
			fillBackground(new Rectangle(0, 0, imageWidth, imageHeight));
		}
		frameIndex = next;
		renderFrame(frames[frameIndex]);
		scheduleNextFrame();
		redraw();
	}

	/** Draws a frame into the frame buffer. */
	private void renderFrame(ImageData data) {
		if (snapshot != null) {
			snapshot.dispose();
			snapshot = null;
		}
		if (data.disposalMethod == SWT.DM_FILL_PREVIOUS) {
			snapshot = new Image(getDisplay(), imageWidth, imageHeight);
			GC snapshotGc = new GC(snapshot);
			try {
				snapshotGc.drawImage(image, 0, 0);
			} finally {
				snapshotGc.dispose();
			}
		}
		Image frame = new Image(getDisplay(), data);
		GC gc = null;
		try {
			gc = new GC(image);
			gc.drawImage(frame, data.x, data.y);
		} finally {
			if (gc != null) {
				gc.dispose();
			}
			frame.dispose();
		}
	}

	private void fillBackground(Rectangle area) {
		GC gc = new GC(image);
		try {
			gc.setBackground(getBackground());
			gc.fillRectangle(area);
		} finally {
			gc.dispose();
		}
	}

	private void clearImage() {
		if (animationStep != null) {
			getDisplay().timerExec(-1, animationStep);
			animationStep = null;
		}
		frames = null;
		if (snapshot != null) {
			snapshot.dispose();
			snapshot = null;
		}
		if (image != null) {
			image.dispose();
			image = null;
		}
		imageWidth = 0;
		imageHeight = 0;
	}

	private double fitScale() {
		return fitScale(getClientArea());
	}

	private double fitScale(Rectangle client) {
		return ImageZoomModel.fitScale(imageWidth, imageHeight, client.width, client.height);
	}

	private double currentScale(Rectangle client) {
		return model.getScale(fitScale(client));
	}

	/**
	 * Zooms with a pinch on a track pad. The magnification that SWT reports is
	 * relative to the start of the gesture, so the scale at that moment is kept.
	 */
	private void handleGesture(Event e) {
		switch (e.detail) {
		case SWT.GESTURE_BEGIN:
			pinchStartScale = image != null ? currentScale(getClientArea()) : 0;
			break;
		case SWT.GESTURE_END:
			pinchStartScale = 0;
			break;
		case SWT.GESTURE_MAGNIFY:
			if (pinchStartScale > 0 && e.magnification > 0) {
				e.doit = false;
				double target = pinchStartScale * e.magnification;
				zoomAround(e.x, e.y, () -> model.zoomTo(target, fitScale()));
			}
			break;
		default:
			break;
		}
	}

	private void updateCursor(boolean zoomOut) {
		Cursor wanted = zoomOut ? zoomOutCursor : zoomInCursor;
		if (getCursor() != wanted) {
			setCursor(wanted);
		}
	}

	/**
	 * Changes the scale and scrolls so that the image point under (x, y) stays
	 * there, as far as the scroll range allows.
	 */
	private void zoomAround(int x, int y, BooleanSupplier changeScale) {
		if (image == null) {
			return;
		}
		ScrollBar horizontal = getHorizontalBar();
		ScrollBar vertical = getVerticalBar();
		Rectangle client = getClientArea();
		double oldScale = currentScale(client);
		int oldOriginX = ImageZoomModel.origin(ImageZoomModel.scaledSize(imageWidth, oldScale), client.width,
				horizontal.getSelection());
		int oldOriginY = ImageZoomModel.origin(ImageZoomModel.scaledSize(imageHeight, oldScale), client.height,
				vertical.getSelection());
		double imageX = ImageZoomModel.imageCoordinate(x, oldOriginX, oldScale);
		double imageY = ImageZoomModel.imageCoordinate(y, oldOriginY, oldScale);

		if (!changeScale.getAsBoolean()) {
			return;
		}
		updateScrollbars();
		client = getClientArea();
		double newScale = currentScale(client);
		int scaledWidth = ImageZoomModel.scaledSize(imageWidth, newScale);
		int scaledHeight = ImageZoomModel.scaledSize(imageHeight, newScale);
		if (scaledWidth > client.width) {
			horizontal.setSelection(
					clamp(ImageZoomModel.scrollOffsetFor(imageX, x, newScale), 0, scaledWidth - client.width));
		}
		if (scaledHeight > client.height) {
			vertical.setSelection(
					clamp(ImageZoomModel.scrollOffsetFor(imageY, y, newScale), 0, scaledHeight - client.height));
		}
		redraw();
	}

	private void updateScrollbars() {
		if (isDisposed()) {
			return;
		}
		// A scrollbar that appears or disappears changes the client area. Once more
		// is enough for the second bar.
		for (int pass = 0; pass < 2; pass++) {
			Rectangle client = getClientArea();
			double scale = currentScale(client);
			configure(getHorizontalBar(), ImageZoomModel.scaledSize(imageWidth, scale), client.width);
			configure(getVerticalBar(), ImageZoomModel.scaledSize(imageHeight, scale), client.height);
		}
	}

	private static void configure(ScrollBar bar, int size, int viewSize) {
		if (size <= viewSize) {
			bar.setVisible(false);
			bar.setSelection(0);
			return;
		}
		bar.setMaximum(size);
		bar.setThumb(viewSize);
		bar.setPageIncrement(viewSize);
		bar.setSelection(Math.min(bar.getSelection(), size - viewSize));
		bar.setVisible(true);
	}

	private void paint(GC gc) {
		if (image == null) {
			if (message != null) {
				gc.drawText(message, 10, 10, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
			}
			return;
		}
		Rectangle client = getClientArea();
		double scale = currentScale(client);
		int scaledWidth = ImageZoomModel.scaledSize(imageWidth, scale);
		int scaledHeight = ImageZoomModel.scaledSize(imageHeight, scale);
		int originX = ImageZoomModel.origin(scaledWidth, client.width, getHorizontalBar().getSelection());
		int originY = ImageZoomModel.origin(scaledHeight, client.height, getVerticalBar().getSelection());

		// Only the visible part is drawn. At 2000% the whole image can be far larger
		// than the window.
		int visibleLeft = Math.max(0, originX);
		int visibleTop = Math.max(0, originY);
		int visibleRight = Math.min(client.width, originX + scaledWidth);
		int visibleBottom = Math.min(client.height, originY + scaledHeight);
		if (visibleRight <= visibleLeft || visibleBottom <= visibleTop) {
			return;
		}
		int sourceLeft = clamp((int) Math.floor((visibleLeft - originX) / scale), 0, imageWidth);
		int sourceTop = clamp((int) Math.floor((visibleTop - originY) / scale), 0, imageHeight);
		int sourceRight = clamp((int) Math.ceil((visibleRight - originX) / scale), 0, imageWidth);
		int sourceBottom = clamp((int) Math.ceil((visibleBottom - originY) / scale), 0, imageHeight);
		int destLeft = originX + (int) Math.round(sourceLeft * scale);
		int destTop = originY + (int) Math.round(sourceTop * scale);
		int destRight = originX + (int) Math.round(sourceRight * scale);
		int destBottom = originY + (int) Math.round(sourceBottom * scale);

		try {
			gc.setAdvanced(true);
			gc.setInterpolation(scale > ImageZoomModel.PIXELATED_ABOVE ? SWT.NONE : SWT.HIGH);
		} catch (SWTException e) {
			// no advanced graphics, the default scaling is used
		}
		int destWidth = Math.max(1, destRight - destLeft);
		int destHeight = Math.max(1, destBottom - destTop);
		gc.drawImage(image, sourceLeft, sourceTop, sourceRight - sourceLeft, sourceBottom - sourceTop, destLeft,
				destTop, destWidth, destHeight);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Creates a magnifier with a plus or a minus. SWT has no zoom cursors.
	 * <p>
	 * The magnifier is drawn with an alpha channel and for the zoom that SWT asks
	 * for, so that it is sharp on a scaled display and has no background.
	 * </p>
	 */
	private static Cursor createZoomCursor(Display display, boolean zoomIn) {
		ImageGcDrawer drawer = new ImageGcDrawer() {
			@Override
			public void drawOn(GC gc, int width, int height) {
				drawMagnifier(gc, display, zoomIn);
			}

			@Override
			public int getGcStyle() {
				return SWT.TRANSPARENT;
			}
		};
		return new Cursor(display, zoom -> {
			Image image = new Image(display, drawer, CURSOR_SIZE, CURSOR_SIZE);
			try {
				return image.getImageData(zoom);
			} finally {
				image.dispose();
			}
		}, CURSOR_HOTSPOT, CURSOR_HOTSPOT);
	}

	/**
	 * Draws the lens, the handle and the sign in a {@link #CURSOR_SIZE} square,
	 * with thin strokes. There is no halo around the shapes, so that nothing
	 * looks like a second handle on a coloured image. The handle starts inside
	 * the lens and the lens is drawn over it, so that there is no gap between
	 * them.
	 */
	private static void drawMagnifier(GC gc, Display display, boolean zoomIn) {
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		gc.setAntialias(SWT.ON);

		// handle, starting at the centre of the lens
		gc.setLineCap(SWT.CAP_FLAT);
		gc.setForeground(black);
		gc.setLineWidth(2);
		gc.drawLine(8, 8, 17, 17);

		// lens, drawn over the handle: white inside, black ring
		gc.setBackground(white);
		gc.fillOval(3, 3, 10, 10);
		gc.setForeground(black);
		gc.setLineWidth(1);
		gc.drawOval(3, 3, 10, 10);

		// sign
		gc.setLineCap(SWT.CAP_FLAT);
		gc.drawLine(5, 8, 11, 8);
		if (zoomIn) {
			gc.drawLine(8, 5, 8, 11);
		}
	}
}
