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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;

/**
 * @since 3.2
 *
 */
public class DayModel_testGetColumnsForEvents extends TestCase {
	// Fixtures ---------------------------------------------------------------

	private final class EventEditorFixture implements IEventEditor {
		private final int divisions_in_hour;

		private EventEditorFixture(int divisions_in_hour) {
			super();
			this.divisions_in_hour = divisions_in_hour;
		}

		public void refresh(Date date) {
		}

		public void setEventContentProvider(
				EventContentProvider eventContentProvider) {
		}

		public void setDayEventCountProvider(EventCountProvider eventCountProvider) {
		}

		public void setStartDate(Date startDate) {
		}

		public int getNumberOfDivisionsInHour() {
			return divisions_in_hour;
		}

		public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		}
	}

	private Date time(int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		return gc.getTime();
	}
	
	private void addCalendarable(Date startTime, Date endTime, String description) {
		Calendarable c = new Calendarable();
		c.setStartTime(startTime);
		c.setEndTime(endTime);
		c.setText(description);
		expectedEvents.add(c);
	}
	
	// Tests ------------------------------------------------------------------
	
	private static final int DIVISIONS_IN_HOUR = 2;
	
	private IEventEditor eventEditor = new EventEditorFixture(DIVISIONS_IN_HOUR);
	private DayModel dayModel;
	private List expectedEvents;
	
	protected void setUp() throws Exception {
		super.setUp();
		dayModel = new DayModel(eventEditor);
		expectedEvents = new ArrayList();
	}

	public void test_getColumnsForEvents_NoEventsInDay() throws Exception {
		Calendarable[][] eventLayout = dayModel.getEventLayout(expectedEvents);
		assertEquals("One column", 1, eventLayout.length);
		assertEquals(IEventEditor.DISPLAYED_HOURS * DIVISIONS_IN_HOUR
				+ " time slots", IEventEditor.DISPLAYED_HOURS
				* DIVISIONS_IN_HOUR, eventLayout[0].length);
		
		for (int column = 0; column < eventLayout.length; column++) {
			for (int timeSlot = 0; timeSlot < eventLayout[column].length; timeSlot++) {
				assertNull(eventLayout[column][timeSlot]);
			}
		}
	}
	
	public void test_getColumnsForEvents_OneEventNoSpan() throws Exception {
		addCalendarable(time(8, 00), time(8, 30), "One event");
		Calendarable[][] eventLayout = dayModel.getEventLayout(expectedEvents);
		
		assertEquals("One column", 1, eventLayout.length);
		int slotForEvent = 16;
		
		for (int slot=0; slot < slotForEvent; ++slot) {
			assertNull("slots before event null", eventLayout[0][slot]);
		}
		assertEquals("should find event here", expectedEvents.get(0), eventLayout[0][slotForEvent]);
		for (int slot=slotForEvent+1; slot < eventLayout[0].length; ++slot) {
			assertNull("slots before event null", eventLayout[0][slot]);
		}
	}
}



