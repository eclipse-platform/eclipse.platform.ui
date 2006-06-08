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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.Date;

import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableSelectionChangeListener;
import org.eclipse.jface.examples.databinding.compositetable.day.NewEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.SelectionChangeEvent;
import org.eclipse.swt.SWTException;

/**
 * Interface IEventEditor.  An interface for editors of time-based data that
 * can be visualized on various calendar-like controls.
 * 
 * @since 3.2
 */
public interface IEventEditor {

	/** 
	 * The number of hours to display at a time.  Normally this is the number
	 * of hours in a day.
	 */
	public static final int DISPLAYED_HOURS = 24;

	/**
	 * Method setTimeBreakdown. Call this method exactly once after constructing
	 * the control in order to set the number of day columns to display.
	 * <p>
	 * This method may be executed exactly once. Executing more than once will
	 * result in undefined behavior.
	 * <p>
	 * This method is a <b>hint</b>.  It may be ignored by specific 
	 * implementations (ie: a month view).
	 * 
	 * @param numberOfDays
	 *            The number of days to display.
	 * @param numberOfDivisionsInHour
	 *            1 == one line per hour; 2 == every 1/2 hour; 4 = every 1/4
	 *            hour; etc...
	 */
	void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour);
	
	/**
	 * Method getNumberOfDays.  Returns the number of days being displayed
	 * in this IEventEditor.
	 * 
	 * @return The number of days being displayed.
	 */
	int getNumberOfDays();
	
	/**
	 * Returns the numberOfDivisionsInHour.  For example, to have a new
	 * time slice every 1/4 hour, this value would be 4.
	 * 
	 * @return Returns the numberOfDivisionsInHour.
	 */
	int getNumberOfDivisionsInHour();

	/**
	 * Set the start date for this event editor.  How this is interpreted depends
	 * on how time is being visualized.
	 * <p>
	 * For example, a month editor would only pay attention to the month portion
	 * of the date.  A multi-day editor would make the date passed be the first
	 * date edited in the set of days being visualized.
	 *  
	 * @param startDate The date representing what slice of time to visualize in the editor.  
	 * null is not permitted.  Passing null will result in undefined behavior.
	 */
	void setStartDate(Date startDate);
	
	/**
	 * Return the current start date for this event editor.  This is the date
	 * that was set in setStartDate.
	 * 
	 * @return The start date, or <code>null</code> if no start date has been specified yet.
	 */
	Date getStartDate();
	
	/**
	 * Set the strategy pattern object that can return how many events to
	 * display for specific periods of time.
	 * <p>
	 * Note that having a separate event count provider and event content
	 * provider assumes that the implementer is single-threaded and that the
	 * count can't change between calling the count provider and the content
	 * provider.
	 * 
	 * @param eventCountProvider
	 *            The eventCountProvider to set.
	 */
	void setEventCountProvider(EventCountProvider eventCountProvider);
	
	/**
	 * Sets the strategy pattern object that can set the properties of the event
	 * objects in order to display the data associated with the specified event.
	 * <p>
	 * Note that having a separate event count provider and event content
	 * provider assumes that the implementer is single-threaded and that the
	 * count can't change between calling the count provider and the content
	 * provider.
	 * 
	 * @param eventContentProvider
	 *            The eventContentProvider to set.
	 */
	void setEventContentProvider(EventContentProvider eventContentProvider);
	
	/**
	 * Tells the IEventEditor to refresh its display for the specified date.
	 * If the specified date is not being displayed, the request will be ignored.
	 * If null is passed as the date, the entire display is refreshed.
	 * 
	 * @param date The date to refresh or null to refresh everything.
	 */
	void refresh(Date date);
	
	/**
	 * Tells the IEventEditor to refresh all days in its display.
	 */
	void refresh();

	/**
	 * Adds the handler to the collection of handlers who will
	 * be notified when a CalendarableItem is inserted in the receiver, by sending
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
	 * @see #removeItemInsertHandler
	 */
	void addItemInsertHandler(CalendarableItemEventHandler insertHandler);

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
	void removeItemInsertHandler(CalendarableItemEventHandler insertHandler);

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
	public NewEvent fireInsert(Date date, boolean allDayEvent);
	
	/**
	 * Adds the handler to the collection of handlers who will
	 * be notified when a CalendarableItem is deleted from the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
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
	 * @see CalendarableItemEventHandler
	 * @see #removeDeleteItemHandler
	 */
	void addItemDeleteHandler(CalendarableItemEventHandler deleteHandler);

	/**
	 * Requests that the event editor delete the specified CalendarableItem's
	 * data.
	 *  
	 * @param toDelete The CalendarableItem to delete.
	 * @return true if successful; false otherwise.
	 */
	public boolean fireDelete(CalendarableItem toDelete);

	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is deleted from the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
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
	 * @see CalendarableItemEventHandler
	 * @see #addDeleteItemHandler
	 */
	void removeItemDeleteHandler(CalendarableItemEventHandler deleteHandler);

	/**
	 * Adds the handler to the collection of handler who will
	 * be notified when a CalendarableItem's control is disposed, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.  This is normally used to remove any data bindings
	 * that may be attached to the (now-unused) CalendarableItem.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
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
	 * @see CalendarableItemEventHandler
	 * @see #removeCalendarableItemDisposeHandler
	 */
	void addItemDisposeHandler(CalendarableItemEventHandler itemDisposeHandler);

	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is disposed, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.  This is normally used to remove any data bindings
	 * that may be attached to the (now-unused) CalendarableItem.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
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
	 * @see CalendarableItemEventHandler
	 * @see #removeDeleteListener
	 */
	void removeItemDisposeHandler(CalendarableItemEventHandler itemDisposeHandler);
	
	/**
	 * Adds the handler to the collection of handlers who will
	 * be notified when a CalendarableItem is inserted in the receiver, by sending
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
	 * @see #removeItemInsertHandler
	 */
	public void addItemEditHandler(CalendarableItemEventHandler handler);
	
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
	public void removeItemEditHandler(CalendarableItemEventHandler handler);

	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the receiver's selection changes, by sending
	 * it one of the messages defined in the <code>CalendarableSelectionChangeListener</code>
	 * interface.
	 * <p>
	 * <code>selectionChanged</code> is called when the selection changes.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableSelectionChangeListener
	 * @see #removeSelectionChangeListener
	 * @see SelectionChangeEvent
	 */
	public void addSelectionChangeListener(CalendarableSelectionChangeListener l);
	
	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when the receiver's selection changes, by sending
	 * it one of the messages defined in the <code>CalendarableSelectionChangeListener</code>
	 * interface.
	 * <p>
	 * <code>selectionChanged</code> is called when the selection changes.
	 * </p>
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableSelectionChangeListener
	 * @see #addSelectionChangeListener
	 * @see SelectionChangeEvent
	 */
	public void removeSelectionChangeListener(CalendarableSelectionChangeListener l);
}
