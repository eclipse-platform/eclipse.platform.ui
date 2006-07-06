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
package org.eclipse.jface.examples.databinding.compositetable.month.internal;

import java.util.Date;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.ICalendarableItemControl;
import org.eclipse.jface.examples.databinding.compositetable.month.MonthCalendarableItemControl;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * An SWT custom control representing a single day in a month-mode calendar.
 */
public class Day extends Canvas implements PaintListener, DisposeListener {
	private final Color FOCUS_RUBBERBAND;
	private Color CURRENT_MONTH;
	private Color OTHER_MONTH;
	
	private static final int FOCUS_LINE_WIDTH = 2;
	private boolean focusControl = false;

	private static final int _SIZE_MULTIPLIER = 7;
	private Label dayNumber = null;
	private Label spacer = null;
	private Point textBounds;

	private Point monthPosition = null;
	
	/**
	 * @param parent
	 * @param style
	 */
	public Day(Composite parent, int style) {
		super(parent, style);
		
		Display display = Display.getCurrent();
		FOCUS_RUBBERBAND = new Color(display, lighten(saturate(display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB(), .85f), -.333f));
		CURRENT_MONTH = display.getSystemColor(SWT.COLOR_WHITE);
		OTHER_MONTH = new Color(display, new RGB(240, 240, 240));
		
		initialize();
		
		addTraverseListener(traverseListener);
		addKeyListener(keyListener);
		addMouseListener(mouseListener);
		spacer.addMouseListener(mouseListener);
		dayNumber.addMouseListener(mouseListener);
		addFocusListener(focusListener);
		addPaintListener(this);
		addDisposeListener(this);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		FOCUS_RUBBERBAND.dispose();
		OTHER_MONTH.dispose();
		removeTraverseListener(traverseListener);
		removeKeyListener(keyListener);
		removeMouseListener(mouseListener);
		spacer.removeMouseListener(mouseListener);
		dayNumber.removeMouseListener(mouseListener);
		removeFocusListener(focusListener);
		removePaintListener(this);
		removeDisposeListener(this);
	}

	private void initialize() {
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		spacer = new Label(this, SWT.NONE);
		spacer.setLayoutData(gridData);
		spacer.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 0;
		dayNumber = new Label(this, SWT.NONE);
		dayNumber.setFont(JFaceResources.getFontRegistry().get(
				JFaceResources.BANNER_FONT));
		dayNumber.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		dayNumber.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_SELECTION));
		dayNumber.setText("31");
		textBounds = dayNumber.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		this.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		this.setLayout(gridLayout);
		setSize(new org.eclipse.swt.graphics.Point(106, 101));
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = new Point(0, 0);
		size.x = textBounds.x * _SIZE_MULTIPLIER;
		size.y = textBounds.y * _SIZE_MULTIPLIER / 2;
		return size;
	}

	/**
	 * @return The (day, week) of this day in the month.
	 */
	public Point getMonthPosition() {
		return monthPosition;
	}
	
	/**
	 * @param monthPosition The (day, week) of this day in the month.
	 */
	public void setMonthPosition(Point monthPosition) {
		this.monthPosition = monthPosition;
	}
	
	/**
	 * @return The day's number
	 */
	public int getDayNumber() {
		return Integer.parseInt(dayNumber.getText());
	}

	/**
	 * @param dayNum the day number to set
	 */
	public void setDayNumber(int dayNum) {
		dayNumber.setText(Integer.toString(dayNum));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		
		// Save stuff we're about to change so we can restore it later
		int oldLineStyle = gc.getLineStyle();
		int oldLineWidth = gc.getLineWidth();
		
		// Draw focus rubberband if we're focused
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
		} finally {
			gc.setLineStyle(oldLineStyle);
			gc.setLineWidth(oldLineWidth);
		}
	}

	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			setFocus();
		}
	};

	private KeyListener keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ARROW_LEFT:
				if (monthPosition.x > 0) {
					traverse(SWT.TRAVERSE_TAB_PREVIOUS);
				}
				return;
			case SWT.ARROW_RIGHT:
				if (monthPosition.x < 6) {
					traverse(SWT.TRAVERSE_TAB_NEXT);
				}
				return;
			case SWT.TAB:
				if ((e.stateMask & SWT.SHIFT) != 0) {
					traverse(SWT.TRAVERSE_TAB_PREVIOUS);
					return;
				}
				traverse(SWT.TRAVERSE_TAB_NEXT);
				return;
			}
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
	
	private void setBackgroundTakingIntoAccountIfWeAreInTheCurrentMonth(Control control) {
		if (inCurrentMonth) {
			control.setBackground(CURRENT_MONTH);
		} else {
			control.setBackground(OTHER_MONTH);
		}
	}
	
	private boolean inCurrentMonth = false;
	
	/**
	 * @param inCurrentMonth
	 */
	public void setInCurrentMonth(boolean inCurrentMonth) {
		this.inCurrentMonth = inCurrentMonth;
		setBackgroundTakingIntoAccountIfWeAreInTheCurrentMonth(this);
		setBackgroundTakingIntoAccountIfWeAreInTheCurrentMonth(spacer);
		setBackgroundTakingIntoAccountIfWeAreInTheCurrentMonth(dayNumber);
	}
	
	private CalendarableItem[] controls = null;

	/**
	 * @param controls
	 */
	public void setItems(CalendarableItem[] controls) {
		if (this.controls != null) {
			for (int i = 0; i < this.controls.length; i++) {
				ICalendarableItemControl control = this.controls[i].getControl();
				control.removeMouseListener(mouseListener);
				control.dispose();
			}
		}
		this.controls = controls;
		for (int i = 0; i < this.controls.length; i++) {
			MonthCalendarableItemControl control = new MonthCalendarableItemControl(this, SWT.NULL);
			setBackgroundTakingIntoAccountIfWeAreInTheCurrentMonth(control);
			control.setText(this.controls[i].getText());
			Image image = this.controls[i].getImage();
			if (image != null) {
				control.setImage(image);
			}
			control.setToolTipText(this.controls[i].getToolTipText());
			control.addMouseListener(mouseListener);
			GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.horizontalSpan=2;
			control.setLayoutData(gd);
			this.controls[i].setControl(control);
		}
	}

	private Date date;
	
	/**
	 * Sets the Date represented by this Day.
	 * 
	 * @param date The date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * Returns the Date represented by this Day.
	 * 
	 * @return This Day's date
	 */
	public Date getDate() {
		return date;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
