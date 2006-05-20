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

/**
 * Encapsulates information about new events that have been added to the calendar.
 * 
 * @since 3.3
 */
public class NewEvent {
	
	/**
	 * The application's event object.  When an application returns a NewEvent,
	 * normally it will create a domain model object that represents the event.
	 * This field is for application use to pass that model object to listeners
	 * who may have requested for the new event to be created.  Clients are free
	 * to pass null if this is not applicable to them. 
	 */
	public final Object event;
	
	/**
	 * The start date/time and end date/time of the new event.  A Date[2] 
	 * containing the start date/time and the end date/time of the new event.
	 * The IEventEditor will automatically refresh any days it is displaying
	 * that overlap this date/time range. 
	 */
	public final Date[] startTimeEndTime;

	/**
	 * Construct a NewEvent.
	 * 
	 * @param event The application-defined event object or null if none.
	 * @param startTimeEndTime A Date[2] containing the range of dates/times
	 * 		this event spans.
	 */
	public NewEvent(final Object event, final Date[] startTimeEndTime) {
		super();
		this.event = event;
		this.startTimeEndTime = startTimeEndTime;
	}
}
