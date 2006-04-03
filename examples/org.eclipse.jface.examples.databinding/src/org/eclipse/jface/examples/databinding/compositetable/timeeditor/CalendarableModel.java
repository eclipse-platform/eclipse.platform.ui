/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * Represents a bunch of stuff that is intended to be displayed in a calendar control
 * 
 * @since 3.2
 */
public class CalendarableModel {

	private int numberOfDays = -1;
	private int numberOfDivisionsInHour = -1;
	private ArrayList[] dateColumns = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setTimeBreakdown(int, int)
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
	 * @return
	 */
	public int getNumberOfDays() {
		return numberOfDays;
	}

	/**
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param numberOfDays
	 */
	private void initializeColumns(int numberOfDays) {
		dateColumns  = new ArrayList[numberOfDays]; 
		for (int i=0; i < numberOfDays; ++i) {
			dateColumns[i] = new ArrayList();
		}
	}

	private EventCountProvider eventCountProvider = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setDayEventCountProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider)
	 */
	public void setDayEventCountProvider(EventCountProvider eventCountProvider) {
		this.eventCountProvider = eventCountProvider;
		refresh();
	}
	
	private EventContentProvider eventContentProvider = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventContentProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider)
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		this.eventContentProvider = eventContentProvider;
		refresh();
	}
	
	private Date startDate = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setStartDate(java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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
			dateToRefresh = calculateDate(i+1);
			refresh(dateToRefresh, i);
		}
	}

	/**
	 * Returns the date that is the numberOfDaysFromStartDate.
	 * 
	 * @param numberOfDaysFromStartDate
	 * @return
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
		
		ICalendarable[] tempEvents = (ICalendarable[]) dateColumns[column]
				.toArray(new ICalendarable[numberOfEventsInDay]);

		eventContentProvider.refresh(
				date, 
				tempEvents);
	}

	/**
	 * @param list
	 * @param numberOfEventsInDay
	 */
	private void resizeList(ArrayList list, int numberOfEventsInDay) {
		while (list.size() < numberOfEventsInDay) {
			list.add(new Calendarable());
		}
		while (list.size() > numberOfEventsInDay) {
			Calendarable c = (Calendarable) list.remove(0);
			c.dispose();
		}
	}

	/**
	 * @param calendarables
	 */
	private void clearCalendarables(ArrayList calendarables) {
		for (Iterator i = calendarables.iterator(); i.hasNext();) {
			ICalendarable c = (ICalendarable) i.next();
			c.reset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh(java.util.Date)
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
	 * @return
	 */
	public ArrayList getCalendarableEvents(int dayOffset) {
		return dateColumns[dayOffset];
	}

}
