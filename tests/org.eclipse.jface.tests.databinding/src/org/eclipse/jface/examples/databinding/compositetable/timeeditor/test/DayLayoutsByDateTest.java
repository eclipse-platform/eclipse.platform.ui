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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayLayout;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayLayoutsByDate;

/**
 * @since 3.2
 *
 */
public class DayLayoutsByDateTest extends TestCase {

	private static final int NUMBER_OF_DAYS = 3;
	private DayLayoutsByDate dayLayoutsByDate;
	private DayLayout expectedDayLayout;
	private Date newDate;
	private static final Date START_DATE = date(2006, 02, 02);
	private static final Date END_DATE = date(2006, 02, 04);

	private static Date date(int year, int month, int day) {
		GregorianCalendar gc = new GregorianCalendar(year, month, day);
		return gc.getTime();
	}

	protected void setUp() throws Exception {
		super.setUp();
		dayLayoutsByDate = new DayLayoutsByDate(START_DATE, NUMBER_OF_DAYS);
		expectedDayLayout = new DayLayout(null, null);
	}
	
	/*
	 *	Tests:
	 * 		put
	 * 			happy day
	 * 			throws IAE
	 * 		get
	 * 			happy day
	 * 			returns null
	 *  
	 */
	public void testDayLayoutsByDate_putAndGetHappyDayOneDate() throws Exception {
		newDate = (Date)START_DATE.clone();

		try {
			dayLayoutsByDate.put(newDate, expectedDayLayout);
			//success
		} catch(IllegalArgumentException iae) {
			fail("new date is within time window");
		}
		
		assertEquals("correct daylayout",  expectedDayLayout, dayLayoutsByDate.get(newDate) );
	}

	public void testDayLayoutsByDate_putAndGetHappyDayTwoDates() throws Exception {
		newDate = (Date)START_DATE.clone();
		
		dayLayoutsByDate.put((Date)START_DATE.clone(), new DayLayout(null, null));
		
		dayLayoutsByDate.put(newDate, expectedDayLayout);
				
		assertEquals("correct daylayout",  expectedDayLayout, dayLayoutsByDate.get(newDate));
	}
	
	public void test_putDateLessThanStartDate() throws Exception {
		newDate = date(2006, 2, 1);
		
		try {
			dayLayoutsByDate.put(newDate, expectedDayLayout);
			fail("new date is outside time window");
		} catch(IllegalArgumentException iae) {
			//success
		}
	}
	
	public void test_putDateEqualsEndDate() throws Exception {
		newDate = date(2006, 2, 4);
		
		try {
			dayLayoutsByDate.put(newDate, expectedDayLayout);
			//success
		} catch(IllegalArgumentException iae) {
			fail("new date is within time window");
		}
	}

	public void test_putDateGreaterThanEndDate() throws Exception {
		newDate = date(2006, 2, 5);
		
		try {
			dayLayoutsByDate.put(newDate, expectedDayLayout);
			fail("new date is outside time window");
		} catch(IllegalArgumentException iae) {
			//success
		}
	}

	public void test_putNullDate() throws Exception {
		newDate = null;
		
		try {
			dayLayoutsByDate.put(newDate, expectedDayLayout);
			fail("null date should try exception");
		} catch(IllegalArgumentException iae) {
			//success
		}
	}
	
	public void test_adjustStartDateReturnsInvalidatedDatesBeforeStartDate() throws Exception {
		dayLayoutsByDate.put(START_DATE, expectedDayLayout);
		dayLayoutsByDate.put(date(2006, 02, 03), new DayLayout(null, null));
		List invalidatedDates = dayLayoutsByDate.adjustStartDate(date(2006, 02, 03));
		
		assertEquals("Expected one day to be removed", 1, invalidatedDates.size());
		assertEquals("Expected expectedDayLayout to be removed", expectedDayLayout, invalidatedDates.get(0));
		assertNull("Get of removed date should return null", dayLayoutsByDate.get(START_DATE));
	}
	
	public void test_adjustStartDateReturnsInvalidateDatesAfterEndDate() throws Exception {
		dayLayoutsByDate.put(END_DATE, expectedDayLayout);
		dayLayoutsByDate.put(date(2006, 02, 03), new DayLayout(null, null));
		List invalidatedDates = dayLayoutsByDate.adjustStartDate(date(2006, 02, 01));
		
		assertEquals("Expected one day to be removed", 1, invalidatedDates.size());
		assertEquals("Expected expectedDayLayout to be removed", expectedDayLayout, invalidatedDates.get(0));
		assertNull("Get of removed date should return null", dayLayoutsByDate.get(END_DATE));
	}
}

