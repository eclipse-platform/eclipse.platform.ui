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

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.CalendarableItemControl;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class represents an event that can be displayed on a calendar.
 * 
 * @since 3.2
 */
public class CalendarableItem extends ModelObject {
	
	/**
	 * 
	 */
	private static final String PROP_DATE = "date";

	/**
	 * A constant representing the name of the toolTipText property.
	 */
	public static final String PROP_TOOL_TIP_TEXT = "toolTipText";

	/**
	 * A constant representing the name of the data property.
	 */
	public static final String PROP_DATA = "data";

	/**
	 * A constant representing the name of the text property.
	 */
	public static final String PROP_TEXT = "text";

	/**
	 * A constant representing the name of the image property.
	 */
	public static final String PROP_IMAGE = "image";

	/**
	 * A constant representing the name of the endTime property.
	 */
	public static final String PROP_END_TIME = "endTime";

	/**
	 * A constant representing the name of the startTime property.
	 */
	public static final String PROP_START_TIME = "startTime";

	/**
	 * A constant representing the name of the allDayEvent property.
	 */
	public static final String PROP_ALL_DAY_EVENT = "allDayEvent";

	/**
	 * A constant representing the name of the continued property.
	 */
	public static final String PROP_CONTINUED = "continued";

	/**
	 * A comparator for CalendarableItem objects
	 */
	public static final Comparator comparator = new Comparator() {
		public int compare(Object c1, Object c2) {
			CalendarableItem cal1 = (CalendarableItem) c1;
			CalendarableItem cal2 = (CalendarableItem) c2;
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
	
	public CalendarableItem(Date day) {
		setDate(day);
	}
	
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
		boolean oldValue = this.allDayEvent;
		this.allDayEvent = allDayEvent;
		firePropertyChange(PROP_ALL_DAY_EVENT, oldValue, allDayEvent);
	}
	
	/**
	 * @return Returns the date on which the event falls.
	 */
	public Date getDate() {
		return startTime;
	}

	/**
	 * @param date Sets the date on which the event falls.  Ignores the time
	 * component of the date that is passed in.
	 */
	public void setDate(Date date) {
		Date oldValue = this.startTime;
		this.startTime = setDateComponentOf(date, startTime);
		this.endTime = setDateComponentOf(date, endTime);
		firePropertyChange(PROP_DATE, oldValue, startTime);
	}

	/**
	 * @param source
	 * @param into 
	 */
	private Date setDateComponentOf(Date source, Date into) {
		GregorianCalendar mergeSource = new GregorianCalendar();
		mergeSource.setTime(source);
		GregorianCalendar mergeTarget = new GregorianCalendar();
		mergeTarget.setTime(into);
		mergeTarget.set(Calendar.MONTH, mergeSource.get(Calendar.MONTH));
		mergeTarget.set(Calendar.DAY_OF_MONTH, mergeSource.get(Calendar.DAY_OF_MONTH));
		mergeTarget.set(Calendar.YEAR, mergeSource.get(Calendar.YEAR));
		return mergeTarget.getTime();
	}
	
	/**
	 * @param source
	 * @param into 
	 */
	private Date setTimeComponentOf(Date source, Date into) {
		GregorianCalendar mergeSource = new GregorianCalendar();
		mergeSource.setTime(source);
		GregorianCalendar mergeTarget = new GregorianCalendar();
		mergeTarget.setTime(into);
		mergeTarget.set(Calendar.AM_PM, mergeSource.get(Calendar.AM_PM));
		mergeTarget.set(Calendar.HOUR, mergeSource.get(Calendar.HOUR));
		mergeTarget.set(Calendar.MINUTE, mergeSource.get(Calendar.MINUTE));
		mergeTarget.set(Calendar.SECOND, mergeSource.get(Calendar.SECOND));
		mergeTarget.set(Calendar.MILLISECOND, mergeSource.get(Calendar.MILLISECOND));
		return mergeTarget.getTime();
	}
	
	private Date startTime = new Date();
	
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
		Date oldValue = this.startTime;
		this.startTime = setTimeComponentOf(startTime, this.startTime);
		firePropertyChange(PROP_START_TIME, oldValue, startTime);
	}
	
	private Date endTime = new Date();

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
		Date oldValue = this.endTime;
		this.endTime = setTimeComponentOf(endTime, this.endTime);
		firePropertyChange(PROP_END_TIME, oldValue, endTime);
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
		Image oldValue = this.image;
		this.image = image;
		if (control != null) {
			control.setImage(image);
		}
		firePropertyChange(PROP_IMAGE, oldValue, image);
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
		String oldValue = this.text;
		this.text = string;
		if (control != null) {
			control.setText(string);
		}
		firePropertyChange(PROP_TEXT, oldValue, text);
	}
	
	private String toolTipText;
	
	/**
	 * Returns the receiver's tool tip text, or null if it has
	 * not been set.
	 *
	 * @return the receiver's tool tip text
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getToolTipText() {
		return toolTipText;
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which
	 * may be null indicating that no tool tip text should be shown.
	 *
	 * @param string the new tool tip text (or null)
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setToolTipText(String string) {
		String oldValue = this.toolTipText;
		this.toolTipText = string;
		if (control != null) {
			control.setToolTipText(string);
		}
		firePropertyChange(PROP_TOOL_TIP_TEXT, oldValue, toolTipText);
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
		Object oldValue = this.data;
		this.data = data;
		firePropertyChange(PROP_DATA, oldValue, data);
	}
	
	private Map dataMap = new HashMap();
	
	/**
	 * Sets the application defined property of the receiver
	 * with the specified name to the given value.
	 * <p>
	 * Applications may associate arbitrary objects with the
	 * receiver in this fashion. 
	 * </p>
	 *
	 * @param key the name of the property
	 * @param value the new value for the property
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 * </ul>
	 *
	 * @see #getData(String)
	 */
	public void setData(String key, Object data) {
		if (key == null) {
			throw new IllegalArgumentException("key is null");
		}
		dataMap.put(key, data);
	}
	
	/**
	 * Returns the application defined property of the receiver
	 * with the specified name, or null if it has not been set.
	 * <p>
	 * Applications may have associated arbitrary objects with the
	 * receiver in this fashion.
	 * </p>
	 *
	 * @param	key the name of the property
	 * @return the value of the property or null if it has not been set
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 * </ul>
	 *
	 * @see #setData(String, Object)
	 */
	public Object getData(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key is null");
		}
		return dataMap.get(key);
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
	 * lower right position to be the same as the upper left if the lower right
	 * has not yet been set.  If the lower right has been set, it is left as it
	 * is.
	 * 
	 * @param upperLeftPositionInDayRowCoordinates The upperLeftPositionInDayRowCoordinates to set.
	 */
	public void setUpperLeftPositionInDayRowCoordinates(
			Point upperLeftPositionInDayRowCoordinates) {
		this.upperLeftPositionInDayRowCoordinates = upperLeftPositionInDayRowCoordinates;

		if (lowerRightPositionInDayRowCoordinates == null) {
			this.lowerRightPositionInDayRowCoordinates = upperLeftPositionInDayRowCoordinates;
		}
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

	private CalendarableItemControl control = null;
	
	/**
	 * (non-API)
	 * Returns the UI control for this CalendarableItem.
	 * 
	 * @return The UI control for this CalendarableItem or null if there is none.
	 */
	public CalendarableItemControl getControl() {
		return control;
	}

	/**
	 * (non-API)
	 * Set the UI control for this CalendarableItem.
	 * 
	 * @param control The control to set.
	 */
	public void setControl(CalendarableItemControl control) {
		if (control == null) {
			this.control.setCalendarableItem(null);
		}
		this.control = control;
		if (control != null) {
			control.setCalendarableItem(this);
			control.setContinued(continued);
		}
	}

	private int continued;

	public static final String BINDING_KEY = "BindingBinding";
	public static final String DATA_KEY = "BindingData";
	
	/**
	 * Sets the "To be continued..." bitmask indicating that this event is
	 * continued on the next or previous days respectively.
	 * 
	 * @param continued One or both of SWT.TOP or SWT.BOTTOM
	 */
	public void setContinued(int continued) {
		this.continued = continued;
		if (control != null) {
			control.setContinued(continued);
		}
	}
	
	/**
	 * @return The continued bitmask; one or both of SWT.TOP or SWT.BOTTOM
	 */
	public int getContinued() {
		return continued;
	}

}


