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

package org.eclipse.jface.examples.databinding.compositetable.day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.2
 *
 */
public abstract class AbstractEventEditor extends Composite {

	private int defaultEventDuration;
	private List insertHandlers = new ArrayList();

	/**
	 * @param parent Parent control
	 * @param style SWT style bit
	 * 
	 */
	public AbstractEventEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Tells the IEventEditor to refresh all days in its display.
	 */
	public abstract void refresh();
	
	/**
	 * Requests that the event editor attempt to insert a new element by calling
	 * its registered insert handlers
	 * 
	 * @param date
	 *            The date/time on which to request the insert. The actual date
	 *            on which the insert is performed may be different. This is a
	 *            HINT.
	 * 
	 * @param allDayEvent
	 *            Indicates if the new event should be an all-day event. This is
	 *            a HINT; the actual event inserted may be a timed event.
	 * 
	 * @return NewEvent a NewEvent object describing the event that was
	 *         inserted.
	 */
	public NewEvent fireInsert(Date date, boolean allDayEvent) {
		checkWidget();
		CalendarableItem item = new CalendarableItem(date);
		item.setAllDayEvent(allDayEvent);
		item.setStartTime(date);
		item.setEndTime(incrementHour(date, getDefaultEventDuration()));
		CalendarableItemEvent e = new CalendarableItemEvent();
		e.calendarableItem = item;
		if (fireEvents(e, insertHandlers)) {
			// TODO: Only refresh the affected days
			refresh();
			return (NewEvent) e.result;
		}
		return null;
	}

	/**
	 * Adds the handler to the collection of handlers who will be notified when
	 * a CalendarableItem is inserted in the receiver, by sending it one of the
	 * messages defined in the <code>CalendarableItemInsertHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is
	 * inserted.
	 * </p>
	 * 
	 * @param handler
	 *            the handler which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see CalendarableItemInsertHandler
	 * @see #removeItemInsertHandler
	 */
	public void addItemInsertHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		insertHandlers.add(handler);
	}

	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is inserted into the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemInsertHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is inserted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemInsertHandler
	 * @see #addItemInsertHandler
	 */
	public void removeItemInsertHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		insertHandlers.remove(handler);
	}

	/**
	 * Returns the default duration of a new event, in hours.
	 * 
	 * @return int the number of hours a new event occupies by default.
	 */
	public int getDefaultEventDuration() {
		checkWidget();
		return defaultEventDuration;
	}

	/**
	 * Sets the default duration of a new event, in hours.
	 * 
	 * @param defaultEventDuration
	 *            int the number of hours a new event occupies by default.
	 */
	public void setDefaultEventDuration(int defaultEventDuration) {
		checkWidget();
		this.defaultEventDuration = defaultEventDuration;
	}

	protected boolean fireEvents(CalendarableItemEvent e, List handlers) {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.handleRequest(e);
			if (!e.doit) {
				break;
			}
		}
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.requestHandled(e);
			if (!e.doit) {
				break;
			}
		}
		return e.doit;
	}

	private Date incrementHour(Date date, int increment) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, increment);
		return c.getTime();
	}


}
