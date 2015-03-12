/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 *
 */
public class TrimPaneLayout extends Layout {
	private static int BORDER_WIDTH = 4;
	private int fixedCorner;

	public Rectangle hSizingRect;
	public Rectangle vSizingRect;
	public Rectangle cornerRect;
	private Rectangle clientRect;
	private boolean resizeInstalled = false;

	private static int NOT_SIZING = 0;
	private static int HORIZONTAL_SIZING = 1;
	private static int VERTICAL_SIZING = 2;
	private static int CORNER_SIZING = 3;

	int trackState = SWT.NONE;
	protected Point curPos;
	private MToolControl toolControl;

	public TrimPaneLayout(MToolControl toolControl, int barSide) {
		this.toolControl = toolControl;
		this.fixedCorner = barSide;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int,
	 * boolean)
	 */
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return new Point(600, 400);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		installResize(composite);

		if (composite.getChildren().length != 1)
			return;

		if (fixedCorner == SWT.NONE)
			return;

		Rectangle bounds = composite.getBounds();

		if (isFixed(SWT.TOP)) {
			if (isFixed(SWT.RIGHT)) {
				hSizingRect = new Rectangle(0, 0, BORDER_WIDTH, bounds.height - BORDER_WIDTH);
				vSizingRect = new Rectangle(BORDER_WIDTH, bounds.height - BORDER_WIDTH,
						bounds.width - BORDER_WIDTH, BORDER_WIDTH);
				cornerRect = new Rectangle(0, bounds.height - BORDER_WIDTH, BORDER_WIDTH,
						BORDER_WIDTH);
				clientRect = new Rectangle(BORDER_WIDTH, 0, bounds.width - BORDER_WIDTH,
						bounds.height - BORDER_WIDTH);
			} else {
				hSizingRect = new Rectangle(bounds.width - BORDER_WIDTH, 0, BORDER_WIDTH,
						bounds.height - BORDER_WIDTH);
				vSizingRect = new Rectangle(0, bounds.height - BORDER_WIDTH, bounds.width
						- BORDER_WIDTH, BORDER_WIDTH);
				cornerRect = new Rectangle(bounds.width - BORDER_WIDTH, bounds.height
						- BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
				clientRect = new Rectangle(0, 0, bounds.width - BORDER_WIDTH, bounds.height
						- BORDER_WIDTH);
			}
		} else if (isFixed(SWT.BOTTOM)) {
			if (isFixed(SWT.RIGHT)) {
				hSizingRect = new Rectangle(0, BORDER_WIDTH, BORDER_WIDTH, bounds.height
						- BORDER_WIDTH);
				vSizingRect = new Rectangle(BORDER_WIDTH, 0, bounds.width - BORDER_WIDTH,
						BORDER_WIDTH);
				cornerRect = new Rectangle(0, 0, BORDER_WIDTH, BORDER_WIDTH);
				clientRect = new Rectangle(BORDER_WIDTH, BORDER_WIDTH, bounds.width - BORDER_WIDTH,
						bounds.height - BORDER_WIDTH);
			} else {
				hSizingRect = new Rectangle(bounds.width - BORDER_WIDTH, BORDER_WIDTH,
						BORDER_WIDTH, bounds.height - BORDER_WIDTH);
				vSizingRect = new Rectangle(0, 0, bounds.width - BORDER_WIDTH, BORDER_WIDTH);
				cornerRect = new Rectangle(bounds.width - BORDER_WIDTH, 0, BORDER_WIDTH,
						BORDER_WIDTH);
				clientRect = new Rectangle(0, BORDER_WIDTH, bounds.width - BORDER_WIDTH,
						bounds.height - BORDER_WIDTH);
			}
		}
		Control child = composite.getChildren()[0];
		child.setBounds(clientRect);
	}

	private void installResize(final Composite composite) {
		if (resizeInstalled)
			return;

		composite.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				Point p = e.display.getCursorLocation();
				if (trackState == NOT_SIZING) {
					setCursor(composite, new Point(e.x, e.y));
				} else if (trackState == HORIZONTAL_SIZING) {
					dragHorizontal(composite, p);
				} else if (trackState == VERTICAL_SIZING) {
					dragVertical(composite, p);
				} else if (trackState == CORNER_SIZING) {
					dragCorner(composite, p);
				}
			}
		});

		composite.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				composite.setCapture(false);

				// Persist the current size
				Point size = composite.getSize();
				toolControl.getPersistedState()
						.put(TrimStack.STATE_XSIZE, Integer.toString(size.x));
				toolControl.getPersistedState()
						.put(TrimStack.STATE_YSIZE, Integer.toString(size.y));

				trackState = NOT_SIZING;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				Point p = new Point(e.x, e.y);
				if (hSizingRect.contains(p)) {
					curPos = e.display.getCursorLocation();
					trackState = HORIZONTAL_SIZING;
					composite.setCapture(true);
				} else if (vSizingRect.contains(p)) {
					curPos = e.display.getCursorLocation();
					trackState = VERTICAL_SIZING;
					composite.setCapture(true);
				} else if (cornerRect.contains(p)) {
					curPos = e.display.getCursorLocation();
					trackState = CORNER_SIZING;
					composite.setCapture(true);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		composite.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
			}

			@Override
			public void mouseExit(MouseEvent e) {
				Composite comp = (Composite) e.widget;
				comp.setCursor(null);
			}

			@Override
			public void mouseEnter(MouseEvent e) {
			}
		});

		resizeInstalled = true;
	}

	/**
	 * @param p
	 */
	protected void setCursor(Composite composite, Point p) {
		if (hSizingRect.contains(p)) {
			composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
		} else if (vSizingRect.contains(p)) {
			composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
		} else if (cornerRect.contains(p)) {
			if (isFixed(SWT.TOP)) {
				if (isFixed(SWT.RIGHT))
					composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZESW));
				else
					composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZESE));
			} else if (isFixed(SWT.BOTTOM)) {
				if (isFixed(SWT.RIGHT))
					composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZESE));
				else
					composite.setCursor(composite.getDisplay().getSystemCursor(SWT.CURSOR_SIZESW));
			}
		} else {
			composite.setCursor(null);
		}
	}

	protected void dragCorner(Composite composite, Point p) {
		int dx = p.x - curPos.x;
		int dy = p.y - curPos.y;
		Rectangle bounds = composite.getBounds();

		if (isFixed(SWT.RIGHT)) {
			bounds.x += dx;
			bounds.width -= dx;
		} else {
			bounds.width += dx;
		}

		if (isFixed(SWT.BOTTOM)) {
			bounds.y += dy;
			bounds.height -= dy;
		} else {
			bounds.height += dy;
		}

		composite.setBounds(bounds);
		composite.getDisplay().update();

		curPos = p;
	}

	protected void dragVertical(Composite composite, Point p) {
		int dy = p.y - curPos.y;
		Rectangle bounds = composite.getBounds();
		if (isFixed(SWT.BOTTOM)) {
			bounds.y += dy;
			bounds.height -= dy;
		} else {
			bounds.height += dy;
		}

		composite.setBounds(bounds);
		composite.getDisplay().update();

		curPos = p;
	}

	protected void dragHorizontal(Composite composite, Point p) {
		int dx = p.x - curPos.x;
		Rectangle bounds = composite.getBounds();
		if (isFixed(SWT.RIGHT)) {
			bounds.x += dx;
			bounds.width -= dx;
		} else {
			bounds.width += dx;
		}

		composite.setBounds(bounds);
		composite.getDisplay().update();

		curPos = p;
	}

	private boolean isFixed(int swtSide) {
		return (fixedCorner & swtSide) != 0;
	}
}
