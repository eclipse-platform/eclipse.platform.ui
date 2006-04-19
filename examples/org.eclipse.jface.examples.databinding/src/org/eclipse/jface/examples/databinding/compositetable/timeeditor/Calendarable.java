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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.jface.examples.databinding.compositetable.day.internal.CalendarableEventControl;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class represents an event that can be displayed on a calendar.
 * 
 * @since 3.2
 */
public class Calendarable {
	
	/**
	 * A comparator for Calenarable objects
	 */
	public static final Comparator comparator = new Comparator() {
		public int compare(Object c1, Object c2) {
			Calendarable cal1 = (Calendarable) c1;
			Calendarable cal2 = (Calendarable) c2;
			if (cal1.isAllDayEvent()) {
				if (cal2.isAllDayEvent()) {
					return 0;
				}
				return -1;
			}
			if (cal2.isAllDayEvent()) {
				return 1;
			}
			return cal1.getStartTime().compareTo(cal2.getStartTime());
		}
	};
	
	private boolean allDayEvent = false;
	
	/**
	 * Returns if this Calenderable represents an all-day event.
	 * 
	 * @return true if this is an all-day event; false otherwise.
	 */
	public boolean isAllDayEvent() {
		return allDayEvent;
	}
	
	/**
	 * Sets if this Calenderable represents an all-day event.
	 * 
	 * @param allDayEvent true if this is an all-day event; false otherwise.
	 */
	public void setAllDayEvent(boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
	}
	
	private Date startTime = null;
	
	/**
	 * Gets the event's start time.  This value is ignored if this is an all-day event.
	 * 
	 * @return the start time for the event.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Sets the event's start time.  This value is ignored if this is an all-day event.
	 * 
	 * @param startTime the event's start time.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	private Date endTime = null;


	/**
	 * Returns the event's end time.  This value is ignored if this is an all-day event.
	 * 
	 * @return the event's end time.  This value is ignored if this is an all-day event.
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Sets the event's end time.  This value is ignored if this is an all-day event.
	 * 
	 * @param endTime the event's end time.  This value is ignored if this is an all-day event.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	private Image image;

	/**
	 * Return the IEvent's image or <code>null</code>.
	 * 
	 * @return the image of the label or null
	 */
	public Image getImage() {
		return this.image;
	}

	/**
	 * Set the IEvent's Image.
	 * The value <code>null</code> clears it.
	 * 
	 * @param image the image to be displayed in the label or null
	 * 
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	private String text = null;

	/**
	 * Returns the widget text.
	 * <p>
	 * The text for a text widget is the characters in the widget, or
	 * an empty string if this has never been set.
	 * </p>
	 *
	 * @return the widget text
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the contents of the receiver to the given string. If the receiver has style
	 * SINGLE and the argument contains multiple lines of text, the result of this
	 * operation is undefined and may vary from platform to platform.
	 *
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setText(String string) {
		this.text = string;
	}
	
	private Object data = null;
	
	/**
	 * Returns the application defined widget data associated
	 * with the receiver, or null if it has not been set. The
	 * <em>widget data</em> is a single, unnamed field that is
	 * stored with every widget. 
	 * <p>
	 * Applications may put arbitrary objects in this field. If
	 * the object stored in the widget data needs to be notified
	 * when the widget is disposed of, it is the application's
	 * responsibility to hook the Dispose event on the widget and
	 * do so.
	 * </p>
	 *
	 * @return the widget data
	 * @see #setData(Object)
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the application defined widget data associated
	 * with the receiver to be the argument. The <em>widget
	 * data</em> is a single, unnamed field that is stored
	 * with every widget. 
	 * <p>
	 * Applications may put arbitrary objects in this field. If
	 * the object stored in the widget data needs to be notified
	 * when the widget is disposed of, it is the application's
	 * responsibility to hook the Dispose event on the widget and
	 * do so.
	 * </p>
	 *
	 * @param data the widget data
	 * @see #getData()
	 */
	public void setData(Object data) {
		this.data = data;
	}
	
	private Point upperLeftPositionInDayRowCoordinates = null;
	

	/**
	 * (non-API)
	 * @return Returns the upperLeftPositionInDayRowCoordinates.
	 */
	public Point getUpperLeftPositionInDayRowCoordinates() {
		return upperLeftPositionInDayRowCoordinates;
	}

	/**
	 * (non-API)
	 * Sets the upper left position of the bounding box and initializes the
	 * lower right position to be the same as the upper left.
	 * 
	 * @param upperLeftPositionInDayRowCoordinates The upperLeftPositionInDayRowCoordinates to set.
	 */
	public void setUpperLeftPositionInDayRowCoordinates(
			Point upperLeftPositionInDayRowCoordinates) {
		this.upperLeftPositionInDayRowCoordinates = upperLeftPositionInDayRowCoordinates;
		this.lowerRightPositionInDayRowCoordinates = upperLeftPositionInDayRowCoordinates;
	}
	
	private Point lowerRightPositionInDayRowCoordinates = null;

	/**
	 * (non-API)
	 * @return Returns the lowerRightPositionInDayRowCoordinates.
	 */
	public Point getLowerRightPositionInDayRowCoordinates() {
		return lowerRightPositionInDayRowCoordinates;
	}

	/**
	 * (non-API)
	 * Sets the lower right position of the bounding box.
	 * 
	 * @param lowerRightPositionInDayRowCoordinates The lowerRightPositionInDayRowCoordinates to set.
	 */
	public void setLowerRightPositionInDayRowCoordinates(
			Point lowerRightPositionInDayRowCoordinates) {
		this.lowerRightPositionInDayRowCoordinates = lowerRightPositionInDayRowCoordinates;
	}

	private CalendarableEventControl control = null;
	
	/**
	 * (non-API)
	 * Returns the UI control for this Calendarable.
	 * 
	 * @return The UI control for this Calendarable or null if there is none.
	 */
	public CalendarableEventControl getControl() {
		return control;
	}

	/**
	 * (non-API)
	 * Set the UI control for this Calendarable.
	 * 
	 * @param control The control to set.
	 */
	public void setControl(CalendarableEventControl control) {
		if (control == null) {
			this.control.setCalendarable(null);
		}
		this.control = control;
		if (control != null) {
			control.setCalendarable(this);
		}
	}

}


