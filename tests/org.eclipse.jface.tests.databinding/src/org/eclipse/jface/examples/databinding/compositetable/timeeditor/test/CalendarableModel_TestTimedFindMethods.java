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

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;

/**
 * Test for find methods against timed Calendarables.
 */
public class CalendarableModel_TestTimedFindMethods extends TestCase {
	private CalendarableModel cm;
	private CMTimedEventFixture cmf = new CMTimedEventFixture(data);

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		cm = new CalendarableModel();
		cm.setTimeBreakdown(cmf.getNumberOfDays(), 4);
		cm.setDayEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
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
	
	private static final Event[][] data = new Event[][] {
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
		{new Event(time(8, 30), time(11, 30), "Stand-up meeting"),
			new Event(time(10, 00), time(12, 15), "Meet with customer"),
			new Event(time(11, 45), time(12, 15), "Meet with customer"),
			new Event(time(11, 00), time(2, 45), "Meet with customer")},
		{new Event(time(9, 50), time(9, 00), "Stand-up meeting"),
			new Event(time(10, 15), time(12, 00), "Work on prototype")},
		{},
	};

	public void testFindTimedCalendarable_ForwardWihoutSelection_NothingToFind() throws Exception {
		fail("TODO: Implement me please");
	}

	public void testFindTimedCalendarable_BackwardWihoutSelection_NothingToFind() throws Exception {
		fail("TODO: Implement me please");
	}

	public void testFindTimedCalendarable_ForwardWihoutSelection_GotAHit() throws Exception {
		fail("TODO: Implement me please");
	}

	public void testFindTimedCalendarable_BackwardWihoutSelection_GotAHit() throws Exception {
		fail("TODO: Implement me please");
	}
	
	public void testFindTimedCalendarable_ForwardEventCollision_GotAHit() throws Exception {
		fail("TODO: Implement me please");
	}

	public void testFindTimedCalendarable_BackwardEventCollision_GotAHit() throws Exception {
		fail("TODO: Implement me please");
	}
	
	public void testFindTimedCalendarable_ForwardEventCollision_NothingToFind() throws Exception {
		fail("TODO: Implement me please");
	}

	public void testFindTimedCalendarable_BackwardEventCollision_NothingToFind() throws Exception {
		fail("TODO: Implement me please");
	}
	

}
