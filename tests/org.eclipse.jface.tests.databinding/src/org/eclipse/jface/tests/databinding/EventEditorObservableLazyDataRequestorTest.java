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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.observable.list.WritableList;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.jface.examples.databinding.compositetable.day.NewEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorBindingDescription;
import org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestorFactory;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor.NewObject;

/**
 * This is basically two tests in one.  It's an integration test that makes
 * sure that we can successfully bind EventEditors to JavaBean-style List 
 * objects.  However, since all of the other layers in this binding are also
 * unit-tested, it is also a unit test of the 
 * EventEditorObservableLazyDataRequestor.
 * 
 * @since 3.2
 */
public class EventEditorObservableLazyDataRequestorTest extends TestCase {

	private EventEditorStub editor;
	private DataBindingContext dbc;

	private DataBindingContext getDBC() {
		DataBindingContext dbc = new DataBindingContext();
		dbc.addBindingFactory(new DefaultBindingFactory());
		dbc.addObservableFactory(new EventEditorObservableLazyDataRequestorFactory());
		dbc.addObservableFactory(new BeanObservableFactory(dbc, null, null));
		dbc.addBindSupportFactory(new DefaultBindSupportFactory());
		return dbc;
	}

	private static class EventEditorStub extends EventEditor {
		public CalendarableModel model() {
			return model;
		}
	}
	
	protected static class Event extends ModelObject {
		public boolean allDay = false;
		public Date startTime;
		public Date endTime;
		public String description;
		
		public Event(Date startTime, Date endTime, String description) {
			this(startTime, endTime, description, false);
		}

		public Event(Date startTime, Date endTime, String description, boolean isAllDay) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.description = description;
			this.allDay = isAllDay;
		}

		public void setDescription(String string) {
			String oldValue = this.description;
			description = string;
			firePropertyChange("description", oldValue, string);
		}

		public boolean isAllDay() {
			return allDay;
		}

		public void setAllDay(boolean allDay) {
			this.allDay = allDay;
		}

		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public String getDescription() {
			return description;
		}

	}
	
	protected Date time(int month, int day, int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.MONTH, month);
		gc.set(Calendar.DATE, day);
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return gc.getTime();
	}

	
	protected Date time(int hour, int minutes) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minutes);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return gc.getTime();
	}

	protected Date date(int month, int day) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.MONTH, month);
		gc.set(Calendar.DATE, day);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return gc.getTime();
	}

    protected Date nextDay(Date date) {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(date);
    	gc.add(Calendar.DATE, 1);
    	return gc.getTime();
    }
    
	private WritableList loadTestDataIntoList(Event[] testData) {
		WritableList testDataList = new WritableList();
		for (int event = 0; event < testData.length; event++) {
			testDataList.add(testData[event]);
		}
		return testDataList;
	}
	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		editor = new EventEditorStub();
		dbc = getDBC();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		dbc.dispose();
	}
	
	private CalendarableItem ci(Date startDate, Date startTime, Date endTime, String description) {
		return ci(startDate, startTime, endTime, description, false);
	}

	private CalendarableItem ci(Date startDate, String description) {
		return ci(startDate, startDate, startDate, description, true);
	}

	private CalendarableItem ci(Date startDate, Date startTime, Date endTime, String description, boolean isAllDay) {
		CalendarableItem result = new CalendarableItem(startDate);
		result.setStartTime(startTime);
		result.setEndTime(endTime);
		result.setText(description);
		result.setAllDayEvent(isAllDay);
		return result;
	}

	protected Date setToStartOfDay(Date rawDate) {
		GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(rawDate);
    	gc.set(Calendar.HOUR_OF_DAY, 0);
    	gc.set(Calendar.MINUTE, 0);
    	gc.set(Calendar.SECOND, 0);
    	gc.set(Calendar.MILLISECOND, 0);
    	return gc.getTime();
	}

	protected Date setToEndOfDay(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		gc.set(Calendar.HOUR_OF_DAY, 23);
		gc.set(Calendar.MINUTE, 59);
		gc.set(Calendar.SECOND, 59);
		gc.set(Calendar.MILLISECOND, 999);
		Date time = gc.getTime();
		return time;
	}
	
	private WritableList makeModel(final Event[] testData) {
		return loadTestDataIntoList(testData);
	}

	static class TestModel extends ModelObject {
		List testDataList;
		TestModel(List testData) {
			this.testDataList = testData;
		}

		public List getTestDataList() {
			return testDataList;
		}

		public void setTestDataList(List testDataList) {
			Object oldValue = this.testDataList;
			this.testDataList = testDataList;
			firePropertyChange("testDataList", oldValue, testDataList);
		}
	};
	
	private Property makeModel(List testData) {
		Object model = new TestModel(testData); 
		return new Property(model, "testDataList");
	}

	private void assertEditorState(
			EventEditorStub editor, 
			CalendarableItem[][] itemsInDay) {
		CalendarableModel cm = editor.model();
		for (int day=0; day < cm.getNumberOfDays(); ++day) {
			List calendarables = cm.getCalendarableItems(day);
			int itemInDay=0;
			assertEquals("Day " + day + ": list sizes same", itemsInDay[day].length, calendarables.size());
			for (Iterator calIter = calendarables.iterator(); calIter.hasNext();) {
				CalendarableItem item = (CalendarableItem) calIter.next();
				assertEquals("All-day", itemsInDay[day][itemInDay].isAllDayEvent(), item.isAllDayEvent());
				assertEquals("Text", itemsInDay[day][itemInDay].getText(), item.getText());
				if (item.isAllDayEvent()) {
					assertTrue("same day", isSameDay(itemsInDay[day][itemInDay].getStartTime(), item.getStartTime()));
				} else {
					assertEquals("Start time", itemsInDay[day][itemInDay].getStartTime(), item.getStartTime());
					assertEquals("End time", itemsInDay[day][itemInDay].getEndTime(), item.getEndTime());					
				}
				++itemInDay;
			}
		}
	}
	
	private boolean isSameDay(Date time1, Date time2) {
		GregorianCalendar gc1 =  new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();
		gc1.setTime(time1);
		gc2.setTime(time2);
		if (gc1.get(Calendar.YEAR) != gc2.get(Calendar.YEAR)) {
			return false;
		}		
		if (gc1.get(Calendar.MONTH) != gc2.get(Calendar.MONTH)) {
			return false;
		}		
		if (gc1.get(Calendar.DATE) != gc2.get(Calendar.DATE)) {
			return false;
		}
		return true;
	}

	private EventEditorBindingDescription makeBindingDescription() {
		return new EventEditorBindingDescription(
				editor, dbc, "startTime", "endTime", "allDay", "description", null, null);
	}
	

	// Tests here -------------------------------------------------------------

	public void test_oneDayEvent_onEditorStartDate() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg")};
		dbc.bind(editorBindDesc, makeModel(testData), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{},
				{},
				{},
				{}
		});
	}

	public void test_addingAndRemovingFromModel() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg")};
		WritableList model = makeModel(testData);
		dbc.bind(editorBindDesc, model, null);	
		Event event = new Event (time(5, 16, 5, 45), time(5, 16, 9, 45), "Stand-up mtg 2");
		model.add(event);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), time(9, 45), "Stand-up mtg")},
				{ci(date(5, 16), time(5, 45), time(9, 45), "Stand-up mtg 2")},
				{},
				{},
				{},
				{},
				{}
		});
		model.remove(event);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{},
				{},
				{},
				{}
		});		
	}

	public void test_oneDayOneEvent_notOnEditorStartDate() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 16, 5, 45), time(5, 16, 9, 45), "Stand-up mtg")};
		dbc.bind(editorBindDesc, makeModel(testData), null); 
		assertEditorState(editor, new CalendarableItem[][] {
				{},
				{ci(date(5, 16), time(5, 45), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{},
				{},
				{}
		});
	}

	public void test_threeDayOneEvent() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 17, 9, 45), "Stand-up mtg")};
		dbc.bind(editorBindDesc, makeModel(testData), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), setToEndOfDay(date(5, 15)), "Stand-up mtg")},
				{ci(date(5, 16), setToStartOfDay(date(5, 16)), setToEndOfDay(date(5, 16)), "Stand-up mtg")},
				{ci(date(5, 17), setToStartOfDay(date(5, 17)), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{},
				{}
		});
	}

	public void test_threeDayOneEvent_notOnEditorStartDate() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 14));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 17, 9, 45), "Stand-up mtg")};
		dbc.bind(editorBindDesc, makeModel(testData), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{},
				{ci(date(5, 15), time(5, 45), setToEndOfDay(date(5, 15)), "Stand-up mtg")},
				{ci(date(5, 16), setToStartOfDay(date(5, 16)), setToEndOfDay(date(5, 16)), "Stand-up mtg")},
				{ci(date(5, 17), setToStartOfDay(date(5, 17)), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{}
		});
	}


	public void test_oneDayOneAllDayEvent() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg", true)};
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		dbc.bind(editorBindDesc, makeModel(testData), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), "Stand-up mtg")},
				{},
				{},
				{},
				{},
				{},
				{}
		});
	}
	
	public void test_multiDayAllDayEvent() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 17, 9, 45), "three day mtg", true)};
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		dbc.bind(editorBindDesc, makeModel(testData), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), "three day mtg")},
				{ci(date(5, 16), "three day mtg")},
				{ci(date(5, 17), "three day mtg")},
				{},
				{},
				{},
				{}
		});
	}
	
	public void test_bindCalendarableDescription() throws Exception {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg")};
		List testDataList = loadTestDataIntoList(testData);
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		dbc.bind(editorBindDesc, makeModel(testDataList), null);
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), time(9, 45), "Stand-up mtg")},
				{},
				{},
				{},
				{},
				{},
				{}
		});
		Event event = (Event) testDataList.get(0);
		event.setDescription("The quick brown fox jumped over the lazy dog.");
		
		List calendarableEvents = editor.model.getCalendarableItems((0));
		CalendarableItem item = (CalendarableItem) calendarableEvents.get(0);
		assertEquals("item Text was changed", event.description, item.getText());
	}
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#dispose()}.
	 */
	public void testDispose() {
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 14));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 17, 9, 45), "Stand-up mtg")};
		dbc.bind(editorBindDesc, makeModel(testData), null);
		
		List daysToDispose = new LinkedList();
		for (int day=0; day < 7; ++day) {
			List calendarables = editor.model().getCalendarableItems(day);
			if (calendarables != null) {
				daysToDispose.addAll(calendarables);
			}
		}
		
		editor.setStartDate(date(5, 1));
		
		for (Iterator disposedDaysIter = daysToDispose.iterator(); disposedDaysIter.hasNext();) {
			CalendarableItem item = (CalendarableItem) disposedDaysIter.next();
			List bindingList = (List) item.getData("BindingBinding");
			for (Iterator bindingListIter = bindingList.iterator(); bindingListIter.hasNext();) {
				Binding binding = (Binding) bindingListIter.next();
				assertTrue("should be disposed", binding.isDisposed());
			}
		}
	}
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#add(int, java.lang.Object)}.
	 */
	public void testInsert() {
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg")};
		final WritableList model = makeModel(testData);
		
		LazyInsertDeleteProvider insertDeleteProvider = new LazyInsertDeleteProvider() {
			public NewObject insertElementAt(LazyInsertEvent e) {
				Event event = new Event(time(5, 17, 5, 45), time(5, 17, 9, 45), "Stand-up mtg 3");
				model.add(e.positionHint, event);
				return new NewObject(e.positionHint, event);
			}
			
			public void deleteElementAt(LazyDeleteEvent e) {
			}
		};
		
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		dbc.bind(editorBindDesc, model, new BindSpec().setLazyInsertDeleteProvider(insertDeleteProvider));	
		Event event = new Event (time(5, 16, 5, 45), time(5, 16, 9, 45), "Stand-up mtg 2");
		model.add(event);
		
		// Add the third event (to the target via the insertDeleteProvider)
		NewEvent results = editorBindDesc.editor.fireInsert(date(5, 17), false);
		
		assertEquals("start date", time(5, 17, 5, 45), results.startTimeEndTime[0]);
		assertEquals("end date", time(5, 17, 9, 45), results.startTimeEndTime[1]);
		
		assertEditorState(editor, new CalendarableItem[][] {
				{ci(date(5, 15), time(5, 45), time(9, 45), "Stand-up mtg")},
				{ci(date(5, 16), time(5, 45), time(9, 45), "Stand-up mtg 2")},
				{ci(date(5, 17), time(5, 45), time(9, 45), "Stand-up mtg 3")},
				{},
				{},
				{},
				{}
		});
	}
	
	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.compositetable.day.binding.EventEditorObservableLazyDataRequestor#remove(int)}.
	 */
	public void testRemove() {
		Event[] testData = new Event[] {
				new Event (time(5, 15, 5, 45), time(5, 15, 9, 45), "Stand-up mtg")};
		final WritableList model = makeModel(testData);
		
		LazyInsertDeleteProvider insertDeleteProvider = new LazyInsertDeleteProvider() {
			public NewObject insertElementAt(LazyInsertEvent e) {
				return null;
			}
			
			public boolean canDeleteElementAt(LazyDeleteEvent e) {
				return true;
			}
			
			public void deleteElementAt(LazyDeleteEvent e) {
				model.remove(e.position);
			}
		};
		
		editor.setTimeBreakdown(7, 4);
		editor.setStartDate(date(5, 15));
		
		EventEditorBindingDescription editorBindDesc = makeBindingDescription();
		dbc.bind(editorBindDesc, model, new BindSpec().setLazyInsertDeleteProvider(insertDeleteProvider));	
		Event event = new Event (time(5, 16, 5, 45), time(5, 16, 9, 45), "Stand-up mtg 2");
		model.add(event);
		
		boolean result = editorBindDesc.editor.fireDelete((CalendarableItem) editor.model.getCalendarableItems(0).get(0));
		
		assertTrue("Could delete", result);
		
		assertEditorState(editor, new CalendarableItem[][] {
				{},
				{ci(date(5, 16), time(5, 45), time(9, 45), "Stand-up mtg 2")},
				{},
				{},
				{},
				{},
				{}
		});
	}
}
