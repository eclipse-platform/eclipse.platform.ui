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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;

/**
 * @since 3.2
 *
 */
public class CalendarableModel_testRefreshDate extends TestCase {
	
	/**
	 * Makes sure that the text properties of the Calenderable objects in the
	 * model match the data in the string array and that the number of days
	 * in the model match the dimension of the data array.
	 * 
	 * @param cm Model
	 * @param data The data to validate.
	 */
	private void verifyModel(CalendarableModel cm, String[][] data) {
		assertEquals("number of days equal", cm.getNumberOfDays(), data.length);
		for (int day = 0; day < data.length; day++) {
			List events = cm.getCalendarableEvents(day);
			for (int element = 0; element < data[day].length; element++) {
				assertEquals("Event " + element + ", day " + day + "equal", data[day][element], ((Calendarable)events.get(element)).getText());
			}
		}
	}

	private CalendarableModel testModelLoading(String[][] data) {
		CMClientFixture cmf = new CMClientFixture(data);
		
		CalendarableModel cm = new CalendarableModel();
		cm.setTimeBreakdown(data.length, 4);
		cm.setDayEventCountProvider(cmf.eventCountProvider);
		cm.setEventContentProvider(cmf.eventContentProvider);
		cm.setStartDate(cmf.startDate);
		
		verifyModel(cm, data);
		return cm;
	}
	
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
	
	
}
