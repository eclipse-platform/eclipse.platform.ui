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

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;

/**
 * Represents a SWT control that displays a calenderable event.
 * 
 * @since 3.2
 */
public interface IEvent extends ISWTCanvas {
	/**
	 * Reset the event object to its default state.  This method does not
	 * dispose any Color or Image objects that may be set into it.
	 */
	void reset();
	
	/**
	 * Gets the event's start time.
	 * 
	 * @return the start time for the event.
	 */
	Date getStartTime();
	
	/**
	 * Sets the event's start time.
	 * 
	 * @param startTime the event's start time.
	 */
	void setStartTime(Date startTime);
	
	/**
	 * Returns the event's end time.
	 * 
	 * @return the event's end time.
	 */
	Date getEndTime();
	
	/**
	 * Sets the event's end time.
	 * 
	 * @param endTime the event's end time.
	 */
	void setEndTime(Date endTime);
	
	/**
	 * Return the IEvent's image or <code>null</code>.
	 * 
	 * @return the image of the label or null
	 */
	Image getImage();
	
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
	void setImage(Image image);
	
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
	String getText();
	
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
	void setText(String string);
}
