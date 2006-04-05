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

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;

/**
 * @since 3.2
 *
 */
public class CalendarableModel_testInit extends TestCase {
	final boolean[] initCalled = new boolean[] {false};
	private CalendarableModel cm;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cm = new CalendarableModel();
	}
	
	public void testInitCalledAfterAllSettersSet() throws Exception {
		EventCountProvider ecp = new EventCountProvider() {
			public int getNumberOfEventsInDay(Date day) {
				initCalled[0] = true;
				return 0;
			};
		};
		
		cm.setDayEventCountProvider(ecp);
		assertNoRefreshCalled();
		cm.setTimeBreakdown(1, 2);
		assertNoRefreshCalled();
		cm.setEventContentProvider(new EventContentProvider() {
			public void refresh(Date day, Calendarable[] controls) {
			}});
		assertNoRefreshCalled();
		cm.setStartDate(new Date());
		assertRefreshCalled();
	}

	private void assertNoRefreshCalled() {
		assertFalse("no refresh yet", initCalled[0]);
	}
	
	private void assertRefreshCalled() {
		assertTrue("refresh already", initCalled[0]);
	}
	
	public void testSetTimeBreakdown_numberOfDivisionsInHourNotSet() throws Exception {
		try {
			cm.setTimeBreakdown(1, 0);
			fail("IllegalArgumentException expected");
		} catch(IllegalArgumentException e) {
			//success
		}
	}

	public void testSetTimeBreakdown_numberOfDaysNotSet() throws Exception {
		try {
			cm.setTimeBreakdown(0, 1);
			fail("IllegalArgumentException expected");
		} catch(IllegalArgumentException e) {
			//success
		}
	}
}
