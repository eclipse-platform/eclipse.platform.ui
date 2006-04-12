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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Maintains the set of DayLayout objects that are valid given a specified 
 * startDate and numberOfDays window.  When the startDate is changed, all
 * DayLayout objects that no longer fall within the window specified by the
 * startDate and numberOfDays are removed.
 * 
 * @since 3.2
 */
public class DayLayoutsByDate {
	private Date startDate;
	private int numberOfDays;
	private Map dayLayoutsByDate = new HashMap();

	/**
	 * Construct a DayLayoutsByDate object.
	 * 
	 * @param startDate The initial startDate that defines the window beginning
	 * @param numberOfDays The number of days in the window.
	 */
	public DayLayoutsByDate(Date startDate, int numberOfDays) {
		this.startDate = startDate;
		this.numberOfDays = numberOfDays;
	}
	
	/**
	 * Adjusts the time window represented by this object to begin starting
	 * with the newStartDate.  Removes all DayLayout objects that fall on
	 * days outside the window specified by (newStartDate, numberOfDays).
	 * Returns the DayLayout objects that it removed.
	 * 
	 * @param newStartDate The startDate that defines the window beginning
	 * @return List Represents the DayLayout instances outside the time window.
	 */
	public List adjustStartDate(Date newStartDate) {
		this.startDate = newStartDate;
		
		LinkedList datesToRemove = findObsoleteDates();
		LinkedList result = removeObsoleteDayLayouts(datesToRemove);
		return result;
	}

	private LinkedList findObsoleteDates() {
		LinkedList datesToRemove = new LinkedList();
		for (Iterator dayLayoutByDateIter = dayLayoutsByDate.keySet().iterator(); dayLayoutByDateIter.hasNext();) {
			Date elementDate = (Date) dayLayoutByDateIter.next();
			if (elementDate.before(startDate)) {
				datesToRemove.add(elementDate);
			}
			if (elementDate.after(endDate())) {
				datesToRemove.add(elementDate);
			}
		}
		return datesToRemove;
	}
	
	private LinkedList removeObsoleteDayLayouts(LinkedList datesToRemove) {
		LinkedList result = new LinkedList();
		for (Iterator datesToRemoveIter = datesToRemove.iterator(); datesToRemoveIter.hasNext();) {
			Date elementDate = (Date) datesToRemoveIter.next();
			result.add(dayLayoutsByDate.remove(elementDate));
		}
		return result;
	}
	
	/**
	 * Returns the current start date.
	 * 
	 * @return The current start date.
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Returns the DayLayout object for a particular date.  If date is outside
	 * the window specified by (startDate, numberOfDays), null is returned.
	 * 
	 * @param date The date whose DayLayout should be returned
	 * @return The DayLayout corresponding to date or null if date is outside the time window
	 */
	public DayLayout get(Date date) {
		return (DayLayout) dayLayoutsByDate.get(date);
	}
	
	/**
	 * Associates a DayLayout object with a particular Date.  If date is outside
	 * the time window implied by (startDate, numberOfDays), an 
	 * IllegalArgumentException is thrown.  If a null date is passed, an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param date Date The date with which to assocate the dayLayout.
	 * @param dayLayout DayLayout The DayLayout object to associate with date.
	 */
	public void put(Date date, DayLayout dayLayout) {
		if(date == null) {
			throw new IllegalArgumentException("Date is null.");
		}
		
		if(date.before(startDate)) {
			throw new IllegalArgumentException("Date is before time window.");
		}
		if(date.after(endDate())) {
			throw new IllegalArgumentException("Date is before time window.");
		}
		
		dayLayoutsByDate.put(date, dayLayout);
	}

	private Date endDate() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(Calendar.DAY_OF_MONTH, numberOfDays - 1);
		return gc.getTime();
	}
	
}
