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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;

/**
 * @since 3.2
 *
 */
public class CalendarableModel_testRefreshResults extends TestCase {
	
	// Test fixtures ----------------------------------------------------------
	
	private CMClientFixture cmf;
	private CalendarableModel cm;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		cmf = new CMClientFixture();
		cm = new CalendarableModel();
	}
	
	private void setupModel(String[][] data) {
		cmf.setData(data);
		cm.setTimeBreakdown(data.length, 4);
		cm.setEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
		
		verifyModel(cm, data);
	}
	
	private void verifyModel(CalendarableModel cm, String[][] data) {
		assertEquals("number of days equal", cm.getNumberOfDays(), data.length);
		for (int day = 0; day < data.length; day++) {
			List events = cm.getCalendarableItems(day);
			assertEquals("number of events equal", events.size(), data[day].length);
			for (int element = 0; element < data[day].length; element++) {
				assertEquals("Event " + element + ", day " + day + "equal", data[day][element], ((CalendarableItem)events.get(element)).getText());
			}
		}
	}

	private void verifyModelShouldFail(CalendarableModel cm, String[][] data) throws AssertionFailedError {
		try {
			// This should throw an assertion failed error
			verifyModel(cm, data);
		} catch (AssertionFailedError e) {
			// Make sure we got the correct assertion failure
			if (e.getMessage().indexOf("number of events equal") == -1) {
				throw e;
			}
			// Success
		}
	}
	
	private CalendarableModel testModelLoading(String[][] data) {
		CalendarableModel cm = new CalendarableModel();
		setupModel(data);
		return cm;
	}
	
	private Date getNextDate(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		gc.add(Calendar.DATE, 1);
		return gc.getTime();
	}
	
	private CalendarableItem[] getAllDayEvents(String[][] data) {
		setupModel(data);
		CalendarableItem[] allDayEvents = cm.getAllDayCalendarables(0);
		return allDayEvents;
	}
	
	// Tests ------------------------------------------------------------------
	
	public void testOneDayOneEvent() throws Exception {
		testModelLoading(new String[][] {
				{"1"}
		});
	}

	public void testTwoDaysTwoEvents() throws Exception {
		testModelLoading(new String[][] {
				{"1", "2"},
				{"3", "4"}
		});
	}
	
	public void testOneDayZeroEvents() throws Exception {
		testModelLoading(new String[][] {
				{}
		});
	}
	
	public void testIncreaseNumberOfEventsInDay() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"}
		};
		setupModel(data);
		
		data[0] = new String[] {"1", "2", "3", "4", "5"};
		cm.refresh(cmf.startDate);
		
		verifyModel(cm, data);
	}

	public void testDecreaseNumberOfEventsInDay() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"}
		};
		setupModel(data);

		data[0] = new String[] {"1"};
		cm.refresh(cmf.startDate);
		
		verifyModel(cm, data);
	}
	
	public void testRefreshDateOutsideDisplayedRange() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"}
		};
		setupModel(data);

		data[0] = new String[] {"1"};
		
		cm.refresh(getNextDate(cmf.startDate));  // This refresh should not occur
		verifyModelShouldFail(cm, data);
	}

	public void testRefreshSecondDay() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"},
				{"0", "3", "6", "9"}
		};
		setupModel(data);

		data[1] = new String[] {"42"};

		cm.refresh(cmf.startDate);  // This refresh should do nothing (the data for that day didn't change)
		verifyModelShouldFail(cm, data);
		cm.refresh(getNextDate(cmf.startDate));  // This refresh should
		verifyModel(cm, data);
	}

	// Number of all day event row tests --------------------------------------
	
	public void testComputeNumberOfAllDayEventRows_OneAllDayEvent() throws Exception {
		String[][] data = new String[][] {
				{"1", "3", "3"},
				{"0", "3", "6", "9"}
		};
		setupModel(data);
		
		assertEquals("max one event", 1, cm.computeNumberOfAllDayEventRows());
	}

	public void testComputeNumberOfAllDayEventRows_TwoAllDayEvent() throws Exception {
		String[][] data = new String[][] {
				{"4", "1", "1"},
				{"0", "3", "6", "9"}
		};
		setupModel(data);
		
		assertEquals("max two event", 2, cm.computeNumberOfAllDayEventRows());
	}

	public void testComputeNumberOfAllDayEventRows_ThreeAllDayEvent() throws Exception {
		String[][] data = new String[][] {
				{"4", "1", "1"},
				{"1", "1", "1", "9"}
		};
		setupModel(data);
		
		assertEquals("max three event", 3, cm.computeNumberOfAllDayEventRows());
	}

	public void testComputeNumberOfAllDayEventRows_ZeroAllDayEvent() throws Exception {
		String[][] data = new String[][] {
				{"0", "2", "4"},
				{"0", "3", "5", "9"}
		};
		setupModel(data);
		
		assertEquals("no all day events", 0, cm.computeNumberOfAllDayEventRows());
	}
	
	// Start time -------------------------------------------------------------
	
	public void testComputeStartHour_UsingDefault() throws Exception {
		String[][] data = new String[][] {
				{"9", "22", "8"},
				{"21", "11", "14", "16"}
		};
		setupModel(data);
		
		assertEquals("8 am start", 8, cm.computeStartHour());
	}
	
	public void testComputeStartHour_Midnight() throws Exception {
		String[][] data = new String[][] {
				{"9", "22", "8"},
				{"21", "11", "0", "16"}
		};
		setupModel(data);
		
		assertEquals("midnight start", 0, cm.computeStartHour());
	}
	
	public void testComputeStartHour_7am() throws Exception {
		String[][] data = new String[][] {
				{"9", "22", "8"},
				{"21", "11", "7", "16"}
		};
		setupModel(data);
		
		assertEquals("midnight start", 7, cm.computeStartHour());
	}
	
	public void testComputeStartHour_7amWithAllDayEvents() throws Exception {
		String[][] data = new String[][] {
				{"9", "1", "8"},
				{"21", "11", "7", "16"}
		};
		setupModel(data);
		
		assertEquals("midnight start", 7, cm.computeStartHour());
	}
	
	// Test all-day event methods ---------------------------------------------
	
	public void testGetAllDayEvents_noEvents() throws Exception {
		String[][] data = new String[][] {
				{"2", "3"}
		};
		setupModel(data);
		
		assertEquals("Should find no all-day events", 0, cm.getAllDayCalendarables(0).length);
	}
	
	public void testGetAllDayEvents_oneEvent() throws Exception {
		String[][] data = new String[][] {
				{"1First", "2", "3"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		
		assertEquals("Should find one all-day event", 1, allDayEvents.length);
		assertEquals("Found 1First", "1First", allDayEvents[0].getText());
	}

	public void testGetAllDayEvents_threeEvents() throws Exception {
		String[][] data = new String[][] {
				{"0", "1First", "1Second", "2", "1Third"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		
		assertEquals("Should find three all-day event", 3, allDayEvents.length);
		assertEquals("Found 1First", "1First", allDayEvents[0].getText());
		assertEquals("Found 1Third", "1Third", allDayEvents[2].getText());
	}
	
	// Test methods that integrate the eventLayout and everything else --------
	
	public void testFindAllDayCalendarable_ForwardNoData() throws Exception {
		String[][] data = new String[][] {
				{"5", "5", "2", "3", "5", "5"}
		};
		setupModel(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, true, null);
		assertNull("No calendarables forward", result);
	}

	public void testFindAllDayCalendarable_BackwardNoData() throws Exception {
		String[][] data = new String[][] {
				{"5", "5", "2", "3", "5", "5"}
		};
		getAllDayEvents(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, false, null);
		assertNull("No calendarables forward", result);
	}
	
	public void testFindAllDayCalendarable_BackwardButAtFirst() throws Exception {
		String[][] data = new String[][] {
				{"1First", "1Second", "2", "3", "1Third", "1Fourth"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, false, allDayEvents[0]);
		assertNull("No Calendarables backward from first", result);
	}
	
	public void testFindAllDayCalendarable_ForwardAtLast() throws Exception {
		String[][] data = new String[][] {
				{"1First", "1Second", "2", "3", "1Third", "1Fourth"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, true, allDayEvents[3]);
		assertNull("No calendarables forward", result);
	}
	
	public void testFindAllDayCalendarable_ForwardWithSelection() throws Exception {
		String[][] data = new String[][] {
				{"1First", "1Second", "2", "3", "1Third", "1Fourth"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, true, allDayEvents[1]);
		assertEquals("Should find third event", allDayEvents[2], result);
	}
	
	public void testFindAllDayCalendarable_BackwardWithSelection() throws Exception {
		String[][] data = new String[][] {
				{"1First", "1Second", "2", "3", "1Third", "1Fourth"}
		};
		CalendarableItem[] allDayEvents = getAllDayEvents(data);
		CalendarableItem result = cm.findAllDayCalendarable(0, false, allDayEvents[1]);
		assertEquals("Should find first event", allDayEvents[0], result);
	}
	
}
