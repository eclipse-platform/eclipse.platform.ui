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
	private CalendarableItem[][][] eventLayout = null;  // [day][column][row]

	private int defaultStartHour = DEFAULT_START_HOUR;
	
	/**
	 * @param dayOffset
	 * @return the number of columns within the day or -1 if this has not been computed yet.
	 */
	public int getNumberOfColumnsWithinDay(int dayOffset) {
		if (eventLayout == null) {
			return -1;
		}
		if (eventLayout[dayOffset] == null) {
			return -1;
		}
		return eventLayout[dayOffset].length;
	}
	
	/**
	 * Sets the eventLayout for a particular dayOffset
	 * 
	 * @param dayOffset
	 * @param eventLayout
	 */
	public void setEventLayout(int dayOffset, CalendarableItem[][] eventLayout) {
		this.eventLayout[dayOffset] = eventLayout;
	}
	
	/**
	 * Gets the eventLayout for a particular dayOffset
	 * 
	 * @param dayOffset
	 * @return the eventLayout array for the specified day or null if none has been computed.
	 */
	public CalendarableItem[][] getEventLayout(int dayOffset) {
		return eventLayout[dayOffset];
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
		eventLayout = new CalendarableItem[numberOfDays][][];
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
		// If there's no overlap between the old and new date ranges
		if (this.startDate == null || 
				startDate.after(calculateDate(this.startDate, numberOfDays-1)) ||
				calculateDate(startDate, numberOfDays-1).before(this.startDate))
		{
			this.startDate = startDate;
			eventLayout = new CalendarableItem[numberOfDays][][];
			return refresh();
		}
		
		// There's an overlap
		List obsoleteCalendarables = new LinkedList();
		int overlap = -1;
		
		// If we're scrolling viewport to the left
		if (startDate.before(this.startDate)) {
			// Calculate the overlap
			for (int day=0; day < numberOfDays; ++day) {
				Date candidate = calculateDate(startDate, day);
				if (candidate.equals(this.startDate))
					overlap = day;
			}
			for (int day=numberOfDays-1; day >= 0; --day) {
				if (numberOfDays - day <= overlap) {
					// Shift the arrays; track obsolete calendarables
					for (Iterator invalidated = dayColumns[day].iterator(); invalidated.hasNext();) {
						obsoleteCalendarables.add(invalidated.next());
					}
					dayColumns[day] = dayColumns[day-overlap];
					eventLayout[day] = eventLayout[day-overlap];
				} if (day >= overlap) {
					// Shift the arrays
					dayColumns[day] = dayColumns[day-overlap];
					eventLayout[day] = eventLayout[day-overlap];
				} else {
					// Recalculate new columns
					dayColumns[day] = new ArrayList();
					eventLayout[day] = null;
					refresh(calculateDate(startDate, day), day, obsoleteCalendarables);
				}
			}
		} else {
			// We're scrolling the viewport to the right
			for (int day=0; day < numberOfDays; ++day) {
				Date candidate = calculateDate(this.startDate, day);
				if (candidate.equals(startDate))
					overlap = day;
			}
			for (int day=0; day < numberOfDays; ++day) {
				if (day < overlap) {
					// Shift the arrays; track obsolete calendarables
					for (Iterator invalidated = dayColumns[day].iterator(); invalidated.hasNext();) {
						obsoleteCalendarables.add(invalidated.next());
					}
					dayColumns[day] = dayColumns[day+overlap];
					eventLayout[day] = eventLayout[day+overlap];
				} if (day < numberOfDays - overlap) {
					// Shift the arrays
					dayColumns[day] = dayColumns[day+overlap];
					eventLayout[day] = eventLayout[day+overlap];
				} else {
					// Recalculate new columns
					dayColumns[day] = new ArrayList();
					eventLayout[day] = null;
					refresh(calculateDate(startDate, day), day, obsoleteCalendarables);
				}
			}
		}
		this.startDate = startDate;
		return obsoleteCalendarables;
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
	public void setEventCountProvider(EventCountProvider eventCountProvider) {
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
		if (startDate == null) {
			return new LinkedList();
		}
		LinkedList result = new LinkedList();
		if(!isInitialized()) {
			return result;
		}
		//refresh
		Date dateToRefresh = null;
		for (int i = 0; i < dayColumns.length; i++) {
			dateToRefresh = calculateDate(startDate, i);
			refresh(dateToRefresh, i, result);
		}
		return result;
	}

	/**
	 * Returns the date that is the numberOfDaysFromStartDate.
	 * 
	 * @param startDate The start date 
	 * @param numberOfDaysFromStartDate
	 * @return Date
	 */
	public Date calculateDate(Date startDate, int numberOfDaysFromStartDate) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(Calendar.DATE, numberOfDaysFromStartDate);
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
		resizeList(date, dayColumns[column], numberOfEventsInDay);
		
		CalendarableItem[] tempEvents = (CalendarableItem[]) dayColumns[column]
				.toArray(new CalendarableItem[numberOfEventsInDay]);

		eventContentProvider.refresh(
				date, 
				tempEvents);
	}

	private void resizeList(Date date, ArrayList list, int numberOfEventsInDay) {
		while (list.size() < numberOfEventsInDay) {
			list.add(new CalendarableItem(date));
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
			Date refreshTarget = calculateDate(startDate, offset);
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
	public List getCalendarableItems(int dayOffset) {
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
				CalendarableItem event = (CalendarableItem) iter.next();
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
				CalendarableItem event = (CalendarableItem) iter.next();
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
	

	/**
	 * Method getDay.  Returns the day on which the specified Calendarable appers.
	 * 
	 * @param calendarable The calendarable to find
	 * @return The day offset (0-based)
	 * @throws IllegalArgumentException if Calendarable isn't found
	 */
	public int getDay(CalendarableItem calendarable) {
		for (int day = 0; day < dayColumns.length; day++) {
			for (Iterator calendarableIter = dayColumns[day].iterator(); calendarableIter.hasNext();) {
				CalendarableItem event = (CalendarableItem) calendarableIter.next();
				if (event == calendarable) {
					return day;
				}
			}
		}
		throw new IllegalArgumentException("Invalid Calenderable passed");
	}

	/**
	 * FIXME: Test me please
	 * @param row The row starting from the beginning of the day
	 * @return The hour portion of the time that this row represents
	 */
	public int computeHourFromRow(int row) {
		return row / getNumberOfDivisionsInHour() + computeStartHour();
	}

	/**
	 * FIXME: Test me please
	 * @param row The row starting from the beginning of the day
	 * @return The minute portion of the time that this row represents
	 */
	public int computeMinuteFromRow(int row) {
		int numberOfDivisionsInHour = getNumberOfDivisionsInHour();
		int minute = (int) ((double) row
				% numberOfDivisionsInHour
				/ numberOfDivisionsInHour * 60);
		return minute;
	}
	
	/**
	 * @param day The day to return all day Calendarables for
	 * @return All the all day Calendarables for the specified day, order maintained
	 */
	public CalendarableItem[] getAllDayCalendarables(int day) {
		List allDays = new LinkedList();
		for (Iterator calendarablesIter = getCalendarableItems(day).iterator(); calendarablesIter.hasNext();) {
			CalendarableItem candidate = (CalendarableItem) calendarablesIter.next();
			if (candidate.isAllDayEvent()) {
				allDays.add(candidate);
			}
		}
		return (CalendarableItem[]) allDays.toArray(new CalendarableItem[allDays.size()]);
	}

	/**
	 * @param day The day to search
	 * @param forward true if we're going forward; false if we're searching backward
	 * @param selection The currently selected Calendarable or null if none
	 * @return The next Calendarable in the specified direction where result != selection; null if none
	 */
	public CalendarableItem findAllDayCalendarable(int day, boolean forward, CalendarableItem selection) {
		CalendarableItem[] calendarables = getAllDayCalendarables(day);
		if (forward) {
			if (calendarables.length < 1) {
				return null;
			}
			if (selection == null) {
				return null;
			} else if (selection == calendarables[calendarables.length-1]) {
				return null;
			}
			for (int selected = 0; selected < calendarables.length; selected++) {
				if (calendarables[selected] == selection) {
					return calendarables[selected+1];
				}
			}
		} else {
			if (calendarables.length < 1) {
				return null;
			}
			if (selection == null) {
				return calendarables[calendarables.length-1];
			} else if (selection == calendarables[0]) {
				return null;
			}
			for (int selected = 0; selected < calendarables.length; selected++) {
				if (calendarables[selected] == selection) {
					return calendarables[selected-1];
				}
			}
		}
		return null;
	}
	
	/**
	 * @param day The day to search
	 * @param currentRow The first row to search
	 * @param stopPosition The row to stop searching on or -1 to search to the first/last element 
	 * @param forward true if we're going forward; false if we're searching backward
	 * @param selection The Calendarable associated with currentRow or null if none
	 * @return The next Calendarable in the specified direction where result != selection; null if none
	 */
	public CalendarableItem findTimedCalendarable(int day, int currentRow, int stopPosition, boolean forward, CalendarableItem selection) {
		CalendarableItem[][] eventLayoutForDay = getEventLayout(day);
		if (eventLayoutForDay == null) {
			throw new IllegalArgumentException("Day " + day + " has no event data!!!");
		}
		
		int startColumn = 0;
		if (selection != null) {
			startColumn = findCalendarable(selection, currentRow, eventLayoutForDay);
			if (startColumn == -1) {
				throw new IllegalArgumentException("day " + day + ", row " + currentRow + " does not contain the specified Calendarable");
			}
		}
		
		int currentColumn = startColumn;
		if (forward) {
			if (stopPosition == -1) {
				stopPosition = eventLayoutForDay[0].length;
			}
			for (int row = currentRow; row < stopPosition; row++) {
				while (true) {
					CalendarableItem candidate = eventLayoutForDay[currentColumn][row];
					if (candidate != null && candidate != selection) {
						if (selection == null || 
							candidate.getStartTime().after(selection.getStartTime()) || 
							(currentColumn > startColumn && !candidate.getStartTime().before(selection.getStartTime()))) 
						{
							return candidate;
						}
					}
					++currentColumn;
					if (currentColumn >= eventLayoutForDay.length) {
						currentColumn = 0;
						break;
					}
				}
			}
		} else {
			if (stopPosition == -1) {
				stopPosition = 0;
			}
			for (int row = currentRow; row >= stopPosition; --row) {
				while (true) {
					CalendarableItem candidate = eventLayoutForDay[currentColumn][row];
					if (candidate != null && candidate != selection && candidate.getUpperLeftPositionInDayRowCoordinates().y == row) {
						if (selection == null || 
							candidate.getStartTime().before(selection.getStartTime()) || 
							(currentColumn < startColumn && !candidate.getStartTime().after(selection.getStartTime()))) 
						{
							if (selection == null && currentColumn > 0) {
								// The candidate could have an earlier start time
								// than some other column
								for (int earlierColumn = currentColumn-1; earlierColumn >= 0; --earlierColumn) {
									CalendarableItem newCandidate = eventLayoutForDay[earlierColumn][row];
									if (newCandidate.getStartTime().after(candidate.getStartTime())) {
										candidate = newCandidate;
									}
								}
							}
							return candidate;
						}
					}
					--currentColumn;
					if (currentColumn < 0) {
						currentColumn = eventLayoutForDay.length-1;
						break;
					}
				}
			}
		}
		return null;
	}

	private int findCalendarable(CalendarableItem selection, int currentRow, CalendarableItem[][] eventLayoutForDay) {
		for (int column = 0; column < eventLayoutForDay.length; column++) {
			if (eventLayoutForDay[column][currentRow] == selection) {
				return column;
			}
		}
		return -1;
	}

	/**
	 * @param selectedDay
	 * @param selectedRow
	 * @param selection
	 * @param isAllDayEventRow
	 * @return
	 */
	public CalendarableItem findNextCalendarable(int selectedDay, int selectedRow, CalendarableItem selection, boolean isAllDayEventRow) {
		// Search the rest of the selectedDay starting at selectedRow
		CalendarableItem result = null;
		if (isAllDayEventRow) {
			result = findAllDayCalendarable(selectedDay, true, selection);
			if (result != null)
				return result;
			result = findTimedCalendarable(selectedDay, 0, -1, true, null);
			if (result != null)
				return result;
		} else {
			result = findTimedCalendarable(selectedDay, selectedRow, -1, true, selection);
			if (result != null) {
				return result;
			}
		}
		
		// Search all days other than selectedDay
		int currentDay = nextDay(selectedDay);
		while (currentDay != selectedDay) {
			// Is there an all-day event to select?
			CalendarableItem[] allDayCalendarables = getAllDayCalendarables(currentDay);
			if (allDayCalendarables.length > 0) {
				return allDayCalendarables[0];
			}
			
			// Nope, search for the first timed event
			result = findTimedCalendarable(currentDay, 0, -1, true, null);
			if (result != null) {
				return result;
			}
			
			currentDay = nextDay(currentDay);
		}
		
		// Search selectedDay from 0 to selectedRow
		CalendarableItem[] allDayCalendarables = getAllDayCalendarables(selectedDay);
		if (allDayCalendarables.length > 0) {
			return allDayCalendarables[0];
		}
		
		// If we started in an all-day event row, we're done searching.
		if (isAllDayEventRow) {
			return null;
		}
		
		// Search the last of the timed-events
		result = findTimedCalendarable(selectedDay, 0, selectedRow-1, true, null);
//		result = findTimedCalendarable(selectedDay, 0, selectedRow-1, true, selection);
		if (result != null) {
			return result;
		}
		
		// Nothing more to search; give up.
		return null;
	}

	private int nextDay(int selectedDay) {
		++selectedDay;
		if (selectedDay >= numberOfDays) {
			selectedDay = 0;
		}
		return selectedDay;
	}

	/**
	 * @param selectedDay
	 * @param selectedRow
	 * @param selection
	 * @param isAllDayEventRow 
	 * @return
	 */
	public CalendarableItem findPreviousCalendarable(int selectedDay, int selectedRow, CalendarableItem selection, boolean isAllDayEventRow) {
		CalendarableItem result = null;
		
		// Search to the beginning of the current day
		if (!isAllDayEventRow) {
			// search timed events to the beginning of the day
			result = findTimedCalendarable(selectedDay, selectedRow, -1, false, selection);
			if (result != null)
				return result;
			
			// Search all-day events
			result = findAllDayCalendarable(selectedDay, false, null);
			if (result != null)
				return result;
		} else {
			result = findAllDayCalendarable(selectedDay, false, selection);
			if (result != null)
				return result;
		}
		
		// Search all days other than selectedDay
		int currentDay = previousDay(selectedDay);
		while (currentDay != selectedDay) {
			// Nope, search for the first timed event
			result = findTimedCalendarable(currentDay, 
					getEventLayout(selectedDay)[0].length-1, 
					-1, false, null);
			if (result != null) {
				return result;
			}
			
			// Is there an all-day event to select?
			CalendarableItem[] allDayCalendarables = getAllDayCalendarables(currentDay);
			if (allDayCalendarables.length > 0) {
				return allDayCalendarables[allDayCalendarables.length-1];
			}
			
			currentDay = previousDay(currentDay);
		}
		
		// Search from the end of the current day to the current time
		result = findTimedCalendarable(currentDay, 
				getEventLayout(selectedDay)[0].length-1, 
				selectedRow+1, false, null);
		if (result != null) {
			return result;
		}

		return null;
	}

	private int previousDay(int selectedDay) {
		--selectedDay;
		if (selectedDay < 0) {
			selectedDay = numberOfDays-1;
		}
		return selectedDay;
	}
}


