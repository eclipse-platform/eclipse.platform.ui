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

package org.eclipse.jface.tests.databinding;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.jface.internal.databinding.internal.LazyListBinding;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor.NewObject;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;

/**
 * This is basically two tests in one.  It's an integration test that makes
 * sure that we can successfully bind
 * @since 3.2
 */
public class EventEditorObservableLazyDataRequestorTest extends TestCase {

	private Date startDate = new Date();
	private IEventEditor editor;
	private WritableList model;
	private Binding binding;
	private DataBindingContext dbc;

	private DataBindingContext getDBC() {
		DataBindingContext dbc = new DataBindingContext();
		dbc.addBindingFactory(new IBindingFactory() {
			public Binding createBinding(DataBindingContext dataBindingContext, IObservable target, IObservable model, BindSpec bindSpec) {
				if (bindSpec == null) {
					bindSpec = new BindSpec();
				}
				return new LazyListBinding(dataBindingContext, target, (IObservableList) model, bindSpec);
			}
		});
		return dbc;
	}

	private static class EventEditorStub extends EventEditor {
		public CalendarableModel model() {
			return model;
		}
	}
	
	private static class Event {
		public boolean allDay = false;
		public Date startTime;
		public Date endTime;
		public String description;
		
		public Event(Date startTime, Date endTime, String description) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.description = description;
		}

		public Event(String description) {
			this.allDay = true;
			this.description = description;
		}
	}
	
	private Date time(int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		return gc.getTime();
	}
	
	private Event[][] testData = new Event[][] {
			{new Event(time(5, 45), time(9, 45), "Stand-up meeting"),
				new Event(time(11, 00), time(12, 15), "Meet with customer")},
			{},
			{},
			{new Event("Nat. Conference"),
				new Event(time(7, 50), time(9, 00), "Stand-up meeting"),
				new Event(time(10, 15), time(12, 00), "Work on prototype")},
			{new Event("Nat. Conference"),
				new Event("Field trip to PC HQ"),
				new Event(time(8, 30), time(9, 30), "Stand-up meeting"),
				new Event(time(10, 00), time(13, 15), "Meet with customer"),
				new Event(time(12, 45), time(14, 15), "RC1 due"),
				new Event(time(13, 45), time(14, 15), "Way too much work"),
				new Event(time(10, 00), time(13, 30), "Callisto meeting")},
			{new Event("Nat. Conference")},
			{new Event(time(8, 30), time(11, 30), "Stand-up meeting"),
				new Event(time(10, 00), time(12, 15), "Meet with customer1"),
				new Event(time(11, 45), time(12, 15), "Meet with customer2"),
				new Event(time(11, 00), time(11, 15), "Meet with customer3")},
			{},
			{new Event(time(8, 50), time(9, 00), "Stand-up meeting"),
				new Event(time(10, 15), time(12, 00), "Work on prototype")},
			{new Event(time(8, 45), time(9, 45), "Stand-up meeting"),
				new Event(time(11, 00), time(12, 15), "Meet with customer")},
			{},
			{},
			{new Event(time(8, 12), time(9, 00), "Stand-up meeting"),
				new Event(time(10, 15), time(12, 00), "Work on prototype")},
			{},
			{},
			{new Event(time(8, 30), time(11, 30), "Stand-up meeting"),
				new Event(time(10, 00), time(12, 15), "Meet with customer"),
				new Event(time(11, 45), time(12, 15), "Meet with customer"),
				new Event(time(11, 00), time(2, 45), "Meet with customer")},
			{new Event(time(9, 50), time(9, 00), "Stand-up meeting"),
				new Event(time(10, 15), time(12, 00), "Work on prototype")},
			{},
	};
	
    private Date nextDay(Date refreshDate) {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(refreshDate);
    	gc.add(Calendar.DATE, 1);
    	return gc.getTime();
    }
    
	private List testDataList;

	/*
	 * FIXME: Need to convert testData into something that supports mult-day
	 * events.  Need to convert testData into a sorted List rather than a
	 * two-dimensional array or Map.
	 */
	private void loadTestDataIntoList() {
		testDataList = new LinkedList();
		Date loadDate = startDate;
		for (int day=0; day < testData.length; ++day) {
			if (testData[day].length > 0) {
				for (int event = 0; event < testData[day].length; event++) {
					testDataList.add(testData[day][event]);
				}
			}
			loadDate = nextDay(loadDate);
		}
	}
	
	private void assertModelMatchesEvents() {
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		loadTestDataIntoList();
		editor = new EventEditor();
	}

	private LazyInsertDeleteProvider insertDeleteProvider = new LazyInsertDeleteProvider() {
		public NewObject insertElementAt(int positionHint, Object initializationData) {
			return null;
		}
		
		public boolean deleteElementAt(int position) {
			return false;
		}
	};
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#add(int, java.lang.Object)}.
	 */
	public void testInsert() {
		fail("Not yet implemented");
	}
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#remove(int)}.
	 */
	public void testRemove() {
		fail("Not yet implemented");
	}
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#dispose()}.
	 */
	public void testDispose() {
		fail("Not yet implemented");
	}

}
