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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.EventLayoutComputer;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;

/**
 * Test for find methods against timed Calendarables.
 */
public class CalendarableModel_TestTimedFindMethods extends TestCase {
	private CalendarableModel cm;
	private CalendarableModel cm0;
	private CMTimedEventFixture cmf = new CMTimedEventFixture(events);
	private CMTimedEventFixture cmf0 = new CMTimedEventFixture(events0);

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		cm = new CalendarableModel();
		cm.setTimeBreakdown(cmf.getNumberOfDays(), 4);
		cm.setEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
		EventLayoutComputer dm = new EventLayoutComputer(4);
		for (int day = 0; day < cmf.getNumberOfDays(); ++day ) {
			CalendarableItem[][] eventLayout = dm.computeEventLayout(cm.getCalendarableItems(day));
			cm.setEventLayout(day, eventLayout);
		}

		cm0 = new CalendarableModel();
		cm0.setTimeBreakdown(cmf0.getNumberOfDays(), 4);
		cm0.setEventCountProvider(cmf0.eventCountProvider);
		cm0.setEventContentProvider(cmf0.eventContentProvider);
		cm0.setStartDate(cmf0.startDate);
		for (int day = 0; day < cmf0.getNumberOfDays(); ++day ) {
			CalendarableItem[][] eventLayout = dm.computeEventLayout(cm0.getCalendarableItems(day));
			cm0.setEventLayout(day, eventLayout);
		}
	}
	
	public static class Event {
		public boolean allDay = false;
		public Date startTime;
		public Date endTime;
		public String text;
		
		public Event(Date startTime, Date endTime, String description) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.text = description;
		}

		public Event(String description) {
			this.allDay = true;
			this.text = description;
		}
	}
	
	private static Date time(int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		return gc.getTime();
	}
	
	private static final Event[][] events = new Event[][] {
		{new Event(time(5, 45), time(9, 45), "Stand-up meeting"),
			new Event(time(11, 00), time(12, 15), "Meet with customer")},
		{},
		{},
		{new Event("Nat. Conference"),
			new Event(time(7, 50), time(9, 00), "Stand-up meeting"),
			new Event(time(10, 15), time(12, 00), "Work on prototype")},
		{new Event("Field trip to PC HQ"),
			new Event("Nat. Conference"),
			new Event(time(8, 30), time(9, 30), "Stand-up meeting"),
			new Event(time(10, 00), time(13, 15), "Meet with customer"),
			new Event(time(12, 45), time(14, 15), "RC1 due"),
			new Event(time(13, 45), time(14, 15), "Way too much work"),
			new Event(time(10, 00), time(13, 30), "Callisto meeting")},
		{new Event("Nat. Conference")},
		{new Event(time(8, 30), time(11, 30), "Stand-up meeting"),
			new Event(time(10, 00), time(12, 15), "Meet with customer1"),
			new Event(time(11, 45), time(12, 15), "Meet with customer2"),
			new Event(time(11, 00), time(11, 15), "Meet with customer3")},
		{},
		{new Event(time(8, 50), time(9, 00), "Stand-up meeting"),
			new Event(time(10, 15), time(12, 00), "Work on prototype")},
		{new Event(time(8, 45), time(9, 45), "Stand-up meeting"),
			new Event(time(11, 00), time(12, 15), "Meet with customer")},
		{},
		{},
		{new Event(time(8, 12), time(9, 00), "Stand-up meeting"),
			new Event(time(10, 15), time(12, 00), "Work on prototype")},
		{},
		{},
		{new Event(time(9, 50), time(9, 00), "Stand-up meeting"),
			new Event(time(10, 15), time(12, 00), "Work on prototype")},
		{},
	};
	
	private static final Event[][] events0 = new Event[][] {
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
		{},
	};

	private CalendarableItem calendarable(int day, int event) {
		return (CalendarableItem) cm.getCalendarableItems(day).get(event);
	}
	
	public void testFindTimedCalendarable_ForwardWithoutSelection_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 51, -1, true, null);
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardWithoutSelection_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 20, -1, false, null);
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_ForwardWithoutSelection_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 40, -1, true, null);
		assertEquals("Should have found Calendarable", calendarable(0, 1), found);
	}

	public void testFindTimedCalendarable_BackwardWithoutSelection_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 40, -1, false, null);
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}

	public void testFindTimedCalendarable_ForwardWithSelection_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 46, -1, true, calendarable(0, 1));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardWithSelection_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 25, -1, false, calendarable(0, 0));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_ForwardWithSelection_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 25, -1, true, calendarable(0, 0));
		assertEquals("Should have found Calendarable", calendarable(0, 1), found);
	}

	public void testFindTimedCalendarable_BackwardWithSelection_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(0, 46, -1, false, calendarable(0, 1));
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}

	public void testFindTimedCalendarable_ForwardEventCollision_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(4, 52, -1, true, calendarable(4, 4));
		assertEquals("Should have found Calendarable", calendarable(4, 5), found);
	}

	public void testFindTimedCalendarable_BackwardEventCollision_GotAHit() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(6, 48, -1, false, calendarable(6, 3));
		assertEquals("Should have found Calendarable", calendarable(6, 2), found);
	}
	
	public void testFindTimedCalendarable_ForwardEventCollision_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(6, 47, -1, true, calendarable(6, 3));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardEventCollision_NothingToFind() throws Exception {
		CalendarableItem found = cm.findTimedCalendarable(6, 41, -1, false, calendarable(6, 0));
		assertNull("Should not find any Calendarable", found);
	}
	
	// findNextCalendarable tests ---------------------------------------------
	
	public void testFindNextCalendarable_FindNextAlldayEventInSameDay() throws Exception {
		CalendarableItem found = cm.findNextCalendarable(4, 0, calendarable(4, 0), true);
		assertEquals("Should have found Calendarable", calendarable(4, 1), found);
	}
	
	public void testFindNextCalendarable_StartWithAllday_GetFirstTimedEventFromAllDaySelection() throws Exception {
		CalendarableItem found = cm.findNextCalendarable(4, 1, calendarable(4, 1), true);
		assertEquals("Should have found Calendarable", calendarable(4, 2), found);
	}

	public void testFindNextCalendarable_StartWithAllday_GetFirstTimedEventFromNoSelection() throws Exception {
		CalendarableItem found = cm.findNextCalendarable(4, 2, null, false);
		assertEquals("Should have found Calendarable", calendarable(4, 2), found);
	}

	public void testFindNextCalendarable_WrapToNextDay() throws Exception {
		CalendarableItem found = cm.findNextCalendarable(6, 47, calendarable(6, 3), false);
		assertEquals("Should have found Calendarable", calendarable(8, 0), found);
	}
	
	public void testFindNextCalendarable_WrapFromLastDayToFirstDay() throws Exception {
		CalendarableItem found = cm.findNextCalendarable(15, 42, calendarable(15, 1), false);
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}
	
	public void testFindNextCalendarable_NoEventsInDisplay() throws Exception {
		CalendarableItem found = cm0.findNextCalendarable(15, 42, null, false);
		assertNull("Should find no events", found);
	}
	
	// findPreviousCalendarable tests -----------------------------------------

	public void testFindPreviousCalendarable_FindPreviousTimedEventInSameDay() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(0, 44, calendarable(0, 1), false);
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}
	
	public void testFindPreviousCalendarable_FindLastAlldayEventInSameDay() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(3, 32, calendarable(3, 1), false);
		assertEquals("Should have found Calendarable", calendarable(3, 0), found);
	}
	
	public void testFindPreviousCalendarable_FindPreviousAlldayEventInSameDay() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(4, 1, calendarable(4, 1), true);
		assertEquals("Should have found Calendarable", calendarable(4, 0), found);
	}
	
	public void testFindPreviousCalendarable_WrapToPreviousDay_AllDayEvent() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(4, 0, calendarable(4, 0), true);
		assertEquals("Should have found Calendarable", calendarable(3, 2), found);
	}
	
	public void testFindPreviousCalendarable_WrapToPreviousDay_TimedEvent() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(8, 35, calendarable(8, 0), false);
		assertEquals("Should have found Calendarable", calendarable(6, 3), found);
	}
	
	public void testFindPreviousCalendarable_WrapFromFirstDayToLastDay() throws Exception {
		CalendarableItem found = cm.findPreviousCalendarable(0, 24, calendarable(0, 0), false);
		assertEquals("Should have found Calendarable", calendarable(15, 1), found);
	}
	
	public void testFindPreviousCalendarable_NoEventsInDisplay() throws Exception {
		CalendarableItem found = cm0.findPreviousCalendarable(15, 42, null, false);
		assertNull("Should find no events", found);
	}
}


