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

package org.eclipse.jface.tests.databinding;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor;


/**
 * @since 3.3
 */
public class EventEditorObservableLazyDataRequestorEventCacheTest 
	extends EventEditorObservableLazyDataRequestorTest {

	/**
	 * 
	 */
	private void setup() {
		

	}
	
	public void test_add_singleDayEvent() throws Exception {
		Event event = new Event(time(4, 1, 5, 45), time(4, 1, 11, 45), "standup comedy");
		EventEditorObservableLazyDataRequestor.EventCache cache = 
			new EventEditorObservableLazyDataRequestor.EventCache();
		cache.add(event);
		assertEventForDate(event.getStartTime(), event, cache);
	}
	
	public void test_add_multiDayEvent() throws Exception {
		Event event = new Event(time(4, 1, 5, 45), time(4, 4, 11, 45), "standup tragedy");
		EventEditorObservableLazyDataRequestor.EventCache cache = 
			new EventEditorObservableLazyDataRequestor.EventCache();
		cache.add(event);
		Date dateToTest = event.getStartTime();
		assertEventForDate(dateToTest, event, cache);
		dateToTest = nextDay(dateToTest);
		assertEventForDate(dateToTest, event, cache);
		dateToTest = nextDay(dateToTest);
		assertEventForDate(dateToTest, event, cache);
	}

	/**
	 * @param date TODO
	 * @param event
	 * @param cache
	 */
	private void assertEventForDate(Date date, Event event, EventEditorObservableLazyDataRequestor.EventCache cache) {
		List events = cache.get(date);
		assertTrue("should contain event", events.contains(event));
	}
	
	public void test_flush() throws Exception {
		
	}
}
