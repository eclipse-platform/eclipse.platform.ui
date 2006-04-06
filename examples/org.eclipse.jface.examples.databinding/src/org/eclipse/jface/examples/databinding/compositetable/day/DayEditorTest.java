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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class DayEditorTest {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"
	private DayEditor dayEditor = null;
	
	private static class Event {
		public Date startTime;
		public Date endTime;
		public String description;
		
		public Event(Date startTime, Date endTime, String description) {
			this.startTime = startTime;
			this.endTime = endTime;
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
			{new Event(time(8, 45), time(9, 45), "Stand-up comedy"),
				new Event(time(11, 00), time(12, 15), "Meet with customer")},
			{},
			{},
			{new Event(time(7, 50), time(9, 00), "Stand-up meeting"),
				new Event(time(10, 15), time(12, 00), "Work on prototype")},
			{new Event(time(8, 30), time(11, 30), "Stand-up comedy"),
				new Event(time(10, 00), time(12, 15), "Meet with customer"),
				new Event(time(11, 45), time(12, 15), "Meet with customer"),
				new Event(time(11, 00), time(2, 45), "Meet with customer")},
			{},
			{},
	};

	/**
	 * This method initializes dayEditor
	 * 
	 */
	private void createDayEditor() {
		dayEditor = new DayEditor(sShell, SWT.NONE);
		dayEditor.setTimeBreakdown(7, 2);
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Day Editor Test");
		sShell.setLayout(new FillLayout());
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
