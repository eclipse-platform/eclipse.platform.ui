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

package org.eclipse.jface.examples.databinding.compositetable.day.binding;

import java.util.Date;

import org.eclipse.jface.internal.databinding.provisional.observable.IDiff;

/**
 * When a multi-day calendarable event is edited, clients may need to know
 * about changes in the span of days covered.  This class encapsulates those
 * changes.
 * 
 * @since 3.3
 */
public class EventDateTimeDiff implements IDiff {
	private final Date oldStartDateTime;
	private final Date oldEndDateTime;
	private Date newStartDateTime;
	private Date newEndDateTime;
	
	/**
	 * @param oldStartDateTime
	 * @param oldEndDateTime
	 */
	public EventDateTimeDiff(Date oldStartDateTime, Date oldEndDateTime) {
		this.oldStartDateTime = oldStartDateTime;
		this.oldEndDateTime = oldEndDateTime;
	}

	/**
	 * @param oldStartDateTime
	 * @param oldEndDateTime
	 * @param newStartDateTime
	 * @param newEndDateTime
	 */
	public EventDateTimeDiff(Date oldStartDateTime, Date oldEndDateTime, Date newStartDateTime, Date newEndDateTime) {
		this.oldStartDateTime = oldStartDateTime;
		this.oldEndDateTime = oldEndDateTime;
		this.newStartDateTime = newStartDateTime;
		this.newEndDateTime = newEndDateTime;
	}

	/**
	 * @return Returns the newEndDateTime.
	 */
	public Date getNewEndDateTime() {
		return newEndDateTime;
	}

	/**
	 * @param newEndDateTime The newEndDateTime to set.
	 */
	public void setNewEndDateTime(Date newEndDateTime) {
		this.newEndDateTime = newEndDateTime;
	}

	/**
	 * @return Returns the newStartDateTime.
	 */
	public Date getNewStartDateTime() {
		return newStartDateTime;
	}

	/**
	 * @param newStartDateTime The newStartDateTime to set.
	 */
	public void setNewStartDateTime(Date newStartDateTime) {
		this.newStartDateTime = newStartDateTime;
	}

	/**
	 * @return Returns the oldEndDateTime.
	 */
	public Date getOldEndDateTime() {
		return oldEndDateTime;
	}

	/**
	 * @return Returns the oldStartDateTime.
	 */
	public Date getOldStartDateTime() {
		return oldStartDateTime;
	}

	/**
	 * @param startDateTime
	 * @param endDateTime
	 */
	public void setNewDateTimes(Date startDateTime, Date endDateTime) {
		setNewStartDateTime(startDateTime);
		setNewEndDateTime(endDateTime);
	}

}
