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

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;

/**
 * @since 3.2
 *
 */
public class CalendarableModel_testRefreshDate extends TestCase {
	
	// Test fixtures ----------------------------------------------------------
	
	private void verifyModel(CalendarableModel cm, String[][] data) {
		assertEquals("number of days equal", cm.getNumberOfDays(), data.length);
		for (int day = 0; day < data.length; day++) {
			List events = cm.getCalendarableEvents(day);
			assertEquals("number of events equal", events.size(), data[day].length);
			for (int element = 0; element < data[day].length; element++) {
				assertEquals("Event " + element + ", day " + day + "equal", data[day][element], ((Calendarable)events.get(element)).getText());
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
	
	private void setupModelwithFixtureAndData(CalendarableModel cm, CMClientFixture cmf, String[][] data) {
		cm.setTimeBreakdown(data.length, 4);
		cm.setDayEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
		
		verifyModel(cm, data);
	}

	private CalendarableModel testModelLoading(String[][] data) {
		CMClientFixture cmf = new CMClientFixture(data);
		CalendarableModel cm = new CalendarableModel();
		setupModelwithFixtureAndData(cm, cmf, data);
		return cm;
	}
	
	private Date getNextDate(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		gc.add(Calendar.DATE, 1);
		return gc.getTime();
	}
	
	// Tests ------------------------------------------------------------------
	
	public void testOneDayOneEvent() throws Exception {
		testModelLoading(new String[][] {
				{"One event"}
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
		CMClientFixture cmf = new CMClientFixture(data);
		CalendarableModel cm = new CalendarableModel();
		setupModelwithFixtureAndData(cm, cmf, data);
		
		data[0] = new String[] {"1", "2", "3", "4", "5"};
		cm.refresh(cmf.startDate);
		
		verifyModel(cm, data);
	}

	public void testDecreaseNumberOfEventsInDay() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"}
		};
		CMClientFixture cmf = new CMClientFixture(data);
		CalendarableModel cm = new CalendarableModel();
		setupModelwithFixtureAndData(cm, cmf, data);

		data[0] = new String[] {"1"};
		cm.refresh(cmf.startDate);
		
		verifyModel(cm, data);
	}
	
	public void testRefreshDateOutsideDisplayedRange() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"}
		};
		CMClientFixture cmf = new CMClientFixture(data);
		CalendarableModel cm = new CalendarableModel();
		setupModelwithFixtureAndData(cm, cmf, data);

		data[0] = new String[] {"1"};
		
		cm.refresh(getNextDate(cmf.startDate));  // This refresh should not occur
		verifyModelShouldFail(cm, data);
	}

	public void testRefreshSecondDay() throws Exception {
		String[][] data = new String[][] {
				{"1", "2", "3"},
				{"0", "3", "6", "9"}
		};
		CMClientFixture cmf = new CMClientFixture(data);
		CalendarableModel cm = new CalendarableModel();
		setupModelwithFixtureAndData(cm, cmf, data);

		data[1] = new String[] {"42"};

		cm.refresh(cmf.startDate);  // This refresh should do nothing (the data for that day didn't change)
		verifyModelShouldFail(cm, data);
		cm.refresh(getNextDate(cmf.startDate));  // This refresh should
		verifyModel(cm, data);
	}

}
