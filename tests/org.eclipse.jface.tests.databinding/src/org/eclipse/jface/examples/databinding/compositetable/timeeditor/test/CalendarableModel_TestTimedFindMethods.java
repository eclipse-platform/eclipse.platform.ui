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
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
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
		cm.setDayEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
		EventLayoutComputer dm = new EventLayoutComputer(4);
		for (int day = 0; day < cmf.getNumberOfDays(); ++day ) {
			Calendarable[][] eventLayout = dm.computeEventLayout(cm.getCalendarableEvents(day));
			cm.setEventLayout(day, eventLayout);
		}

		cm0 = new CalendarableModel();
		cm0.setTimeBreakdown(cmf0.getNumberOfDays(), 4);
		cm0.setDayEventCountProvider(cmf0.eventCountProvider);
		cm0.setEventContentProvider(cmf0.eventContentProvider);
		cm0.setStartDate(cmf0.startDate);
		for (int day = 0; day < cmf0.getNumberOfDays(); ++day ) {
			Calendarable[][] eventLayout = dm.computeEventLayout(cm0.getCalendarableEvents(day));
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
			new Event(time(11, 00), time(2, 45), "Meet with customer3")},
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

	private Calendarable calendarable(int day, int event) {
		return (Calendarable) cm.getCalendarableEvents(day).get(event);
	}
	
	public void testFindTimedCalendarable_ForwardWithoutSelection_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 51, true, null);
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardWithoutSelection_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 20, false, null);
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_ForwardWithoutSelection_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 40, true, null);
		assertEquals("Should have found Calendarable", calendarable(0, 1), found);
	}

	public void testFindTimedCalendarable_BackwardWithoutSelection_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 40, false, null);
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}

	public void testFindTimedCalendarable_ForwardWithSelection_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 46, true, calendarable(0, 1));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardWithSelection_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 25, false, calendarable(0, 0));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_ForwardWithSelection_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 25, true, calendarable(0, 0));
		assertEquals("Should have found Calendarable", calendarable(0, 1), found);
	}

	public void testFindTimedCalendarable_BackwardWithSelection_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(0, 46, false, calendarable(0, 1));
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}

	public void testFindTimedCalendarable_ForwardEventCollision_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(4, 52, true, calendarable(4, 4));
		assertEquals("Should have found Calendarable", calendarable(4, 5), found);
	}

	public void testFindTimedCalendarable_BackwardEventCollision_GotAHit() throws Exception {
		Calendarable found = cm.findTimedCalendarable(4, 40, false, calendarable(4, 4));
		assertEquals("Should have found Calendarable", calendarable(4, 3), found);
	}
	
	public void testFindTimedCalendarable_ForwardEventCollision_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(6, 47, true, calendarable(6, 3));
		assertNull("Should not find any Calendarable", found);
	}

	public void testFindTimedCalendarable_BackwardEventCollision_NothingToFind() throws Exception {
		Calendarable found = cm.findTimedCalendarable(6, 41, false, calendarable(6, 0));
		assertNull("Should not find any Calendarable", found);
	}
	
	// findNextCalendarable tests ---------------------------------------------
	
	public void testFindNextCalendarable_FindNextAlldayEventInSameDay() throws Exception {
		Calendarable found = cm.findNextCalendarable(4, 0, calendarable(4, 0), true);
		assertEquals("Should have found Calendarable", calendarable(4, 1), found);
	}
	
	public void testFindNextCalendarable_StartWithAllday_GetFirstTimedEventFromAllDaySelection() throws Exception {
		Calendarable found = cm.findNextCalendarable(4, 1, calendarable(4, 1), true);
		assertEquals("Should have found Calendarable", calendarable(4, 2), found);
	}

	public void testFindNextCalendarable_StartWithAllday_GetFirstTimedEventFromNoSelection() throws Exception {
		Calendarable found = cm.findNextCalendarable(4, 2, null, false);
		assertEquals("Should have found Calendarable", calendarable(4, 2), found);
	}

	public void testFindNextCalendarable_WrapToNextDay() throws Exception {
		Calendarable found = cm.findNextCalendarable(6, 47, calendarable(6, 3), false);
		assertEquals("Should have found Calendarable", calendarable(8, 0), found);
	}
	
	public void testFindNextCalendarable_WrapFromLastDayToFirstDay() throws Exception {
		Calendarable found = cm.findNextCalendarable(15, 42, calendarable(15, 1), false);
		assertEquals("Should have found Calendarable", calendarable(0, 0), found);
	}
	
	public void testFindNextCalendarable_NoEventsInDisplay() throws Exception {
		Calendarable found = cm0.findNextCalendarable(15, 42, null, false);
		assertNull("Should find no events", found);
	}
	
	// findPreviousCalendarable tests -----------------------------------------

	public void testFindPreviousCalendarable_FindPreviousTimedEventInSameDay() throws Exception {
		fail("Implement me, please");
	}
	
	public void testFindPreviousCalendarable_FindLastAlldayEventInSameDay() throws Exception {
		fail("Implement me, please");
	}
	
	public void testFindPreviousCalendarable_WrapToPreviousDay() throws Exception {
		fail("Implement me, please");
	}
	
	public void testFindPreviousCalendarable_WrapFromFirstDayToLastDay() throws Exception {
		fail("Implement me, please");
	}
	
	public void testFindPreviousCalendarable_NoEventsInDisplay() throws Exception {
		fail("Implement me, please");
	}
}


