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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

/**
 * Represents a time slice that is the same time but may span several days.
 * For example: 11:00 - 11:15 PM from Sunday through Saturday.
 * 
 * @since 3.2
 */
public class TimeSlice extends Composite {

	private final Image allDayImage = new Image(Display.getCurrent(), TimeSlice.class.getResourceAsStream("clock.png"));
	
	/**
	 * The 0th control in the layout may have a java.lang.Integer LayoutData
	 * indicating its preferred width. Otherwise, DaysLayout will ask the
	 * control to compute its preferred size and will use the width returned by
	 * that computation. All other controls will be equally allotted horizontal
	 * width in the parent control.
	 */
	private static class TimeSliceAcrossTimeLayout extends Layout {
		Point preferredSize = new Point(-1, -1);

		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (preferredSize.x == -1 || flushCache) {
				preferredSize.x = wHint;
				preferredSize.y = -1; // NOTE: This assumes at least one child
										// control
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					preferredSize.y = Math.max(preferredSize.y, child
							.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
				}
			}
			return preferredSize;
		}

		protected void layout(Composite composite, boolean flushCache) {
			Point parentSize = composite.getSize();
			Control[] children = composite.getChildren();

			// layout 0th control
			Integer preferredWidth = (Integer) children[0].getLayoutData();
			if (preferredWidth == null) {
				preferredWidth = new Integer(children[0].computeSize(
						SWT.DEFAULT, SWT.DEFAULT).x);
			}
			children[0].setBounds(0, 0, preferredWidth.intValue(), parentSize.y);

			// layout the rest of the controls
			int controlWidth = 0;
			int extraWidth = 0;
			if (children.length >= 2) {
				controlWidth = (parentSize.x - preferredWidth.intValue())
						/ (children.length - 1);
				extraWidth = (parentSize.x - preferredWidth.intValue())
						% (children.length - 1);
			}
			int leftPosition = preferredWidth.intValue();

			for (int i = 1; i < children.length; i++) {
				Control control = children[i];
				int width = controlWidth;
				if (extraWidth > 0) {
					++width;
					--extraWidth;
				}
				control.setBounds(leftPosition, 0, width, parentSize.y);
				leftPosition += width;
			}
		}
	}

	private CLabel timeLabel = null;

	private LinkedList columns = new LinkedList();

	/**
	 * @return Returns the columns.
	 */
	public LinkedList getColumns() {
		return columns;
	}
	
	/**
	 * Returns the control that implements the specified column.
	 * 
	 * @param column The column number.
	 * @return Control the SWT control that implements this column.
	 */
	public Control getColumnControl(int column) {
		return (Control) columns.get(column);
	}
	
	/**
	 * Return the column number of the specified widget.
	 * 
	 * @param widget the TimeSlot widget
	 * @return the column number of the specified TimeSlot within this TimeSlice.
	 */
	public int getControlColumn(Widget widget) {
		int columnNumber = 0;
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			if (columnsIter.next() == widget) {
				return columnNumber;
			}
			++columnNumber;
		}
		throw new IllegalArgumentException("Unrecognized widget passed to getControlColumn");
	}

	/**
	 * Constructor TimeSlice. Construct a TimeSlice control, passing the parent
	 * and style bits.
	 * 
	 * @param parent
	 *            The SWT parent object.
	 * @param style
	 *            The set of style bits this control accepts. Currently SWT.NONE.
	 */
	public TimeSlice(Composite parent, int style) {
		super(parent, SWT.NULL);
		initialize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
	 */
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			TimeSlot cell = (TimeSlot) columnsIter.next();
			cell.setMenu(menu);
		}
	}

	/**
	 * Initialize the control
	 */
	private void initialize() {
		timeLabel = new CLabel(this, SWT.RIGHT);
		timeLabel.setText("23:00 PM");
		Integer preferredWidth = new Integer(timeLabel.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, false).x + 5);
		timeLabel.setLayoutData(preferredWidth);
		timeLabel.setText("");
		setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		setSize(new Point(537, 16));
		setLayout(new TimeSliceAcrossTimeLayout());
	}

	private boolean headerControl = false;

	/**
	 * @return Returns the headerControl.
	 */
	public boolean isHeaderControl() {
		return headerControl;
	}

	/**
	 * @param headerControl The headerControl to set.
	 */
	public void setHeaderControl(boolean headerControl) {
		this.headerControl = headerControl;
	}

	private int numberOfColumns = 1;

	/**
	 * Gets the number of columns that will be displayed in this row. The
	 * default number of columns is 1.
	 * 
	 * @return numberOfColumns The number of days to display.
	 */
	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	/**
	 * Sets the number of columns that will be displayed in this row. The
	 * default number of columns is 1. This method may only be called *once* at
	 * the beginning of the control's life cycle, and the value passed must be
	 * >1.
	 * <p>
	 * Calling this method more than once results in undefined behavior.
	 * 
	 * @param numberOfColumns
	 *            The number of days to display.
	 */
	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		Control[] tabStops = new Control[numberOfColumns];
		for (int i = numberOfColumns; i > 0; --i) {
			if (headerControl) {
				CLabel control = new CLabel(this, SWT.SHADOW_OUT | SWT.BORDER | SWT.CENTER);
				tabStops[numberOfColumns-i] = control;
				columns.add(control);
			} else {
				TimeSlot control = new TimeSlot(this, SWT.NONE);
				tabStops[numberOfColumns-i] = control;
				columns.add(control);
			}
		}
		setTabList(tabStops);
	}

	private Date currentTime = new Date();

	/**
	 * @return The current time set in this "days" row.
	 */
	public Date getCurrentTime() {
		return currentTime;
	}

	/**
	 * @param currentTime
	 */
	public void setCurrentTime(Date currentTime) {
		// if currentTime is null, we are becoming an all-day event row
		if (currentTime == null) {
			timeLabel.setImage(allDayImage);
			timeLabel.setText("");
			setAllDayEventOnDays(true);
			return;
		}
		
		setAllDayEventOnDays(false);
		timeLabel.setImage(null);
		
		setTimeOnDays(currentTime);
		
		this.currentTime = currentTime;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentTime);

		// Only the hours will display in the label
		if (calendar.get(Calendar.MINUTE) == 0) {
			DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
			String time = df.format(currentTime);
			timeLabel.setText(time);
		} else {
			timeLabel.setText("");
		}
	}
	
	private void setTimeOnDays(Date currentTime) {
		for (Iterator daysIter = columns.iterator(); daysIter.hasNext();) {
			Object dayCandidate = daysIter.next();
			if (dayCandidate instanceof TimeSlot) {
				TimeSlot day = (TimeSlot) dayCandidate;
				day.setTime(currentTime);
			}
		}
	}

	private void setAllDayEventOnDays(boolean isAllDayEvent) {
		for (Iterator daysIter = columns.iterator(); daysIter.hasNext();) {
			Object dayCandidate = daysIter.next();
			if (dayCandidate instanceof TimeSlot) {
				TimeSlot day = (TimeSlot) dayCandidate;
				day.setAllDay(isAllDayEvent);
			}
		}
	}

	/**
	 * @see org.eclipse.swt.widgets.Control#addFocusListener
	 * 
	 * @param listener
	 */
	public void addCellFocusListener(FocusListener listener) {
		for (Iterator daysIter = columns.iterator(); daysIter.hasNext();) {
			Object dayCandidate = daysIter.next();
			if (dayCandidate instanceof TimeSlot) {
				TimeSlot day = (TimeSlot) dayCandidate;
				day.addFocusListener(listener);
			}
		}
	}
	
	/**
	 * @see org.eclipse.swt.widgets.Control#removeFocusListener
	 * @param listener
	 */
	public void removeCellFocusListener(FocusListener listener) {
		for (Iterator daysIter = columns.iterator(); daysIter.hasNext();) {
			Object dayCandidate = daysIter.next();
			if (dayCandidate instanceof TimeSlot) {
				TimeSlot day = (TimeSlot) dayCandidate;
				day.removeFocusListener(listener);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			TimeSlot cell = (TimeSlot) columnsIter.next();
			cell.addKeyListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener listener) {
		super.removeKeyListener(listener);
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			TimeSlot cell = (TimeSlot) columnsIter.next();
			cell.removeKeyListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addMouseListener(MouseListener listener) {
		super.addMouseListener(listener);
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			TimeSlot cell = (TimeSlot) columnsIter.next();
			cell.addMouseListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeMouseListener(MouseListener listener) {
		super.removeMouseListener(listener);
		for (Iterator columnsIter = columns.iterator(); columnsIter.hasNext();) {
			TimeSlot cell = (TimeSlot) columnsIter.next();
			cell.removeMouseListener(listener);
		}
	}
	
} // @jve:decl-index=0:visual-constraint="10,10"

