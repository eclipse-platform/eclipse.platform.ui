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
import java.util.Date;
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

	/**
	 * 
	 */
	private static final int DIVISIONS_IN_HOUR = 2;
	
	private IEventEditor eventEditor = new EventEditorFixture(DIVISIONS_IN_HOUR);

	public void test_getColumnsForEvents_NoEventsInDay() throws Exception {
		DayModel dayModel = new DayModel(eventEditor);
		List events = new ArrayList();
		Calendarable[][] models = dayModel.getColumnsForEvents(events);
		
		for (int i = 0; i < models.length; i++) {
			for (int j = 0; j < models[i].length; j++) {
				assertNull(models[i][j]);
			}
		}
	}
}
