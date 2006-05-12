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
package org.eclipse.jface.examples.databinding.compositetable.day;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class DayEditorTest {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"
	private Composite navBar = null;
	private Button left = null;
	private Button right = null;
	private Label startDateLabel = null;
	private DayEditor dayEditor = null;
	
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
	
	private Event[][] events = new Event[][] {
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
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
			{},
	};

	/**
	 * This method initializes navBar
	 */
	private void createNavBar() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData1 = new org.eclipse.swt.layout.GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		navBar = new Composite(sShell, SWT.NONE);
		navBar.setLayoutData(gridData1);
		navBar.setLayout(gridLayout);
		left = new Button(navBar, SWT.LEFT | SWT.ARROW);
		left.addSelectionListener(previous);
		startDateLabel = new Label(navBar, SWT.NONE);
		right = new Button(navBar, SWT.RIGHT | SWT.ARROW);
		right.addSelectionListener(next);
	}

	/**
	 * This method initializes dayEditor
	 */
	private void createDayEditor() {
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		dayEditor = new DayEditor(sShell, SWT.NONE);
		dayEditor.setTimeBreakdown(7, 4);
		
		dayEditor.setDefaultStartHour(8);
		dayEditor.setEventCountProvider(eventCountProvider);
		dayEditor.setEventContentProvider(eventContentProvider);
		dayEditor.setLayoutData(gridData);
		updateStartDate();
	}

	private Date startDate = new Date();

	private void updateStartDate() {
		startDateLabel.setText(DateFormat.getDateInstance(DateFormat.FULL).format(startDate));
		startDateLabel.getParent().getParent().layout();
		dayEditor.setStartDate(startDate);
	}

	private void mutateDayOfMonth(int offset) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(Calendar.DAY_OF_MONTH, offset);
		startDate = gc.getTime();
		updateStartDate();
	}

	private SelectionAdapter previous = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			mutateDayOfMonth(-1);
		}
	};
	
	private SelectionAdapter next = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			mutateDayOfMonth(1);
		}
	};
	
	private EventCountProvider eventCountProvider = new EventCountProvider() {
		public int getNumberOfEventsInDay(Date day) {
			return events[getOffset(day)].length;
		}
	};
	
	private EventContentProvider eventContentProvider = new EventContentProvider() {
		public void refresh(Date day, CalendarableItem[] controls) {
			int dayOffset = getOffset(day);
			
			for (int event=0; event < events[dayOffset].length; ++event) {
				fillEvent(controls[event], events[dayOffset][event]);
			}
		}

		private void fillEvent(CalendarableItem c, Event event) {
			if (event.allDay) {
				c.setAllDayEvent(true);
			} else {
				c.setStartTime(event.startTime);
				c.setEndTime(event.endTime);
			}
			c.setData(event);
			c.setText(event.description);
		}
	};

	protected int getOffset(Date day) {
		GregorianCalendar dateToFind = new GregorianCalendar();
		dateToFind.setTime(day);
		GregorianCalendar dateToTest = new GregorianCalendar();
		dateToTest.setTime(new Date());
		for (int i=0; i < events.length; ++i) {
			if (dateToTest.get(Calendar.MONTH) == dateToFind.get(Calendar.MONTH) &&
					dateToTest.get(Calendar.DAY_OF_MONTH) == dateToFind.get(Calendar.DAY_OF_MONTH) &&
					dateToTest.get(Calendar.YEAR) == dateToFind.get(Calendar.YEAR)) 
			{
				return i;
			}
			dateToTest.add(Calendar.DAY_OF_MONTH, 1);
		}
		throw new IndexOutOfBoundsException(day + " does not have any data");
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Day Editor Test");
		sShell.setLayout(new GridLayout());
		createNavBar();
		createDayEditor();
		sShell.setSize(new org.eclipse.swt.graphics.Point(800, 592));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		DayEditorTest thisClass = new DayEditorTest();
		thisClass.createSShell();
		thisClass.sShell.open();
		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
