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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.RowConstructionListener;
import org.eclipse.jface.examples.databinding.compositetable.ScrollEvent;
import org.eclipse.jface.examples.databinding.compositetable.ScrollListener;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.CalendarableEventControl;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayLayoutsByDate;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayModel;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.TimeSlice;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * A DayEditor is an SWT control that can display events on a time line that can
 * span one or more days.  This class is not intended to be subclassed.
 * 
 * @since 3.2
 */
public class DayEditor extends Composite implements IEventEditor {
	/**
	 * The default start hour.  Normally 8:00 AM
	 */
	private CompositeTable compositeTable = null;
	private CalendarableModel model = new CalendarableModel();
	private DayLayoutsByDate dayLayoutsByDate;
	private List spareCalendarableEventControls = new LinkedList();
	protected TimeSlice daysHeader;

	/**
	 * Constructor DayEditor.  Constructs a calendar control that can display
	 * events on one or more days.
	 * 
	 * @param parent
	 * @param style
	 */
	public DayEditor(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		setLayout(new FillLayout());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setTimeBreakdown(int, int)
	 */
	public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		model.setTimeBreakdown(numberOfDays, numberOfDivisionsInHour);
		
		if (compositeTable != null) {
			compositeTable.dispose();
		}
		
		createCompositeTable(numberOfDays, numberOfDivisionsInHour);
		dayLayoutsByDate = new DayLayoutsByDate(model.getStartDate(), model.getNumberOfDays());
	}

	/**
	 * This method initializes compositeTable
	 * 
	 * @param numberOfDays
	 *            The number of day columns to display
	 */
	private void createCompositeTable(final int numberOfDays,
			final int numberOfDivisionsInHour) {
		compositeTable = new CompositeTable(this, SWT.NONE);
		new TimeSlice(compositeTable, SWT.BORDER);		// The prototype header
		new TimeSlice(compositeTable, SWT.NONE); // The prototype row
		
		compositeTable.setNumRowsInCollection(computeNumRowsInCollection(numberOfDivisionsInHour));
		
		compositeTable.addRowConstructionListener(new RowConstructionListener() {
			public void headerConstructed(Control newHeader) {
				daysHeader = (TimeSlice) newHeader;
				daysHeader.setHeaderControl(true);
				daysHeader.setNumberOfColumns(numberOfDays);
				if (model.getStartDate() == null) {
					return;
				}
				refreshColumnHeaders(daysHeader.getColumns());
			}
			
			public void rowConstructed(Control newRow) {
				TimeSlice timeSlice = (TimeSlice) newRow;
				timeSlice.setNumberOfColumns(numberOfDays);
			}
		});
		compositeTable.addRowContentProvider(new IRowContentProvider() {
			public void refresh(CompositeTable sender, int currentObjectOffset,
					Control row) {
				TimeSlice timeSlice = (TimeSlice) row;
				refreshRow(currentObjectOffset, timeSlice);
			}
		});
		compositeTable.addScrollListener(new ScrollListener() {
			public void tableScrolled(ScrollEvent scrollEvent) {
				refreshCalendarableEventControls();
			}
		});
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				refreshCalendarableEventControls();
			}
		});
		
		compositeTable.setRunTime(true);
	}

	/**
	 * @return Returns the defaultStartHour.
	 */
	public int getDefaultStartHour() {
		return model.getDefaultStartHour();
	}

	/**
	 * @param defaultStartHour The defaultStartHour to set.
	 */
	public void setDefaultStartHour(int defaultStartHour) {
		model.setDefaultStartHour(defaultStartHour);
		updateVisibleRows();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setDayEventCountProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider)
	 */
	public void setDayEventCountProvider(EventCountProvider eventCountProvider) {
		model.setDayEventCountProvider(eventCountProvider);
		updateVisibleRows();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventContentProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider)
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		model.setEventContentProvider(eventContentProvider);
		updateVisibleRows();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setStartDate(java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		model.setStartDate(startDate);
		refreshColumnHeaders(daysHeader.getColumns());
		updateVisibleRows();
		refreshCalendarableEventControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getStartDate()
	 */
	public Date getStartDate() {
		return model.getStartDate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh(java.util.Date)
	 */
	public void refresh(Date date) {
		model.refresh(date);
		updateVisibleRows();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getNumberOfDays()
	 */
	public int getNumberOfDays() {
		return model.getNumberOfDays();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getNumberOfDivisionsInHour()
	 */
	public int getNumberOfDivisionsInHour() {
		return model.getNumberOfDivisionsInHour();
	}
	
	// Display Refresh logic here ----------------------------------------------
	
	private int numberOfAllDayEventRows = 0;
	Calendar calendar = new GregorianCalendar();

	private int computeNumRowsInCollection(final int numberOfDivisionsInHour) {
		numberOfAllDayEventRows = model.computeNumberOfAllDayEventRows();
		return (DISPLAYED_HOURS-model.computeStartHour()) * numberOfDivisionsInHour+numberOfAllDayEventRows;
	}
	
	private int computeHourFromRow(int currentObjectOffset) {
		return currentObjectOffset / getNumberOfDivisionsInHour() + model.computeStartHour();
	}

	private int computeMinuteFromRow(int currentObjectOffset) {
		int numberOfDivisionsInHour = getNumberOfDivisionsInHour();
		int minute = (int) ((double) currentObjectOffset
				% numberOfDivisionsInHour
				/ numberOfDivisionsInHour * 60);
		return minute;
	}

	/*
	 * Update the number of rows that are displayed inside the CompositeTable control
	 */
	private void updateVisibleRows() {
		compositeTable.setNumRowsInCollection(computeNumRowsInCollection(getNumberOfDivisionsInHour()));
	}
	
	private void refreshRow(int currentObjectOffset, TimeSlice timeSlice) {
		// Decrement currentObjectOffset for each all-day event line we need.
		for (int allDayEventRow = 0; allDayEventRow < numberOfAllDayEventRows; ++allDayEventRow) {
			--currentObjectOffset;
		}
		
		if (currentObjectOffset < 0) {
			timeSlice.setCurrentTime(null);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 
					computeHourFromRow(currentObjectOffset));
			calendar.set(Calendar.MINUTE,
					computeMinuteFromRow(currentObjectOffset));
			timeSlice.setCurrentTime(calendar.getTime());
		}
	}

	/**
	 * (non-API) Method initializeColumnHeaders.  Called internally when the
	 * column header text needs to be updated.
	 * 
	 * @param columns A LinkedList of CLabels representing the column objects
	 */
	protected void refreshColumnHeaders(LinkedList columns) {
		Date startDate = getStartDate();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);

		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d");
		formatter.applyPattern(formatter.toLocalizedPattern());
		
		for (Iterator iter = columns.iterator(); iter.hasNext();) {
			CLabel headerLabel = (CLabel) iter.next();
			headerLabel.setText(formatter.format(gc.getTime()));
			gc.add(Calendar.DAY_OF_MONTH, 1);
		}
	}
	
	/**
	 * Make the correct event controls visible for the segment in time that
	 * we are currently displaying and resize them so that they occupy the
	 * correct portions of their day columns.
	 */
	private void refreshCalendarableEventControls() {
		DayModel dayLayoutFactory = new DayModel(model.getNumberOfDivisionsInHour());
		
		for (int i=0; i < model.getNumberOfDays(); ++i) {
			Calendarable[][] layout = dayLayoutFactory.getEventLayout(model.getCalendarableEvents(0));
		}
	}
	
	// CalendarableEventControl construction/destruction here -----------------
	
	private CalendarableEventControl newCEC() {
		if (spareCalendarableEventControls.size() > 0) {
			CalendarableEventControl result = (CalendarableEventControl) spareCalendarableEventControls.remove(0);
			result.setVisible(true);
			return result;
		}
		return new CalendarableEventControl(this, SWT.NULL);
	}
	
	private void freeCEC(CalendarableEventControl control) {
		control.setVisible(false);
		spareCalendarableEventControls.add(control);
	}

} // @jve:decl-index=0:visual-constraint="10,10"



