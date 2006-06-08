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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableSelectionChangeListener;
import org.eclipse.jface.examples.databinding.compositetable.day.NewEvent;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;

/**
 * A concrete class that implements IEventEditor for testing purposes.
 * <p>
 * Eventually, this could become an abstract parent class for creating 
 * arbitrary new IEventEditor implementations.
 * 
 * @since 3.3
 */
public class EventEditor implements IEventEditor {

	protected CalendarableModel model = new CalendarableModel();

	// Utilities --------------------------------------------------------------
	
	private void fireEvent(CalendarableItemEvent e, List handlers) {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.handleRequest(e);
			if (!e.doit) {
				break;
			}
		}
	}
	
	private CalendarableItemEvent calendarableItemEvent(CalendarableItem item) {
		CalendarableItemEvent e = new CalendarableItemEvent();
		e.calendarableItem = item;
		return e;
	}
	
	// Events ----------------------------------------------------------------
	
	private List insertHandlers = new ArrayList();
	
    public NewEvent fireInsert(Date date, boolean allDayEvent) {
		CalendarableItem item = new CalendarableItem(date);
		CalendarableItemEvent e = calendarableItemEvent(item);
		fireEvent(e, insertHandlers);
		if (e.doit) {
			// TODO: Only refresh days that need refreshing
			refresh();
			return (NewEvent) e.result;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#addItemInsertHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void addItemInsertHandler(CalendarableItemEventHandler insertHandler) {
		insertHandlers.add(insertHandler);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#removeItemInsertHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void removeItemInsertHandler(
			CalendarableItemEventHandler insertHandler) {
		insertHandlers.remove(insertHandler);
	}
	
	private List deleteHandlers = new ArrayList();

	public boolean fireDelete(CalendarableItem toDelete) {
		CalendarableItemEvent e = calendarableItemEvent(toDelete);
		fireEvent(e, deleteHandlers);
		if (e.doit) {
			// TODO: only refresh affected days
			refresh();
		}
		return e.doit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#addItemDeleteHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void addItemDeleteHandler(CalendarableItemEventHandler deleteHandler) {
		deleteHandlers.add(deleteHandler);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#removeItemDeleteHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void removeItemDeleteHandler(CalendarableItemEventHandler deleteHandler) {
		deleteHandlers.remove(deleteHandler);
	}

	private List editHandlers = new ArrayList();
	
	public void fireEdit(CalendarableItem toEdit) {
		fireEvent(calendarableItemEvent(toEdit), editHandlers);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#addItemEditHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void addItemEditHandler(CalendarableItemEventHandler handler) {
		editHandlers.add(handler);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#removeItemEditHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void removeItemEditHandler(CalendarableItemEventHandler handler) {
		editHandlers.remove(handler);
	}
	
	private List disposeHandlers = new ArrayList();

	public void fireDispose(CalendarableItem disposed) {
		fireEvent(calendarableItemEvent(disposed), disposeHandlers);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#addItemDisposeHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void addItemDisposeHandler(CalendarableItemEventHandler itemDisposeHandler) {
		disposeHandlers.add(itemDisposeHandler);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#removeItemDisposeHandler(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler)
	 */
	public void removeItemDisposeHandler(CalendarableItemEventHandler itemDisposeHandler) {
		disposeHandlers.remove(itemDisposeHandler);
	}
	
	// Model stuff ------------------------------------------------------------

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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setTimeBreakdown(int, int)
	 */
	public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		model.setTimeBreakdown(numberOfDays, numberOfDivisionsInHour);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventContentProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider)
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		model.setEventContentProvider(eventContentProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventCountProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider)
	 */
	public void setEventCountProvider(EventCountProvider eventCountProvider) {
		model.setEventCountProvider(eventCountProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setStartDate(java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		List disposedCalendarables = model.setStartDate(startDate);
		for (Iterator i = disposedCalendarables.iterator(); i.hasNext();) {
			CalendarableItem element = (CalendarableItem) i.next();
			fireDispose(element);
		}
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh()
	 */
	public void refresh() {
		Date dateToRefresh = getStartDate();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(dateToRefresh);
		for (int i=0; i < getNumberOfDays(); ++i) {
			refresh(gc.getTime());
			gc.add(Calendar.DATE, 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#addSelectionChangeListener(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableSelectionChangeListener)
	 */
	public void addSelectionChangeListener(CalendarableSelectionChangeListener l) {
		// no op
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#removeSelectionChangeListener(org.eclipse.jface.examples.databinding.compositetable.day.CalendarableSelectionChangeListener)
	 */
	public void removeSelectionChangeListener(CalendarableSelectionChangeListener l) {
		// no op
	}

}


