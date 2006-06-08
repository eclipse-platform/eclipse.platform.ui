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

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.test.CalendarableModel_TestTimedFindMethods.Event;

/**
 * Fixture for testing CalendarableModel
 */
public class CMTimedEventFixture {
	
	private CalendarableModel_TestTimedFindMethods.Event[][] data;

	/**
	 * Hard-code a start date that's easy to do arithmetic with
	 */
	public final Date startDate;

	public CMTimedEventFixture(Event[][] data) {
		this.data = data;
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.MONTH, 1);
		gc.set(Calendar.YEAR, 2006);
		gc.set(Calendar.DATE, 1);
		startDate = gc.getTime();
	}
	
	private int getOffset(Date day) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(day);
		return gc.get(Calendar.DATE) - 1;
	}
	
	/**
	 */
	private class ECTP extends EventCountProvider {
		public int getNumberOfEventsInDay(Date day) {
			return data[getOffset(day)].length;
		}
	}

	/**
	 */
	private class ECNP extends EventContentProvider {
		public void refresh(Date day, CalendarableItem[] controls) {
			if (controls.length != data[getOffset(day)].length) {
				throw new RuntimeException("Number of elements to fill != amount of data we've got");
			}
			int dateOffset = getOffset(day);
			for (int i = 0; i < controls.length; i++) {
				controls[i].setText(data[dateOffset][i].text);
				controls[i].setAllDayEvent(data[dateOffset][i].allDay);
				controls[i].setStartTime(data[dateOffset][i].startTime);
				controls[i].setEndTime(data[dateOffset][i].endTime);
			}
		}
	}
	
	/**
	 */
	public ECTP eventCountProvider = new ECTP();
	
	/**
	 */
	public ECNP eventContentProvider = new ECNP();

	/**
	 */
	public int getNumberOfDays() {
		return data.length;
	}
	


}
