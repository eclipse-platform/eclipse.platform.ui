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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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
	private final Color CELL_BACKGROUND_LIGHT;
	private final Color CELL_BACKGROUND_WHITE;
	private final Color CELL_BORDER_EMPHASIZED;
	private final Color CELL_BORDER_LIGHT;
	private final Color TIME_BAR_COLOR;
	private final Color FOCUS_RUBBERBAND;

	/**
	 * Width of the bar between events
	 */
	public static final int TIME_BAR_WIDTH = 3;

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
		addKeyListener(keyListener);
		addMouseListener(mouseListener);

		Display display = Display.getCurrent();

		WHITE = display.getSystemColor(SWT.COLOR_WHITE);

		CELL_BACKGROUND_WHITE = display.getSystemColor(SWT.COLOR_WHITE);
		CELL_BORDER_EMPHASIZED = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		CELL_BACKGROUND_LIGHT = new Color(display, new RGB(248, 248, 248));
		CELL_BORDER_LIGHT = new Color(display, saturate(CELL_BORDER_EMPHASIZED.getRGB(), .2f));
		TIME_BAR_COLOR = new Color(display, saturate(CELL_BORDER_EMPHASIZED.getRGB(), .1f));
		FOCUS_RUBBERBAND = new Color(display, lighten(saturate(display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB(), .85f), -.333f));

		setBackground(CELL_BACKGROUND_LIGHT);
	}

	/**
	 * Sets the color's saturation to the specified value.
	 * 
	 * @param color The RGB of the color
	 * @param saturation the new saturation (between 0 and 1)
	 * @return a Color that is saturated by the specified amount
	 */
	private RGB saturate(RGB color, float saturation) {
		float[] hsb = color.getHSB();
		return new RGB(hsb[0], saturation, hsb[2]);
	}
	
	/**
	 * @param color The RGB of the color
	 * @param amount The amount to lighten as a percentage expresssed as a float between -1 and 1.
	 * @return The new RGB that is lightened by the specified amount
	 */
	private RGB lighten(RGB color, float amount) {
		float[] hsb = color.getHSB();
		float b = hsb[2] + hsb[2] * amount;
		if (b < 0) b=0;
		if (b > 1) b=1;
		return new RGB(hsb[0], hsb[1], b);
	}
	

	/**
	 * Make sure we remove our listeners...
	 */
	private DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			removeTraverseListener(traverseListener);
			removeFocusListener(focusListener);
			removePaintListener(paintListener);
			removeMouseListener(mouseListener);
			removeKeyListener(keyListener);
			removeDisposeListener(disposeListener);

			// Dispose colors here
			CELL_BACKGROUND_LIGHT.dispose();
			CELL_BORDER_LIGHT.dispose();
			TIME_BAR_COLOR.dispose();
			FOCUS_RUBBERBAND.dispose();
		}
	};
	
	private KeyListener keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ARROW_LEFT:
				traverse(SWT.TRAVERSE_TAB_PREVIOUS);
				return;
			case SWT.ARROW_RIGHT:
				traverse(SWT.TRAVERSE_TAB_NEXT);
				return;
			}
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
				gc.setForeground(CELL_BORDER_LIGHT);
				int lineStyle = gc.getLineStyle();
				gc.setLineStyle(SWT.LINE_DOT);
				gc.drawLine(TIME_BAR_WIDTH + 1, 0, TIME_BAR_WIDTH + 1,
						controlSize.y);
				gc.setLineStyle(lineStyle);
				gc.setForeground(TIME_BAR_COLOR);
				gc.drawLine(controlSize.x - 1, 0, controlSize.x - 1,
						controlSize.y);
				if (isMinutesAfterHour(0)) {
					gc.setForeground(CELL_BORDER_EMPHASIZED);
				} else {
					gc.setForeground(CELL_BORDER_LIGHT);
				}
//				gc.drawLine(TIME_BAR_WIDTH + 2, 0, controlSize.x - 2, 0);
				if (isMinutesAfterHour(0) || isMinutesAfterHour(30) && !isAllDay()) {
					gc.drawLine(0, 0, controlSize.x, 0);
				}
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
					gc.setForeground(FOCUS_RUBBERBAND);
					Point parentSize = getSize();
					gc.drawRectangle(FOCUS_LINE_WIDTH,
							FOCUS_LINE_WIDTH, parentSize.x - 4,
							parentSize.y - 3);
				}

				gc.setForeground(CELL_BACKGROUND_LIGHT);
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
			// NOOP: this just lets us receive focus from SWT
		}
	};
	
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			setFocus();
		}
	};

	/**
	 * @param minute The minute to check
	 *  
	 * @return true if the time falls on the specified minute of the hour.
	 * false otherwise.
	 */
	public boolean isMinutesAfterHour(int minute) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(time);
		return calendar.get(Calendar.MINUTE) == minute;
	}
	
	private boolean allDay = false;
	
	/**
	 * @param isAllDayEvent
	 */
	public void setAllDay(boolean isAllDayEvent) {
		this.allDay = isAllDayEvent;
		if (isAllDayEvent) {
			setBackground(CELL_BACKGROUND_WHITE);
		} else {
			setBackground(CELL_BACKGROUND_LIGHT);
		}
	}
	
	/**
	 * @return Returns the allDay.
	 */
	public boolean isAllDay() {
		return allDay;
	}
	
	private Date time = new Date();

	/**
	 * @param currentTime
	 */
	public void setTime(Date currentTime) {
		this.time = currentTime;
		redraw();
	}
}
