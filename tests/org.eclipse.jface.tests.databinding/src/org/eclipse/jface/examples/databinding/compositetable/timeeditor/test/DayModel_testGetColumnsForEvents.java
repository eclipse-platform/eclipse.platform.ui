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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.EventLayoutComputer;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.swt.graphics.Point;

/**
 * @since 3.2
 *
 */
public class DayModel_testGetColumnsForEvents extends TestCase {
	// Fixtures ---------------------------------------------------------------

	private CalendarableItem event(int number) {
		return (CalendarableItem) expectedEvents.get(number);
	}

	private Date time(int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		return gc.getTime();
	}
	
	private CalendarableItem addCalendarable(Date startTime, Date endTime, String description) {
		CalendarableItem c = new CalendarableItem(new Date());
		c.setStartTime(startTime);
		c.setEndTime(endTime);
		c.setText(description);
		expectedEvents.add(c);
		return c;
	}
	
	private CalendarableItem addCalendarable(String description) {
		CalendarableItem c = new CalendarableItem(new Date());
		c.setText(description);
		c.setAllDayEvent(true);
		expectedEvents.add(c);
		return c;
	}
	
	private void assertEventInColumnInPositions(CalendarableItem event, int column, int[] expectedSlotsForEvent) {
		int firstSlotForEvent = expectedSlotsForEvent[0];
		int lastSlotForEvent = expectedSlotsForEvent[expectedSlotsForEvent.length-1];
		int lastSlotInDay = eventLayout[column].length;
		
		//before event
		for (int slot=0; slot < firstSlotForEvent; ++slot) {
			assertNotSame("slots before event != event: " + slot, event, eventLayout[column][slot]);
		}
		
		//during event
		for (int slot=firstSlotForEvent; slot <= lastSlotForEvent; ++slot) {
			assertEquals("should find event here: " + slot, event, eventLayout[column][slot]);
		}
		
		//after event
		for (int slot=lastSlotForEvent+1; slot < lastSlotInDay; ++slot) {
			assertNotSame("slots after event != event: " + slot, event, eventLayout[column][slot]);
		}
	}
	
	private void assertEventNotInColumn(CalendarableItem event, int column) {
		for (int slot = 0; slot < eventLayout[column].length; slot++) {
			assertFalse("event not in column: " + column + ", " + slot, event == eventLayout[column][slot]);
		}
	}

	private void assertExpectedColumns(int expected) {
		assertEquals(expected+ " columns", expected, eventLayout.length);
	}

	private void assertBounds(CalendarableItem c, Point leftTop, Point rightBottom) {
		assertEquals("top left position", leftTop, c.getUpperLeftPositionInDayRowCoordinates());
		assertEquals("bottom right position", rightBottom, c.getLowerRightPositionInDayRowCoordinates());
	}

	// Tests ------------------------------------------------------------------
	
	private static final int DIVISIONS_IN_HOUR = 2;
	
	private EventLayoutComputer dayModel;
	private List expectedEvents;
	private CalendarableItem[][] eventLayout; 
		
	protected void setUp() throws Exception {
		super.setUp();
		dayModel = new EventLayoutComputer(DIVISIONS_IN_HOUR);
		expectedEvents = new ArrayList();
	}

	public void test_getEventLayout_NoEventsInDay() throws Exception {
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		assertExpectedColumns(1);
		
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
	
	public void test_getEventLayout_OneEventNoSpan() throws Exception {
		CalendarableItem c = addCalendarable(time(8, 00), time(8, 30), "One event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(1);
		assertEventInColumnInPositions(event(0), 0, new int[] {16});
		assertBounds(c, new Point(0, 16), new Point(0, 16));
	}

	public void test_getEventLayout_TwoEventsOneIsAllDaySpan3FullBlocks() throws Exception {
		CalendarableItem c = addCalendarable(time(8, 00), time(9, 30), "One event, 3 timeslots");
		addCalendarable("All-day-event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(1);
		assertEventInColumnInPositions(event(1), 0, new int[] {16, 18});
		assertBounds(c, new Point(0, 16), new Point(0, 18));
	}

	public void test_getEventLayout_OneEventSpanWithPartialBlock() throws Exception {
		addCalendarable(time(00, 00), time(0, 31), "One event, 3 timeslots");
		eventLayout = dayModel.computeEventLayout(expectedEvents);

		assertExpectedColumns(1);
		assertEventInColumnInPositions(event(0), 0, new int[] {0, 1});
	}
	
	public void test_getEventLayout_OneEventStartAndEndTimesSame() throws Exception {
		addCalendarable(time(01, 00), time(01, 00), "One event, same start and end times");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(1);
		assertEventInColumnInPositions(event(0), 0, new int[] {2});
	}
	
	public void test_getEventLayout_OneEventEndTimeBeforeStartTime() throws Exception {
		addCalendarable(time(01, 00), time(00, 30), "One event, end time before start time");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(1);
		assertEventInColumnInPositions(event(0), 0, new int[] {2});
	}
	
	public void test_getEventLayout_OneDayTwoSequentialEventsInputUnsorted() throws Exception {
		addCalendarable(time(01, 30), time(2, 00), "Second event");
		addCalendarable(time(01, 00), time(1, 30), "First event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(1);
		
		assertEventInColumnInPositions(event(0), 0, new int[] {2});
		assertEventInColumnInPositions(event(1), 0, new int[] {3});
	}
	
	public void test_getEventLayout_OneDayTwoEventsTwoColumnsInputUnsorted() throws Exception {
		addCalendarable(time(01, 30), time(2, 00), "Second event");
		addCalendarable(time(01, 00), time(2, 00), "First event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(2);
		
		assertEventInColumnInPositions(event(0), 0, new int[] {2, 3});
		assertEventNotInColumn(event(0), 1);
		
		assertEventInColumnInPositions(event(1), 1, new int[] {3});
		assertEventNotInColumn(event(1), 0);
	}

	public void test_getEventLayout_OneDay4EventsThreeColumnsInputSorted() throws Exception {
		addCalendarable(time(01, 00), time(3, 00), "First event");
		addCalendarable(time(01, 30), time(3, 00), "Second event");
		addCalendarable(time(02, 00), time(4, 00), "Third event");
		CalendarableItem c = addCalendarable(time(03, 00), time(4, 00), "Fourth event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(3);
		
		assertEventInColumnInPositions(event(0), 0, new int[] {2, 5});
		assertEventNotInColumn(event(0), 1);
		assertEventNotInColumn(event(0), 2);

		assertEventNotInColumn(event(1), 0); 
		assertEventInColumnInPositions(event(1), 1, new int[] {3, 5});
		assertEventNotInColumn(event(1), 2);
		
		assertEventNotInColumn(event(2), 0);
		assertEventNotInColumn(event(2), 1);
		assertEventInColumnInPositions(event(2), 2, new int[] {4, 7});

		assertEventInColumnInPositions(event(3), 0, new int[] {6, 7});
		assertEventInColumnInPositions(event(3), 1, new int[] {6, 7});
		assertEventNotInColumn(event(3), 2);
		assertBounds(c, new Point(0, 6), new Point(1, 7));
	}

	public void test_getEventLayout_OneDay4EventsThreeColumnsInputSortedWithAllDayEvents() throws Exception {
		addCalendarable(time(01, 00), time(3, 00), "First event");
		addCalendarable("ad1");
		addCalendarable(time(01, 30), time(3, 00), "Second event");
		addCalendarable(time(02, 00), time(4, 00), "Third event");
		addCalendarable("ad2");
		CalendarableItem c = addCalendarable(time(03, 00), time(4, 00), "Fourth event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(3);
		
		assertEventInColumnInPositions(event(2), 0, new int[] {2, 5});
		assertEventNotInColumn(event(2), 1);
		assertEventNotInColumn(event(2), 2);

		assertEventNotInColumn(event(3), 0); 
		assertEventInColumnInPositions(event(3), 1, new int[] {3, 5});
		assertEventNotInColumn(event(3), 2);
		
		assertEventNotInColumn(event(4), 0);
		assertEventNotInColumn(event(4), 1);
		assertEventInColumnInPositions(event(4), 2, new int[] {4, 7});

		assertEventInColumnInPositions(event(5), 0, new int[] {6, 7});
		assertEventInColumnInPositions(event(5), 1, new int[] {6, 7});
		assertEventNotInColumn(event(5), 2);
		assertBounds(c, new Point(0, 6), new Point(1, 7));
	}

	public void test_getEventLayout_OneDay6EventsFourColumnsInputSorted() throws Exception {
		addCalendarable(time(01, 00), time(3, 00), "First event");
		addCalendarable(time(01, 30), time(3, 00), "Second event");
		addCalendarable(time(02, 00), time(3, 00), "Third event");
		addCalendarable(time(02, 30), time(5, 00), "Fourth event");
		addCalendarable(time(03, 00), time(5, 00), "Fifth event");
		addCalendarable(time(04, 00), time(5, 00), "Sixth event");
		eventLayout = dayModel.computeEventLayout(expectedEvents);
		
		assertExpectedColumns(4);
		
		assertEventInColumnInPositions(event(0), 0, new int[] {2, 5});
		assertEventNotInColumn(event(0), 1);
		assertEventNotInColumn(event(0), 2);
		assertEventNotInColumn(event(0), 3);

		assertEventNotInColumn(event(1), 0); 
		assertEventInColumnInPositions(event(1), 1, new int[] {3, 5});
		assertEventNotInColumn(event(1), 2);
		assertEventNotInColumn(event(1), 3);
		
		assertEventNotInColumn(event(2), 0);
		assertEventNotInColumn(event(2), 1);
		assertEventInColumnInPositions(event(2), 2, new int[] {4, 5});
		assertEventNotInColumn(event(2), 3);

		assertEventNotInColumn(event(3), 0);
		assertEventNotInColumn(event(3), 1);
		assertEventNotInColumn(event(3), 2);
		assertEventInColumnInPositions(event(3), 3, new int[] {5, 9});

		assertEventInColumnInPositions(event(4), 0, new int[] {6, 9});
		assertEventNotInColumn(event(4), 1);
		assertEventNotInColumn(event(4), 2);
		assertEventNotInColumn(event(4), 3);

		assertEventNotInColumn(event(5), 0);
		assertEventInColumnInPositions(event(5), 1, new int[] {8, 9});
		assertEventInColumnInPositions(event(5), 2, new int[] {8, 9});
		assertEventNotInColumn(event(5), 3);
	}

}

