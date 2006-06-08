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

import java.util.Date;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;

/**
 * Class DayEditorSelection.  Represents the current selection in a DayEditor
 * control.
 * 
 * @since 3.3
 */
public class DayEditorSelection {

	/**
	 * Represents the selected CalendarableItem or null if none is selected.
	 */
	public CalendarableItem selectedCalendarable = null;
	
	/**
	 * Sets the selectedCalendarable.
	 * 
	 * @param selectedCalendarable the CalendarableItem to select
	 */
	public void setSelectedCalendarable(CalendarableItem selectedCalendarable) {
		this.selectedCalendarable = selectedCalendarable;
	}

	/**
	 * Indicates if the current selection is in an all-day event row.
	 */
	public boolean allDay = false;
	
	/**
	 * Sets the allDay flag.
	 * 
	 * @param allDay true if the selection is in an all-day event row or if
	 * the selectedCalenderable represents an all-day event; false otherwise.
	 */
	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	/**
	 * The date/time of the currently selected cell or null if selectedCalenderable != null
	 */
	public Date dateTime = null;
	
	/**
	 * Sets the dateTime.
	 * 
	 * @param date the Date to set.
	 */
	public void setDateTime(Date date) {
		this.dateTime = date;
	}

}
