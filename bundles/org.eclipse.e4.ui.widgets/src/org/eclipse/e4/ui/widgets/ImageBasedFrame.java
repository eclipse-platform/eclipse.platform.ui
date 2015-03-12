/*******************************************************************************
 * Copyright (c) 2012, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;


public class ImageBasedFrame extends Canvas {
	//TODO: Change to the public after API freeze
	private static final String HANDLE_IMAGE= "handleImage"; //$NON-NLS-1$

	private static final String FRAME_IMAGE= "frameImage"; //$NON-NLS-1$

	private Control framedControl;

	private boolean draggable = true;
	private boolean vertical = true;

	private int w1;
	private int w2;
	private int w3;
	private int h1;
	private int h2;
	private int h3;
	private Image imageCache;

	private Image handle;
	private int handleWidth;
	private int handleHeight;

	protected String id;

	public ImageBasedFrame(Composite parent, Control toWrap, boolean vertical,
			boolean draggable) {
		super(parent, SWT.NONE);

		this.framedControl = toWrap;
		this.vertical = vertical;
		this.draggable = draggable;

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				drawFrame(e);
			}
		});

		addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event event) {
				ImageBasedFrame frame = (ImageBasedFrame) event.widget;
				frame.setCursor(null);
			}
		});

		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				// Compute the display location for the handle
				// Note that this is an empty rect if !draggable
				Rectangle handleRect = getHandleRect();

				ImageBasedFrame frame = (ImageBasedFrame) e.widget;
				if (handleRect.contains(e.x, e.y)) {
					frame.setCursor(frame.getDisplay().getSystemCursor(
							SWT.CURSOR_SIZEALL));
				} else {
					frame.setCursor(null);
				}
			}
		});

		toWrap.setParent(this);
		toWrap.pack(true);

		toWrap.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				pack(true);
			}

			public void controlMoved(ControlEvent e) {
			}
		});
		if (vertical) {
			toWrap.setLocation(w1, h1 + handleHeight);
		} else {
			toWrap.setLocation(w1 + handleWidth, h1);
		}
		setSize(computeSize(-1, -1));

		if (toWrap instanceof ToolBar) {
			id = "TB";// ((ToolBar) toWrap).getItem(0).getToolTipText(); //$NON-NLS-1$
		}
	}

	public Rectangle getHandleRect() {
		Rectangle handleRect = new Rectangle(0, 0, 0, 0);
		if (!draggable || handle.isDisposed())
			return handleRect;

		if (vertical) {
			handleRect.x = w1;
			handleRect.y = h1;
			handleRect.width = framedControl.getSize().x;
			handleRect.height = handle.getBounds().height;
		} else {
			handleRect.x = w1;
			handleRect.y = h1;
			handleRect.width = handle.getBounds().width;
			handleRect.height = framedControl.getSize().y;
		}
		return handleRect;
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		if (framedControl == null || framedControl.isDisposed())
			return new Point(0, 0);

		if (vertical) {
			int width = w1 + framedControl.getSize().x + w3;
			int height = h1 + handleHeight + framedControl.getSize().y + h3;
			return new Point(width, height);
		} else {
			int width = w1 + handleWidth + framedControl.getSize().x + w3;
			int height = h1 + framedControl.getSize().y + h3;
			return new Point(width, height);
		}
	}

	protected void drawFrame(PaintEvent e) {
		if (handle.isDisposed() || (imageCache != null && imageCache.isDisposed())) {
			reskin(SWT.NONE);
			return;
		}

		if (framedControl == null || framedControl.isDisposed())
			return;

		Point inner = framedControl.getSize();
		int handleWidth = (handle != null && !vertical) ? handle.getBounds().width
				: 0;
		int handleHeight = (handle != null && vertical) ? handle.getBounds().height
				: 0;

		Rectangle srcRect = new Rectangle(0, 0, 0, 0);
		Rectangle dstRect = new Rectangle(0, 0, 0, 0);

		// Top Left
		srcRect.x = 0;
		srcRect.y = 0;
		srcRect.width = w1;
		srcRect.height = h1;
		dstRect.x = 0;
		dstRect.y = 0;
		dstRect.width = w1;
		dstRect.height = h1;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Top Rail
		srcRect.x = w1;
		srcRect.y = 0;
		srcRect.width = w2;
		srcRect.height = h1;
		dstRect.x = w1;
		dstRect.y = 0;
		dstRect.width = inner.x + handleWidth;
		dstRect.height = h1;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// handle (if vertical)
		if (handleHeight > 0) {
			srcRect.x = 0;
			srcRect.y = 0;
			srcRect.width = handle.getBounds().width;
			srcRect.height = handle.getBounds().height;
			dstRect.x = w1;
			dstRect.y = h1;
			dstRect.width = inner.x;
			dstRect.height = handleHeight;
			e.gc.drawImage(handle, srcRect.x, srcRect.y, srcRect.width,
					srcRect.height, dstRect.x, dstRect.y, dstRect.width,
					dstRect.height);
		}

		// Top Right
		srcRect.x = w1 + w2;
		srcRect.y = 0;
		srcRect.width = w3;
		srcRect.height = h1;
		dstRect.x = w1 + handleWidth + inner.x;
		dstRect.y = 0;
		dstRect.width = w3;
		dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Left Rail
		srcRect.x = 0;
		srcRect.y = h1;
		srcRect.width = w1;
		srcRect.height = h2;
		dstRect.x = 0;
		dstRect.y = h1;
		dstRect.width = w1;
		dstRect.height = inner.y + handleHeight;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Handle (if horizontal)
		if (handleWidth > 0) {
			srcRect.x = 0;
			srcRect.y = 0;
			srcRect.width = handle.getBounds().width;
			srcRect.height = handle.getBounds().height;
			dstRect.x = w1;
			dstRect.y = h1;
			dstRect.width = handleWidth;
			dstRect.height = inner.y;
			e.gc.drawImage(handle, srcRect.x, srcRect.y, srcRect.width,
					srcRect.height, dstRect.x, dstRect.y, dstRect.width,
					dstRect.height);
		}

		// Right Rail
		srcRect.x = w1 + w2;
		srcRect.y = h1;
		srcRect.width = w3;
		srcRect.height = h2;
		dstRect.x = w1 + handleWidth + inner.x;
		dstRect.y = h1;
		dstRect.width = w3;
		dstRect.height = inner.y + handleHeight;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Bottom Left
		srcRect.x = 0;
		srcRect.y = h1 + h2;
		srcRect.width = w1;
		srcRect.height = h3;
		dstRect.x = 0;
		dstRect.y = h1 + handleHeight + inner.y;
		dstRect.width = w1;
		dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Bottom Rail
		srcRect.x = w1;
		srcRect.y = h1 + h2;
		srcRect.width = w2;
		srcRect.height = h3;
		dstRect.x = w1;
		dstRect.y = h1 + handleHeight + inner.y;
		dstRect.width = handleWidth + inner.x;
		dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Bottom right
		srcRect.x = w1 + w2;
		srcRect.y = h1 + h2;
		srcRect.width = w3;
		srcRect.height = h3;
		dstRect.x = w1 + handleWidth + inner.x;
		dstRect.y = h1 + handleHeight + inner.y;
		dstRect.width = w3;
		dstRect.height = h3;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);

		// Imterior
		srcRect.x = w1;
		srcRect.y = h1;
		srcRect.width = w2;
		srcRect.height = h2;
		dstRect.x = w1 + handleWidth;
		dstRect.y = h1 + handleHeight;
		dstRect.width = inner.x;
		dstRect.height = inner.y;
		e.gc.drawImage(imageCache, srcRect.x, srcRect.y, srcRect.width,
				srcRect.height, dstRect.x, dstRect.y, dstRect.width,
				dstRect.height);
	}

	public Image getImageCache() {
		return imageCache;
	}

	public Image getHandleImage() {
		return handle;
	}

	public void setImages(Image frameImage, Integer[] frameInts,
			Image handleImage) {
		if (frameImage != null) {
			imageCache = frameImage;
			setData(FRAME_IMAGE, frameImage);
		}
		if (handleImage != null) {
			handle = handleImage;
			setData(HANDLE_IMAGE, handleImage);
		}

		if (frameInts != null) {
			w1 = frameInts[0];
			w2 = frameInts[1];
			h1 = frameInts[2];
			h2 = frameInts[3];
			w3 = imageCache.getBounds().width - (w1 + w2);
			h3 = imageCache.getBounds().height - (h1 + h2);
		}

		// Compute the size of the handle in the 'offset' dimension
		handleWidth = (handle != null && !vertical) ? handle.getBounds().width
				: 0;
		handleHeight = (handle != null && vertical) ? handle.getBounds().height
				: 0;

		if (vertical) {
			framedControl.setLocation(w1, h1 + handleHeight);
		} else {
			framedControl.setLocation(w1 + handleWidth, h1);
		}
		setSize(computeSize(-1, -1));
	}
}
