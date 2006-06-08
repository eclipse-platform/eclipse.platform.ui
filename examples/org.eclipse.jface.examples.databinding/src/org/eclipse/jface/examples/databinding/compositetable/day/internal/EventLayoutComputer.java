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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.swt.graphics.Point;

/**
 * Represents a model of how the events are laid out in a particular day
 * 
 * @since 3.2
 */
public class EventLayoutComputer {
	
	private static final int START = 0;
	private static final int END = 1;
	private final int numberOfDivisionsInHour;
	
	/**
	 * Construct a DayModel for an IEventEditor.
	 * TODO: We could make numberOfDivisionsInHour a parameter to getEventLayout()
	 * 
	 * @param numberOfDivisionsInHour 
	 */
	public EventLayoutComputer(int numberOfDivisionsInHour) {
		this.numberOfDivisionsInHour = numberOfDivisionsInHour;
	}
	
	private int computeBaseSlot(GregorianCalendar gc) {
		return gc.get(Calendar.HOUR_OF_DAY) * numberOfDivisionsInHour;
	}
	
	private float computeAdditionalSlots(GregorianCalendar gc) {
		return ((float)gc.get(Calendar.MINUTE)) / 60 * numberOfDivisionsInHour;
	}
	
	private int getSlotForStartTime(Date time) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(time);
		return computeBaseSlot(gc) + ((int) computeAdditionalSlots(gc));
	}

	private int getSlotForEndTime(Date time) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(time);
		
		int baseSlot = computeBaseSlot(gc);
		float additionalSlots = computeAdditionalSlots(gc);
		
		return keepExtraTimeIfEndTimePushesIntoNextTimeSlot(baseSlot, additionalSlots);
	}

	private int keepExtraTimeIfEndTimePushesIntoNextTimeSlot(int baseSlot, float additionalSlots) {
		if(additionalSlots % (int)additionalSlots > 0) {
			return baseSlot + (int)additionalSlots;
		}
		return baseSlot + (int)additionalSlots-1;
	}

	private int[] getSlotsForEvent(CalendarableItem event) {
		int startTime = getSlotForStartTime(event.getStartTime());
		int endTime = getSlotForEndTime(event.getEndTime());
		if (endTime >= startTime) {
			return new int[] {startTime, endTime};
		}
		return new int[] {startTime, startTime};
	}
	
	private class EventLayout {
		private CalendarableItem[][] eventLayout;
		private final int timeSlotsInDay;

		public EventLayout(int timeSlotsInDay) {
			this.timeSlotsInDay = timeSlotsInDay;
			eventLayout = new CalendarableItem[1][timeSlotsInDay];
			initializeColumn(0, timeSlotsInDay);
		}
		
		private void initializeColumn(int column, final int timeSlotsInDay) {
			eventLayout[column] = new CalendarableItem[timeSlotsInDay];
			for (int slot = 0; slot < eventLayout[column].length; slot++) {
				eventLayout[column][slot] = null;
			}
		}
		
		public void addColumn() {
			CalendarableItem[][] old = eventLayout;
			eventLayout = new CalendarableItem[old.length+1][timeSlotsInDay];
			for (int i = 0; i < old.length; i++) {
				eventLayout[i] = old[i];
			}
			initializeColumn(eventLayout.length-1, timeSlotsInDay);
		}
		
		public CalendarableItem[][] getLayout() {
			return eventLayout;
		}

		public int getNumberOfColumns() {
			return eventLayout.length;
		}
	}

	/**
	 * Given an unsorted list of Calendarables, each of which has a start and an
	 * end time, this method will compute the day row coordinates for each 
	 * Calendarable, set that information into each Calendarable, and will
	 * return the number of columns that will be required to lay out the given
	 * list of Calendarables.
	 * 
	 * @param calendarables
	 *            A list of Calenderables
	 * @return The number of columns required to lay out those Calendarables. 
	 */
	public CalendarableItem[][] computeEventLayout(List calendarables) {
		Collections.sort(calendarables, CalendarableItem.comparator);
		
		final int timeSlotsInDay = IEventEditor.DISPLAYED_HOURS * numberOfDivisionsInHour;
		
		EventLayout eventLayout = new EventLayout(timeSlotsInDay);
		
		// Lay out events
		for (Iterator eventsIter = calendarables.iterator(); eventsIter.hasNext();) {
			CalendarableItem event = (CalendarableItem) eventsIter.next();
			if (event.isAllDayEvent()) continue;
			
			int[] slotsEventSpans = getSlotsForEvent(event);
			
			int eventColumn = findColumnForEvent(eventLayout, slotsEventSpans);
			placeEvent(event, eventLayout.getLayout(), eventColumn, slotsEventSpans);
		}
		
		// Expand them horizontally if possible
		for (Iterator eventsIter = calendarables.iterator(); eventsIter.hasNext();) {
			CalendarableItem event = (CalendarableItem) eventsIter.next();
			if (event.isAllDayEvent()) continue;

			int[] slotsEventSpans = getSlotsForEvent(event);
			int eventColumn = findEventColumn(event, eventLayout.getLayout(), slotsEventSpans);
			
			if (eventColumn < eventLayout.getNumberOfColumns()) {
				for (int nextColumn = eventColumn+1; nextColumn < eventLayout.getNumberOfColumns(); ++nextColumn) {
					if (columnIsAvailable(nextColumn, eventLayout.getLayout(), slotsEventSpans)) {
						placeEvent(event, eventLayout.getLayout(), nextColumn, slotsEventSpans);
					} else {
						break;
					}
				}
			}
		}
		
		return eventLayout.getLayout();
	}
	
	private int findEventColumn(CalendarableItem event, CalendarableItem[][] layout, int[] slotsEventSpans) {
		for (int column = 0; column < layout.length; column++) {
			if (layout[column][slotsEventSpans[START]] == event) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("Could not find event");
	}

	private int findColumnForEvent(EventLayout eventLayout, int[] slotsEventSpans) {
		int currentColumn = 0;
		while (true) {
			CalendarableItem[][] layout = eventLayout.getLayout();
			if (columnIsAvailable(currentColumn, layout, slotsEventSpans)) {
				return currentColumn;
			}
			if (isNewColumnNeeded(currentColumn, layout)) {
				eventLayout.addColumn();
			}
			++currentColumn;
		}
	}

	private boolean columnIsAvailable(int column, CalendarableItem[][] layout, int[] slotsEventSpans) {
		int currentSlot = slotsEventSpans[START];
		while (currentSlot <= slotsEventSpans[END]) {
			if (isSlotAlreadyOccupiedInColumn(currentSlot, layout, column)) {
				return false;
			}
			++currentSlot;
		}
		return true;
	}
	
	private void placeEvent(CalendarableItem event, CalendarableItem[][] eventLayout, int currentColumn, int[] slotsEventSpans) {
		for (int slot = slotsEventSpans[START]; slot <= slotsEventSpans[END]; ++slot) {
			eventLayout[currentColumn][slot] = event;
			Point position = new Point(currentColumn, slot);
			if (event.getUpperLeftPositionInDayRowCoordinates() == null) {
				event.setUpperLeftPositionInDayRowCoordinates(position);
			} else {
				event.setLowerRightPositionInDayRowCoordinates(position);
			}
		}
	}
	
	private boolean isSlotAlreadyOccupiedInColumn(int slot, CalendarableItem[][] layout, int currentColumn) {
		return layout[currentColumn][slot] != null;
	}

	private boolean isNewColumnNeeded(int currentColumn, CalendarableItem[][] layout) {
		return currentColumn >= layout.length-1;
	}
}


