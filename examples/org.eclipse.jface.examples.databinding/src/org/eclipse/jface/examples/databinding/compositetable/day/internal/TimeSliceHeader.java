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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

/**
 * Represents a time slice that is the same time but may span several days.
 * For example: 11:00 - 11:15 PM from Sunday through Saturday.
 * 
 * @since 3.2
 */
public class TimeSliceHeader extends Composite {

	private final Image allDayImage = new Image(Display.getCurrent(), TimeSliceHeader.class.getResourceAsStream("clock.png"));
	
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
			children[0]
					.setBounds(0, 0, preferredWidth.intValue(), parentSize.y);

			// layout the rest of the controls
			int controlWidth = (parentSize.x - preferredWidth.intValue())
					/ (children.length - 1);
			int extraWidth = (parentSize.x - preferredWidth.intValue())
					% (children.length - 1);
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

	private LinkedList days = new LinkedList();

	private CLabel timeLabel = null;

	/**
	 * @param parent
	 * @param style
	 */
	public TimeSliceHeader(Composite parent, int style) {
		super(parent, style);
		initialize();
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
		setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		days.addLast(new TimeSlot(this, SWT.NONE));
		setSize(new Point(537, 16));
		setLayout(new TimeSliceAcrossTimeLayout());
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
	 * 
	 * Calling this method more than once results in undefined behavior.
	 * 
	 * @param numberOfColumns
	 *            The number of days to display.
	 */
	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		for (int i = numberOfColumns - 1; i > 0; --i) {
			days.add(new TimeSlot(this, SWT.NONE));
		}
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
			return;
		}
		timeLabel.setImage(null);
		
		this.currentTime = currentTime;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentTime);

		// Only the hours will display in the label
		if (calendar.get(Calendar.MINUTE) == 0) {
			DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
			String time = df.format(currentTime);
			timeLabel.setText(time);
			setHourStartOnDays(true);
		} else {
			timeLabel.setText("");
			setHourStartOnDays(false);
		}
	}

	private void setHourStartOnDays(boolean isHourStart) {
		for (Iterator daysIter = days.iterator(); daysIter.hasNext();) {
			TimeSlot day = (TimeSlot) daysIter.next();
			day.setHourStart(isHourStart);
		}
	}

} // @jve:decl-index=0:visual-constraint="10,10"

