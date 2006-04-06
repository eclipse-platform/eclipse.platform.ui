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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a bunch of stuff that is intended to be displayed in a calendar control
 * 
 * @since 3.2
 */
public class CalendarableModel {

	private int numberOfDays = -1;
	private int numberOfDivisionsInHour = -1;
	private ArrayList[] dateColumns = null;

	/**
	 * @param numberOfDays
	 * @param numberOfDivisionsInHour
	 */
	public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		if (numberOfDivisionsInHour < 1) {
			throw new IllegalArgumentException("There must be at least one division in the hour");
		}
		
		if (numberOfDays < 1) {
			throw new IllegalArgumentException("There must be at least one day in the editor");
		}

		this.numberOfDays = numberOfDays;
		this.numberOfDivisionsInHour = numberOfDivisionsInHour;
		initializeColumns(numberOfDays);
		
		refresh();
	}

	/**
	 * @return The number of days to display
	 */
	public int getNumberOfDays() {
		return numberOfDays;
	}

	private void initializeColumns(int numberOfDays) {
		dateColumns  = new ArrayList[numberOfDays]; 
		for (int i=0; i < numberOfDays; ++i) {
			dateColumns[i] = new ArrayList();
		}
	}

	private Date startDate = null;
	
	/**
	 * @param startDate The starting date to display
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
		refresh();
	}

	/**
	 * @return The starting date to display
	 */
	public Date getStartDate() {
		return startDate;
	}

	private EventCountProvider eventCountProvider = null;

	/**
	 * Sets a strategy pattern object that can return the number of events 
	 * to display on a particulr day.
	 * 
	 * @param eventCountProvider
	 */
	public void setDayEventCountProvider(EventCountProvider eventCountProvider) {
		this.eventCountProvider = eventCountProvider;
		refresh();
	}
	
	private EventContentProvider eventContentProvider = null;

	/**
	 * Sets a strategy pattern object that can set the data for the actual events for
	 * a particular day.
	 * 
	 * @param eventContentProvider
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		this.eventContentProvider = eventContentProvider;
		refresh();
	}
	
	/**
	 * Refresh everything in the display.
	 */
	private void refresh() {
		if(!isInitialized()) {
			return;
		}
		//refresh
		Date dateToRefresh = null;
		for (int i = 0; i < dateColumns.length; i++) {
			dateToRefresh = calculateDate(i);
			refresh(dateToRefresh, i);
		}
	}

	/**
	 * Returns the date that is the numberOfDaysFromStartDate.
	 * 
	 * @param numberOfDaysFromStartDate
	 * @return Date
	 */
	public Date calculateDate(int numberOfDaysFromStartDate) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(this.startDate);
		gc.roll(Calendar.DATE, numberOfDaysFromStartDate);
		return gc.getTime();
	}

	/**
	 * Has all data been set for a refresh.
	 * 
	 */
	private boolean isInitialized() {
		return 
			null != startDate &&
			numberOfDays > 0 &&
			numberOfDivisionsInHour > 0 &&
			null != eventContentProvider &&
			null != eventCountProvider;
	}
	
	private void refresh(Date date, int column) {
		int numberOfEventsInDay = eventCountProvider.getNumberOfEventsInDay(date);
		
		resizeList(dateColumns[column], numberOfEventsInDay);
		clearCalendarables(dateColumns[column]);
		
		Calendarable[] tempEvents = (Calendarable[]) dateColumns[column]
				.toArray(new Calendarable[numberOfEventsInDay]);

		eventContentProvider.refresh(
				date, 
				tempEvents);
	}

	private void resizeList(ArrayList list, int numberOfEventsInDay) {
		while (list.size() < numberOfEventsInDay) {
			list.add(new Calendarable());
		}
		while (list.size() > numberOfEventsInDay) {
			Calendarable c = (Calendarable) list.remove(0);
			c.dispose();
		}
	}

	private void clearCalendarables(ArrayList calendarables) {
		for (Iterator i = calendarables.iterator(); i.hasNext();) {
			Calendarable c = (Calendarable) i.next();
			c.reset();
		}
	}

	/**
	 * Refresh the display for the specified Date.  If Date isn't being
	 * displayed, this method ignores the request.
	 * 
	 * @param date the date to refresh.
	 */
	public void refresh(Date date) {
		GregorianCalendar dateToRefresh = new GregorianCalendar();
		dateToRefresh.setTime(date);
		for (int offset=0; offset < numberOfDays; ++offset) {
			Date refreshTarget = calculateDate(offset);
			GregorianCalendar target = new GregorianCalendar();
			target.setTime(refreshTarget);
			
			if (target.get(Calendar.DATE) == dateToRefresh.get(Calendar.DATE) &&
				target.get(Calendar.MONTH) == dateToRefresh.get(Calendar.MONTH) &&
				target.get(Calendar.YEAR) == dateToRefresh.get(Calendar.YEAR)) 
			{
				refresh(date, offset);
				break;
			}
		}
	}

	/**
	 * Return the events for a particular day offset.
	 * 
	 * @param dayOffset
	 * @return A List of events.
	 */
	public List getCalendarableEvents(int dayOffset) {
		return dateColumns[dayOffset];
	}

}
