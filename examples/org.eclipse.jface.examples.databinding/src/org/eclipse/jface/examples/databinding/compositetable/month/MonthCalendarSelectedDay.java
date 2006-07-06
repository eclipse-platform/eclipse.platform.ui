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

package org.eclipse.jface.examples.databinding.compositetable.month;

import java.util.Date;

import org.eclipse.swt.graphics.Point;

/**
 * Represents the currrently-selected day in the month calendar.
 * 
 * @since 3.3
 */
public class MonthCalendarSelectedDay {

	/**
	 * The selected Date
	 */
	public final Date date;
	
	/**
	 * The coordinates (day, week) of the selected date in the current month
	 * where day is a number 0-6 representing [Sunday .. Saturday] and week
	 * is a 0-based offset representing the the week number of the selected
	 * date's week within the month.
	 */
	public final Point coordinates;

	/**
	 * Constructor MonthCalendarSelection.  Construct a MonthCalendarSelection.
	 * 
	 * @param date The selected date.
	 * @param coordinates The coordinates of the selected date.
	 */
	public MonthCalendarSelectedDay(Date date, Point coordinates) {
		this.date = date;
		this.coordinates = coordinates;
	}

}
