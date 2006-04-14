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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the model behind the calendar control.  This model manages three
 * concerns:
 *   1) Setting/maintaining the visible range of days (startDate, numberOfDays)
 *   2) Keeping the events for a particular day within the range of visible days
 *   3) Keeping track of the number of columns required to display the events
 *      in a given day from the set of visible days.
 * 
 * @since 3.2
 */
public class CalendarableModel {

	private static final int DEFAULT_START_HOUR = 8;
	
	private int numberOfDays = -1;
	private int numberOfDivisionsInHour = -1;
	private ArrayList[] dayColumns = null;
	private Integer[] columnsWithinDay = null;

	private int defaultStartHour = DEFAULT_START_HOUR;
	
	/**
	 * @param dayOffset
	 * @return
	 */
	public Integer getNumberOfColumnsWithinDay(int dayOffset) {
		return columnsWithinDay[dayOffset];
	}
	
	/**
	 * @param dayOffset
	 * @param numberOfColumns
	 */
	public void setNumberOfColumnsWithinDay(int dayOffset, int numberOfColumns) {
		columnsWithinDay[dayOffset] = new Integer(numberOfColumns);
	}
	
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
		initializeDayArrays(numberOfDays);
		
		refresh();
	}
	
	private void initializeDayArrays(int numberOfDays) {
		dayColumns  = new ArrayList[numberOfDays]; 
		for (int i=0; i < numberOfDays; ++i) {
			dayColumns[i] = new ArrayList();
		}
		columnsWithinDay = new Integer[numberOfDays];
	}
	
	/**
	 * @return The number of days to display
	 */
	public int getNumberOfDays() {
		return numberOfDays;
	}
	
	/**
	 * @return Returns the numberOfDivisionsInHour.
	 */
	public int getNumberOfDivisionsInHour() {
		return numberOfDivisionsInHour;
	}

	private Date startDate = null;
	
	/**
	 * @param startDate The starting date to display
	 * @return The obsolete Calendarable objects
	 */
	public List setStartDate(Date startDate) {
		this.startDate = startDate;
		columnsWithinDay = new Integer[numberOfDays];// FIXME: This currently refreshes everything, even data we already have
		return refresh();
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
	private List refresh() {
		LinkedList result = new LinkedList();
		if(!isInitialized()) {
			return result;
		}
		//refresh
		Date dateToRefresh = null;
		for (int i = 0; i < dayColumns.length; i++) {
			dateToRefresh = calculateDate(i);
			refresh(dateToRefresh, i, result);
		}
		return result;
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
	
	private void refresh(Date date, int column, List invalidatedElements) {
		int numberOfEventsInDay = eventCountProvider.getNumberOfEventsInDay(date);

		while (dayColumns[column].size() > 0) {
			invalidatedElements.add(dayColumns[column].remove(0));
		}
		resizeList(dayColumns[column], numberOfEventsInDay);
		
		Calendarable[] tempEvents = (Calendarable[]) dayColumns[column]
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
			list.remove(0);
		}
	}

	/**
	 * Refresh the display for the specified Date.  If Date isn't being
	 * displayed, this method ignores the request.
	 * 
	 * @param date the date to refresh.
	 * @return List any Calendarables that were invalidated
	 */
	public List refresh(Date date) {
		LinkedList invalidatedCalendarables = new LinkedList();
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
				refresh(date, offset, invalidatedCalendarables);
				break;
			}
		}
		return invalidatedCalendarables;
	}

	/**
	 * Return the events for a particular day offset.
	 * 
	 * @param dayOffset
	 * @return A List of events.
	 */
	public List getCalendarableEvents(int dayOffset) {
		return dayColumns[dayOffset];
	}

	/**
	 * Method computeNumberOfAllDayEventRows. 
	 * 
	 * @return int representing the max number of events in all visible days. 
	 */
	public int computeNumberOfAllDayEventRows() {
		int maxAllDayEvents = 0;
		for (int day = 0; day < dayColumns.length; day++) {
			ArrayList calendarables = dayColumns[day];
			int allDayEventsInCurrentDay = 0;
			for (Iterator iter = calendarables.iterator(); iter.hasNext();) {
				Calendarable event = (Calendarable) iter.next();
				if (event.isAllDayEvent()) {
					allDayEventsInCurrentDay++;
				}
			}
			if (allDayEventsInCurrentDay > maxAllDayEvents) {
				maxAllDayEvents = allDayEventsInCurrentDay;
			}
		}
		return maxAllDayEvents;
	}

	/**
	 * Method computeStartHour.  Computes the start hour of the day for all
	 * days that are displayed.  If no events are before the defaultStartHour,
	 * the defaultStartHour is returned.  If any day in the model has an event
	 * beginning before defaultStartHour, the hour of the earliest event is
	 * used instead.
	 * 
	 * @return int The start hour.
	 */
	public int computeStartHour() {
		GregorianCalendar gc = new GregorianCalendar();
		
		int startHour = getDefaultStartHour();
		for (int day = 0; day < dayColumns.length; day++) {
			ArrayList calendarables = dayColumns[day];
			for (Iterator iter = calendarables.iterator(); iter.hasNext();) {
				Calendarable event = (Calendarable) iter.next();
				if (event.isAllDayEvent()) {
					continue;
				}
				gc.setTime(event.getStartTime());
				int eventStartHour = gc.get(Calendar.HOUR_OF_DAY);
				if (eventStartHour < startHour) {
					startHour = eventStartHour;
				}
			}
		}
		return startHour;
	}

	/**
	 * Method setDefaultStartHour.
	 * 
	 * @param defaultStartHour The first hour to be displayed by default.
	 */
	public void setDefaultStartHour(int defaultStartHour) {
		this.defaultStartHour = defaultStartHour;
	}

	/**
	 * Method getDefaultStartHour
	 * 
	 * @return int representing the first hour to be displayed by default.
	 */
	public int getDefaultStartHour() {
		return defaultStartHour;
	}

}
