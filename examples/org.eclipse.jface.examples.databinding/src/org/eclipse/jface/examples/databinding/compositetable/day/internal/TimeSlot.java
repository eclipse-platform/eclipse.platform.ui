/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Represents a particular range in time in a single day.
 * 
 * @since 3.2
 */
public class TimeSlot extends Canvas {

	private static final int FOCUS_LINE_WIDTH = 2;

	private boolean focusControl = false;

	private final Color WHITE;
	private final Color BLACK;
	private final Color CELL_BACKGROUND;
	private final Color CELL_BORDER_EMPHASIZED;
	private final Color CELL_BORDER_LIGHT;

	private final int TIME_BAR_WIDTH = 6;

	/**
	 * Constructor EmptyTablePlaceholder. Construct an EmptyTablePlaceholder
	 * control.
	 * 
	 * @param parent
	 *            The parent control
	 * @param style
	 *            Style bits. These are the same as what Canvas accepts.
	 */
	public TimeSlot(Composite parent, int style) {
		super(parent, style);

		addTraverseListener(traverseListener);
		addFocusListener(focusListener);
		addPaintListener(paintListener);
		addDisposeListener(disposeListener);

		Display display = Display.getCurrent();

		WHITE = display.getSystemColor(SWT.COLOR_WHITE);
		BLACK = display.getSystemColor(SWT.COLOR_BLACK);

		// Bluish color scheme by default; change as necessary.
		CELL_BACKGROUND = new Color(display, 250, 250, 255);
		CELL_BORDER_EMPHASIZED = new Color(display, 100, 100, 255);
		CELL_BORDER_LIGHT = new Color(display, 200, 200, 255);

		setBackground(CELL_BACKGROUND);
	}

	/**
	 * Make sure we remove our listeners...
	 */
	private DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			removeTraverseListener(traverseListener);
			removeFocusListener(focusListener);
			removePaintListener(paintListener);
			removeDisposeListener(disposeListener);

			// Dispose colors here
			CELL_BACKGROUND.dispose();
			CELL_BORDER_EMPHASIZED.dispose();
			CELL_BORDER_LIGHT.dispose();
		}
	};

	private Point preferredSize = new Point(-1, -1);

	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (preferredSize.x == -1 || changed) {
			preferredSize.x = getSize().x;
			Display display = Display.getCurrent();
			GC gc = new GC(display);
			try {
				Font font = display.getSystemFont();
				gc.setFont(font);
				FontMetrics fm = gc.getFontMetrics();
				preferredSize.y = fm.getHeight();
			} finally {
				gc.dispose();
			}
		}
		return preferredSize;
	}

	/**
	 * Paint the control.
	 */
	private PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			Color oldForeground = gc.getForeground();
			Color oldBackground = gc.getBackground();
			Point controlSize = getSize();

			// Draw basic background here
			try {
				// Draw "time bar" on left side
				gc.setBackground(WHITE);
				gc.setForeground(WHITE);
				gc.fillRectangle(0, 0, TIME_BAR_WIDTH, controlSize.y);
				gc.setForeground(BLACK);
				gc.drawLine(TIME_BAR_WIDTH + 1, 0, TIME_BAR_WIDTH + 1,
						controlSize.y);
				gc.drawLine(controlSize.x - 1, 0, controlSize.x - 1,
						controlSize.y);
				if (hourStart) {
					gc.setForeground(CELL_BORDER_EMPHASIZED);
				} else {
					gc.setForeground(CELL_BORDER_LIGHT);
				}
				gc.drawLine(TIME_BAR_WIDTH + 2, 0, controlSize.x - 2, 0);
			} finally {
				gc.setBackground(oldBackground);
				gc.setForeground(oldForeground);
			}

			// Draw focus rubberband if we're focused
			int oldLineStyle = gc.getLineStyle();
			int oldLineWidth = gc.getLineWidth();
			try {
				if (focusControl) {
					gc.setLineStyle(SWT.LINE_DASH);
					gc.setLineWidth(FOCUS_LINE_WIDTH);
					Point parentSize = getSize();
					gc.drawRectangle(TIME_BAR_WIDTH + FOCUS_LINE_WIDTH,
									FOCUS_LINE_WIDTH, parentSize.x - TIME_BAR_WIDTH - 4,
									parentSize.y - 3);
				}

				gc.setForeground(CELL_BACKGROUND);
			} finally {
				gc.setForeground(oldForeground);
				gc.setLineStyle(oldLineStyle);
				gc.setLineWidth(oldLineWidth);
			}
		}
	};

	/**
	 * When we gain/lose focus, redraw ourselves appropriately
	 */
	private FocusListener focusListener = new FocusListener() {
		public void focusGained(FocusEvent e) {
			focusControl = true;
			redraw();
		}

		public void focusLost(FocusEvent e) {
			focusControl = false;
			redraw();
		}
	};

	/**
	 * Permit focus events via keyboard.
	 */
	private TraverseListener traverseListener = new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
		}
	};

	private boolean hourStart = true;

	/**
	 * @param isHourStart
	 */
	public void setHourStart(boolean isHourStart) {
		this.hourStart = isHourStart;
		redraw();
	}

	/**
	 * @return true if the current day represents the start of an hour; false
	 *         otherwise.
	 */
	public boolean isHourStart() {
		return hourStart;
	}
}
